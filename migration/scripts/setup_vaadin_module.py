#!/usr/bin/env python3
"""Scaffold a self-contained Vaadin 25 / Spring Boot project under migration/vaadin/.

Deterministic, app-agnostic, and SAFE: it only ever writes under <out> (which must live
inside a 'migration' directory) and never touches the existing JSF project. It:
  1. unzips the Vaadin starter into <out>,
  2. COPIES the framework-neutral service-layer packages (from the manifest's
     source.retainPackages) into <out>, preserving package paths (originals untouched),
  3. copies the i18n .properties bundles and Flyway migrations,
  4. patches application.properties (allowed-packages += basePackage, Postgres datasource,
     Flyway, Envers, ddl-auto=none, a non-conflicting server.port),
  5. removes the Hilla starter (Flow-only).

It does NOT perform the port (javax->jakarta, CDI->Spring, Deltaspike->Spring Data,
Shiro->Spring Security) — that is done per-feature in the [port] issues. So the copied
sources will not compile until those are done; that is expected mid-migration.

Usage:
  setup_vaadin_module.py --manifest migration/manifest.json --starter vaadin-starter.zip \
                         [--project-root .] [--out migration/vaadin] [--port 8090] [--force]

Nothing here is specific to web-budget: the base package, the packages to copy, and the
resource locations are all taken from the manifest / discovered from the source tree.
Pure stdlib.
"""
import argparse
import glob
import json
import os
import shutil
import sys
import zipfile


def die(msg):
    sys.stderr.write(f"error: {msg}\n")
    raise SystemExit(1)


def find_one_dir(path):
    entries = [e for e in os.listdir(path) if not e.startswith(".")]
    dirs = [e for e in entries if os.path.isdir(os.path.join(path, e))]
    return os.path.join(path, dirs[0]) if len(dirs) == 1 and not [
        e for e in entries if os.path.isfile(os.path.join(path, e))] else None


def copy_packages(src_java, out_java, packages):
    copied = 0
    for pkg in packages:
        src_dir = os.path.join(src_java, pkg.replace(".", os.sep))
        if not os.path.isdir(src_dir):
            continue
        dst_dir = os.path.join(out_java, pkg.replace(".", os.sep))
        os.makedirs(dst_dir, exist_ok=True)
        for f in os.listdir(src_dir):
            if f.endswith(".java"):
                shutil.copy2(os.path.join(src_dir, f), os.path.join(dst_dir, f))
                copied += 1
    return copied


def copy_globs(patterns, dst_dir):
    n = 0
    for pat in patterns:
        for f in glob.glob(pat):
            if os.path.isfile(f):
                os.makedirs(dst_dir, exist_ok=True)
                shutil.copy2(f, os.path.join(dst_dir, os.path.basename(f)))
                n += 1
    return n


def patch_application_properties(path, base_package, port):
    lines = []
    if os.path.isfile(path):
        with open(path) as fh:
            lines = fh.read().splitlines()
    out = []
    for ln in lines:
        if ln.startswith("vaadin.allowed-packages=") and base_package and base_package not in ln:
            ln = ln.rstrip() + "," + base_package
        if ln.startswith("spring.jpa.hibernate.ddl-auto="):
            ln = "spring.jpa.hibernate.ddl-auto=none"
        if ln.startswith("server.port="):
            continue  # replaced in the appended block
        out.append(ln)
    out += [
        "",
        "# --- migration: run alongside the JSF app, same database -------------------",
        f"server.port=${{PORT:{port}}}",
        "spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/webbudget}",
        "spring.datasource.username=${DB_USER:sa_webbudget}",
        "spring.datasource.password=${DB_PASSWORD:sa_webbudget}",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
        "# reuse the existing Flyway migrations (copied under db/migration)",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "# Hibernate Envers (auditing) — same settings as the JSF app",
        "spring.jpa.properties.org.hibernate.envers.store_data_at_delete=true",
    ]
    with open(path, "w") as fh:
        fh.write("\n".join(out) + "\n")


def patch_pom_remove_hilla(path):
    if not os.path.isfile(path):
        return
    with open(path) as fh:
        txt = fh.read()
    # remove the <dependency>...hilla-spring-boot-starter...</dependency> block
    import re
    txt = re.sub(
        r"\s*<dependency>\s*<groupId>com\.vaadin</groupId>\s*"
        r"<artifactId>hilla-spring-boot-starter</artifactId>\s*</dependency>",
        "", txt)
    with open(path, "w") as fh:
        fh.write(txt)


