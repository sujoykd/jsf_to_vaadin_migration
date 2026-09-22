#!/usr/bin/env python3
"""Resolve every JSF view to its .xhtml path and backing-bean .java file(s).

Removes the manual "find the xhtml and its backing bean" step: scans the webapp for
navigable views, reads each view's EL to find its backing-bean names, and resolves each
bean name to a .java file in the source tree. Emits JSON on stdout — the worklist that
drives batch behavior-spec generation.

Output: [
  { "view": "secured/registration/wallet/formWallet.xhtml",
    "slug": "secured__registration__wallet__formWallet",
    "beans": [ {"name": "walletBean", "file": "src/main/java/.../WalletBean.java"} ],
    "unresolved": [] }
, ... ]

Usage:
    resolve_views.py <webapp-root> [java-root]

java-root defaults to src/main/java (relative to CWD). Pure stdlib, heuristic.
"""
import json
import os
import re
import sys

EL_RE = re.compile(r"#\{([^}]+)\}")
BUILTINS = {"facesContext", "component", "cc", "request", "session", "view",
            "null", "true", "false", "empty", "and", "or", "not", "messages",
            "menu", "enums", "breadcrumb", "permission", "validation"}

FULL_PAGE_RE = re.compile(r"<\s*(html|f:view|h:body)\b", re.I)
FRAGMENT_RE = re.compile(r"<\s*(ui:composition|ui:component|ui:fragment)\b", re.I)
TEMPLATE_CLIENT_RE = re.compile(r"\btemplate\s*=", re.I)

NAMED_VALUE_RE = re.compile(r'@Named\s*\(\s*"([^"]+)"\s*\)')
NAMED_BARE_RE = re.compile(r'@Named\b(?!\s*\()')
# Anchor to a real declaration line (line start + optional modifiers) so prose like
# "This class holds ..." in a javadoc comment isn't mistaken for the class name.
CLASS_DECL_RE = re.compile(
    r'^\s*(?:public\s+|final\s+|abstract\s+|sealed\s+|non-sealed\s+)*class\s+(\w+)',
    re.MULTILINE)


def read(p):
    try:
        with open(p, "r", encoding="utf-8", errors="replace") as fh:
            return fh.read()
    except OSError:
        return ""


def is_view(path):
    name = os.path.basename(path)
    if not name.endswith(".xhtml") or name.lower().startswith(("dialog", "sidebar")):
        return False
    if os.sep + "template" + os.sep in path:
        return False
    head = read(path)[:4096]
    if FULL_PAGE_RE.search(head):
        return True
    if FRAGMENT_RE.search(head):
        return bool(TEMPLATE_CLIENT_RE.search(head))
    return False


def bean_names(xhtml_text):
    names = set()
    for expr in EL_RE.findall(xhtml_text):
        expr = expr.strip()
        m = re.match(r"([a-zA-Z_]\w*)", expr)
        if not m:
            continue
        n = m.group(1)
        # skip bundle access like messages['x'] and EL keywords/builtins
        if n in BUILTINS or "[" in expr.split(".")[0]:
            continue
        names.add(n)
    return names


def index_beans(java_root):
    """Map CDI bean name -> java file. Bean name = @Named("x") value, else decapitalized class name."""
    idx = {}
    for dirpath, _d, files in os.walk(java_root):
        if os.sep + "target" in dirpath:
            continue
        for f in files:
            if not f.endswith(".java"):
                continue
            path = os.path.join(dirpath, f)
            txt = read(path)
            if "@Named" not in txt:
                continue
            cls = CLASS_DECL_RE.search(txt)
            class_name = cls.group(1) if cls else f[:-len(".java")]
            explicit = NAMED_VALUE_RE.search(txt)
            if explicit:
                idx.setdefault(explicit.group(1), path)
            elif NAMED_BARE_RE.search(txt):
                # bare @Named -> decapitalized simple class name
                bean = class_name[0].lower() + class_name[1:] if class_name else None
                if bean:
                    idx.setdefault(bean, path)
            # also index by decapitalized class name as a fallback
            fallback = class_name[0].lower() + class_name[1:]
            idx.setdefault("__class__" + fallback, path)
    return idx


sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _env import load_env


def main(argv):
    load_env()  # read migration/migration.config.json directly (real env still wins)
    webapp = argv[1] if len(argv) > 1 else os.environ.get("WEBAPP")
    if not webapp:
        sys.stderr.write("usage: resolve_views.py [<webapp-root> [java-root]]\n"
                         "  (or set WEBAPP in migration/migration.config.json)\n")
        return 2
    webapp = os.path.abspath(webapp)
    java_arg = argv[2] if len(argv) > 2 else os.environ.get("JAVA_ROOT", "src/main/java")
    java_root = os.path.abspath(java_arg)
    if not os.path.isdir(webapp):
        sys.stderr.write(f"error: webapp root not found: {webapp}\n")
        return 1

    idx = index_beans(java_root) if os.path.isdir(java_root) else {}

    out = []
    for dirpath, _d, files in os.walk(webapp):
        if os.sep + "target" in dirpath:
            continue
        for f in sorted(files):
            path = os.path.join(dirpath, f)
            if not is_view(path):
                continue
            rel = os.path.relpath(path, webapp).replace(os.sep, "/")
            txt = read(path)
            beans, unresolved = [], []
            for name in sorted(bean_names(txt)):
                jf = idx.get(name) or idx.get("__class__" + name)
                if jf:
                    beans.append({"name": name,
                                  "file": os.path.relpath(jf, os.getcwd())})
                else:
                    unresolved.append(name)
            out.append({"view": rel,
                        "slug": rel[:-len(".xhtml")].replace("/", "__"),
                        "beans": beans,
                        "unresolved": unresolved})

    out.sort(key=lambda v: v["view"])
    json.dump(out, sys.stdout, indent=2)
    sys.stdout.write("\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
