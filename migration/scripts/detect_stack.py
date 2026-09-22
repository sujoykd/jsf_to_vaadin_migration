#!/usr/bin/env python3
"""Detect the technology stack of a JSF 2 project to plan a Vaadin 25 migration.

Scans a project source tree and reports, as JSON on stdout:
  namespace        "javax" | "jakarta" | "mixed"   (which EE namespace dominates)
  java             detected source/target Java version (best effort)
  build            "maven" | "gradle" | "unknown"
  packaging        "war" | "jar" | "unknown"
  di               list of DI/CDI frameworks detected (cdi, deltaspike, spring, ...)
  persistence      list (jpa, hibernate<major>, eclipselink, ...)
  security         list (shiro, spring-security, jaas, ...)
  ui               list (jsf, primefaces, omnifaces, richfaces, ...)
  views            count of navigable *.xhtml full pages (template clients / html pages)
  counts           raw javax/jakarta import counts
  migrationTasks   conversion tasks implied by the gaps (done per-feature in the [port] issues)
  notes            human-readable observations

Target is ALWAYS Vaadin 25 + Spring Boot (per the migration strategy); this script
only characterizes the SOURCE so the migration can size the work.

Usage:
    detect_stack.py <project-root>

Pure stdlib. Heuristic — verify anything surprising against the actual files.
"""
import json
import os
import re
import sys
import xml.etree.ElementTree as ET

JAVAX_RE = re.compile(r"^\s*import\s+javax\.", re.MULTILINE)
JAKARTA_RE = re.compile(r"^\s*import\s+jakarta\.", re.MULTILINE)

# Marker substrings -> capability label. Checked against pom.xml / build.gradle text
# and, for a few, against import scans.
DEP_MARKERS = {
    "di": [
        ("deltaspike", "deltaspike"),
        ("spring-context", "spring"),
        ("spring-boot", "spring-boot"),
    ],
    "persistence": [
        ("hibernate-core", "hibernate"),
        ("eclipselink", "eclipselink"),
        ("spring-data-jpa", "spring-data-jpa"),
    ],
    "security": [
        ("shiro", "shiro"),
        ("spring-security", "spring-security"),
        ("keycloak", "keycloak"),
    ],
    "ui": [
        ("primefaces", "primefaces"),
        ("omnifaces", "omnifaces"),
        ("richfaces", "richfaces"),
        ("icefaces", "icefaces"),
        ("javax.faces", "jsf"),
        ("jakarta.faces", "jsf"),
        ("myfaces", "jsf"),
    ],
}


def read(path):
    try:
        with open(path, "r", encoding="utf-8", errors="replace") as fh:
            return fh.read()
    except OSError:
        return ""


def scan_imports(root):
    javax = jakarta = 0
    cdi = False
    for dirpath, _d, files in os.walk(root):
        if os.sep + "target" in dirpath or os.sep + "build" in dirpath:
            continue
        for f in files:
            if not f.endswith(".java"):
                continue
            txt = read(os.path.join(dirpath, f))
            javax += len(JAVAX_RE.findall(txt))
            jakarta += len(JAKARTA_RE.findall(txt))
            if "javax.inject.Inject" in txt or "jakarta.inject.Inject" in txt or "enterprise.context" in txt:
                cdi = True
    return javax, jakarta, cdi


def detect_java_and_packaging(pom_text):
    java = None
    packaging = "unknown"
    if pom_text:
        m = re.search(r"<maven\.compiler\.(?:source|release)>\s*(\d+)", pom_text)
        if not m:
            m = re.search(r"<source>\s*(\d+)\s*</source>", pom_text)
        if not m:
            m = re.search(r"<java\.version>\s*(\d+)", pom_text)
        if m:
            java = m.group(1)
        pm = re.search(r"<packaging>\s*(war|jar|ear)\s*</packaging>", pom_text)
        if pm:
            packaging = pm.group(1)
    return java, packaging


def hibernate_major(pom_text):
    m = re.search(r"hibernate[.-](?:core[.-]?version|version)?\s*[>:=]?\s*(\d+)\.", pom_text)
    m = re.search(r"<hibernate\.version>\s*(\d+)\.", pom_text) or m
    if not m:
        m = re.search(r"hibernate-core</artifactId>\s*<version>\s*(\d+)\.", pom_text, re.DOTALL)
    return m.group(1) if m else None


def collect_caps(haystack, pom_text):
    caps = {k: [] for k in DEP_MARKERS}
    for cap, markers in DEP_MARKERS.items():
        for needle, label in markers:
            if needle in haystack and label not in caps[cap]:
                caps[cap].append(label)
    hib = hibernate_major(pom_text)
    if hib:
        caps["persistence"] = [f"hibernate{hib}" if c == "hibernate" else c for c in caps["persistence"]]
        if f"hibernate{hib}" not in caps["persistence"]:
            caps["persistence"].append(f"hibernate{hib}")
    return caps


def count_views(root):
    """Count full-page *.xhtml (html pages or template clients), excluding fragments."""
    full = re.compile(r"<\s*(html|f:view|h:body)\b", re.I)
    frag = re.compile(r"<\s*(ui:composition|ui:component|ui:fragment)\b", re.I)
    tmpl = re.compile(r"\btemplate\s*=", re.I)
    n = 0
    for dirpath, _d, files in os.walk(root):
        if os.sep + "target" in dirpath or os.sep + "build" in dirpath:
            continue
        for f in files:
            if not f.endswith(".xhtml") or f.lower().startswith(("dialog", "sidebar")):
                continue
            if os.sep + "template" + os.sep in dirpath + os.sep:
                continue
            head = read(os.path.join(dirpath, f))[:4096]
            if full.search(head) or (frag.search(head) and tmpl.search(head)):
                n += 1
    return n


