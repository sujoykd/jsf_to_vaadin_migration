#!/usr/bin/env python3
"""Statically enumerate navigable JSF view URLs from a webapp source tree.

Globs **/*.xhtml under a webapp root, drops fragments/templates/dialogs, maps each
remaining file to its browser URL, and classifies it. Emits a JSON array of
{url, file, kind, needs_id} objects on stdout.

Usage:
    enumerate_views.py <webapp-root> <base-url>

Example:
    enumerate_views.py src/main/webapp https://localhost:8443
    -> [{"url": "https://localhost:8443/secured/dashboard.xhtml",
         "file": "secured/dashboard.xhtml", "kind": "dashboard", "needs_id": false}, ...]

Generic: works on any JSF 2 / PrimeFaces webapp, not just web-budget. Pure stdlib.
"""
import json
import os
import re
import sys

# Files whose relative path starts with one of these segments are layout/fragments,
# never standalone navigable views.
EXCLUDE_DIR_SEGMENTS = ("template/", "templates/", "WEB-INF/", "META-INF/", "resources/")

# Filename patterns that indicate non-navigable fragments included via <ui:include>.
EXCLUDE_NAME_RE = re.compile(r"(^dialog|^sidebar)", re.IGNORECASE)

# A standalone page is either a plain HTML/JSF page, or a template *client*:
# <ui:composition template="..."> composes a full page via the named template.
# A fragment is a <ui:composition>/<ui:component>/<ui:fragment> WITHOUT a template,
# meant to be pulled in via <ui:include> (e.g. dialogs, sidebars).
FRAGMENT_ROOT_RE = re.compile(r"<\s*(ui:composition|ui:component|ui:fragment)\b", re.IGNORECASE)
FULL_PAGE_RE = re.compile(r"<\s*(html|f:view|h:body)\b", re.IGNORECASE)
TEMPLATE_CLIENT_RE = re.compile(r"\btemplate\s*=", re.IGNORECASE)

# Views that need a record id (?id=...) to render meaningfully.
NEEDS_ID_RE = re.compile(r"(^detail|statistics$|^balance|^form.*)", re.IGNORECASE)


def classify(rel_path: str, name_stem: str) -> tuple[str, bool]:
    """Return (kind, needs_id) for a view given its relative path and filename stem."""
    lower = name_stem.lower()
    if rel_path == "index.xhtml":
        return "public", False
    if rel_path.startswith("error/"):
        return "error", False
    if lower == "dashboard":
        return "dashboard", False
    if lower.startswith("list"):
        return "list", False
    if lower.endswith("statistics") or lower.startswith("detail") or lower.startswith("balance"):
        return "detail-needs-id", True
    if lower.startswith("form"):
        # form pages render in "new" mode without an id, and "edit" mode with one.
        return "form-new", False
    return "other", False


def is_fragment(path: str) -> bool:
    """Heuristic: a file is a fragment if it is a ui:composition/component WITHOUT a
    template (so it's meant to be <ui:include>d), and isn't a plain HTML/JSF page.
    A <ui:composition template="..."> is a template client = a real navigable page.
    Reads only the head of the file."""
    try:
        with open(path, "r", encoding="utf-8", errors="replace") as fh:
            head = fh.read(4096)
    except OSError:
        return True
    if FULL_PAGE_RE.search(head):
        return False
    if FRAGMENT_ROOT_RE.search(head):
        # template client -> page; bare composition/component -> fragment
        return not TEMPLATE_CLIENT_RE.search(head)
    return False


sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _env import load_env


def main(argv: list[str]) -> int:
    load_env()  # read migration/migration.config.json directly (real env still wins)
    webapp = argv[1] if len(argv) > 1 else os.environ.get("WEBAPP")
    base = argv[2] if len(argv) > 2 else os.environ.get("BASE_URL")
    if not webapp or not base:
        sys.stderr.write("usage: enumerate_views.py [<webapp-root> <base-url>]\n"
                         "  (or set WEBAPP and BASE_URL in migration/migration.config.json)\n")
        return 2
    webapp_root = os.path.abspath(webapp)
    base_url = base.rstrip("/")

    if not os.path.isdir(webapp_root):
        sys.stderr.write(f"error: webapp root not found: {webapp_root}\n")
        return 1

    views = []
    for dirpath, _dirs, files in os.walk(webapp_root):
        for fname in files:
            if not fname.endswith(".xhtml"):
                continue
            abspath = os.path.join(dirpath, fname)
            rel = os.path.relpath(abspath, webapp_root).replace(os.sep, "/")

            if any(rel.startswith(seg) for seg in EXCLUDE_DIR_SEGMENTS):
                continue
            if EXCLUDE_NAME_RE.search(fname):
                continue
            if is_fragment(abspath):
                continue

            stem = fname[: -len(".xhtml")]
            kind, needs_id = classify(rel, stem)
            views.append(
                {
                    "url": f"{base_url}/{rel}",
                    "file": rel,
                    "kind": kind,
                    "needs_id": needs_id,
                }
            )

    views.sort(key=lambda v: v["file"])
    json.dump(views, sys.stdout, indent=2)
    sys.stdout.write("\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