def main(argv):
    ap = argparse.ArgumentParser()
    ap.add_argument("--manifest", required=True)
    ap.add_argument("--starter", required=True)
    ap.add_argument("--project-root", default=".")
    ap.add_argument("--out", default=None)
    ap.add_argument("--port", default="8090")
    ap.add_argument("--force", action="store_true")
    args = ap.parse_args(argv[1:])

    root = os.path.abspath(args.project_root)
    out = os.path.abspath(args.out or os.path.join(root, "migration", "vaadin"))
    starter = os.path.abspath(args.starter)

    # --- SAFETY: never write outside migration/, never touch the JSF project ---
    if "migration" not in os.path.relpath(out, root).split(os.sep):
        die(f"refusing: --out must be inside a 'migration' directory (got {out})")
    jsf_src = os.path.join(root, "src")
    if os.path.commonpath([out, jsf_src]) == jsf_src:
        die("refusing: --out must not be inside the existing project's src/")
    if not os.path.isfile(starter):
        die(f"starter zip not found: {starter}\n"
            "  Use the curated vaadin-starter.zip committed at the repo root — do NOT download a\n"
            "  substitute. If it's missing, restore/commit it (it's tracked via a .gitignore negation).")

    manifest = json.load(open(args.manifest))
    src = manifest.get("source", {})
    base_package = src.get("basePackage")
    retain = src.get("retainPackages", [])
    if not retain:
        die("manifest has no source.retainPackages — run jsf-migration-assess first")

    if os.path.isdir(out) and os.listdir(out):
        if not args.force:
            die(f"{out} exists and is not empty; pass --force to overwrite the scaffold")
        shutil.rmtree(out)
    os.makedirs(out, exist_ok=True)

    # 1. unzip starter -> out (flatten the single top-level dir if present)
    tmp = out + ".tmp-unzip"
    if os.path.isdir(tmp):
        shutil.rmtree(tmp)
    os.makedirs(tmp)
    with zipfile.ZipFile(starter) as z:
        z.extractall(tmp)
    top = find_one_dir(tmp) or tmp
    for e in os.listdir(top):
        shutil.move(os.path.join(top, e), os.path.join(out, e))
    shutil.rmtree(tmp)
    print(f">>> unzipped starter -> {os.path.relpath(out, root)}")

    # 2. copy framework-neutral packages (COPY — originals untouched)
    src_java = os.path.join(root, "src", "main", "java")
    out_java = os.path.join(out, "src", "main", "java")
    n_java = copy_packages(src_java, out_java, retain)
    print(f">>> copied {n_java} .java files from {len(retain)} service-layer packages")

    # 3. copy i18n bundles + Flyway migrations
    src_res = os.path.join(root, "src", "main", "resources")
    out_res = os.path.join(out, "src", "main", "resources")
    n_i18n = copy_globs([os.path.join(src_res, "i18n", "*.properties"),
                         os.path.join(src_res, "*.properties")], os.path.join(out_res, "i18n"))
    # flyway migrations (support common locations)
    n_fly = 0
    for loc in ("db/migration", "db/migrations"):
        d = os.path.join(src_res, loc)
        if os.path.isdir(d):
            dst = os.path.join(out_res, "db", "migration")
            os.makedirs(dst, exist_ok=True)
            for f in glob.glob(os.path.join(d, "*")):
                if os.path.isfile(f):
                    shutil.copy2(f, os.path.join(dst, os.path.basename(f)))
                    n_fly += 1
    print(f">>> copied {n_i18n} i18n bundle(s), {n_fly} Flyway migration(s)")

    # 4. patch config
    patch_application_properties(os.path.join(out_res, "application.properties"), base_package, args.port)
    patch_pom_remove_hilla(os.path.join(out, "pom.xml"))
    print(f">>> patched application.properties (allowed-packages+={base_package}, port={args.port}, "
          f"Postgres/Flyway/Envers) and removed Hilla")

    print("\nNext: work the per-feature [port] issues — each rewrites its feature's entity/repo/service "
          "to jakarta + Spring Data + Spring DI (logic unchanged; see service-port-playbook.md). "
          "The module will not compile until those ports are done — expected mid-migration.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