PKG_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)
# Markers that identify framework-neutral service/domain code to RETAIN (copy + port).
RETAIN_MARKERS = ("@Entity", "@ApplicationScoped", "@Stateless", "@Singleton",
                  "@Transactional", "@Repository", "EntityRepository", "BusinessLogic",
                  "@MappedSuperclass")
# Markers that identify UI/JSF code to REWRITE (not copied to the Vaadin app).
UI_MARKERS = ("javax.faces", "jakarta.faces", "org.primefaces", "org.omnifaces",
              "FacesContext", "PrimeFaces")


def _iter_java(java_root):
    for dirpath, _d, files in os.walk(java_root):
        if os.sep + "target" in dirpath or os.sep + "build" in dirpath:
            continue
        for f in files:
            if f.endswith(".java"):
                yield os.path.join(dirpath, f)


def detect_base_package(java_root):
    """Longest common dotted package prefix across all .java files (app's root package)."""
    pkgs = []
    for path in _iter_java(java_root):
        m = PKG_RE.search(read(path))
        if m:
            pkgs.append(m.group(1))
    if not pkgs:
        return None
    common = []
    for parts in zip(*[p.split(".") for p in pkgs]):
        if len(set(parts)) == 1:
            common.append(parts[0])
        else:
            break
    return ".".join(common) or None


def classify_packages(java_root):
    """Split packages into service-layer (retain/copy) vs UI (rewrite), by their markers.
    Generic: driven by annotations/imports, not by package names."""
    info = {}  # package -> {"svc": bool, "ui": bool}
    for path in _iter_java(java_root):
        txt = read(path)
        m = PKG_RE.search(txt)
        if not m:
            continue
        pkg = m.group(1)
        rec = info.setdefault(pkg, {"svc": False, "ui": False})
        if any(mk in txt for mk in UI_MARKERS):
            rec["ui"] = True
        if any(mk in txt for mk in RETAIN_MARKERS):
            rec["svc"] = True
    retain = sorted(p for p, r in info.items() if r["svc"] and not r["ui"])
    rewrite = sorted(p for p, r in info.items() if r["ui"])
    return retain, rewrite


def migration_tasks(namespace, java, caps):
    """Human-readable conversion tasks implied by the detected stack. These are carried out
    per-feature in the [port] issues (the service-layer logic is kept; only plumbing changes)."""
    tasks = []
    if namespace in ("javax", "mixed"):
        tasks.append("javax.* -> jakarta.* namespace migration")
    if java and int(java) < 21:
        tasks.append(f"bump Java {java} -> 21")
    if "spring" not in caps.get("di", []) and "spring-boot" not in caps.get("di", []):
        tasks.append("CDI/Deltaspike -> Spring DI + Spring Data JPA")
    if any(c.startswith("hibernate5") for c in caps.get("persistence", [])):
        tasks.append("Hibernate 5 -> 6")
    if "shiro" in caps.get("security", []):
        tasks.append("Shiro -> Spring Security (UI auth)")
    return tasks


def main(argv):
    if len(argv) != 2:
        sys.stderr.write("usage: detect_stack.py <project-root>\n")
        return 2
    root = os.path.abspath(argv[1])
    if not os.path.isdir(root):
        sys.stderr.write(f"error: not a directory: {root}\n")
        return 1

    pom = os.path.join(root, "pom.xml")
    gradle = os.path.join(root, "build.gradle")
    pom_text = read(pom)
    gradle_text = read(gradle) + read(os.path.join(root, "build.gradle.kts"))
    build = "maven" if pom_text else ("gradle" if gradle_text else "unknown")
    haystack = pom_text + "\n" + gradle_text

    javax, jakarta, cdi = scan_imports(root)
    if javax and jakarta:
        namespace = "mixed"
    elif jakarta:
        namespace = "jakarta"
    elif javax:
        namespace = "javax"
    else:
        namespace = "unknown"

    java, packaging = detect_java_and_packaging(pom_text)
    caps = collect_caps(haystack, pom_text)
    if cdi and "cdi" not in caps["di"]:
        caps["di"].insert(0, "cdi")

    java_root = os.path.join(root, "src", "main", "java")
    base_package = detect_base_package(java_root) if os.path.isdir(java_root) else None
    retain_packages, rewrite_packages = classify_packages(java_root) if os.path.isdir(java_root) else ([], [])

    manifest = {
        "target": {"vaadin": "25", "springBoot": "4", "java": "21", "jakartaEE": "11", "uiModel": "flow"},
        "source": {
            "namespace": namespace,
            "java": java,
            "build": build,
            "packaging": packaging,
            "basePackage": base_package,
            "di": caps["di"],
            "persistence": caps["persistence"],
            "security": caps["security"],
            "ui": caps["ui"],
            "views": count_views(root),
            "counts": {"javaxImports": javax, "jakartaImports": jakarta},
            # framework-neutral packages to copy/port vs JSF/UI packages to rewrite
            "retainPackages": retain_packages,
            "rewritePackages": rewrite_packages,
        },
        "migrationTasks": migration_tasks(namespace, java, caps),
        "notes": [],
    }
    if namespace == "javax":
        manifest["notes"].append(
            "javax.* detected: a full javax->jakarta migration is required to reach Vaadin 25 "
            "(Vaadin 24+ are all Jakarta-based)."
        )
    if java and int(java) < 21:
        manifest["notes"].append(f"Java {java} detected: Vaadin 25 requires Java 21 — bump the toolchain.")
    if "spring" not in caps["di"] and "spring-boot" not in caps["di"]:
        manifest["notes"].append("No Spring detected: DI must be ported CDI/Deltaspike -> Spring (mechanical).")

    json.dump(manifest, sys.stdout, indent=2)
    sys.stdout.write("\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
