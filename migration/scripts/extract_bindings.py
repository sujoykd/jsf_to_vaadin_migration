#!/usr/bin/env python3
"""Extract a first-pass behavior spec from a JSF/PrimeFaces .xhtml view.

Heuristically pulls the structured facts a Vaadin Flow re-implementation needs, so
Claude refines rather than reverse-engineers from raw markup. Emits JSON on stdout.

What it extracts:
  title             <title> or first p:link/h:outputText that looks like a heading
  backingBeans      distinct EL bean names referenced (#{cardBean...} -> cardBean)
  inputs            form fields: tag, valueBinding, label/placeholder, required
  actions           command buttons/links: label, action EL, update/process targets
  columns           p:dataTable / p:column: header + value binding
  navigation        p:link/p:button outcomes and faces-redirect outcomes
  dialogs           p:dialog / referenced widgetVars and ui:include dialog files
  i18nKeys          message-bundle keys used (#{messages['x']}, #{menu['y']}, ...)
  elBindings        all distinct #{...} expressions (raw, for completeness)

Usage:
    extract_bindings.py <view.xhtml>

Pure stdlib, regex-based (facelets aren't always strict XML). Output is a STARTING
POINT — the jsf-view-analyze skill cross-checks it against the backing bean source.
"""
import json
import re
import sys
import os

EL_RE = re.compile(r"#\{([^}]+)\}")
BUNDLE_RE = re.compile(r"#\{(\w+)\[\s*'([^']+)'\s*\]\}")
TAG_RE = re.compile(r"<((?:p|h|f|pe|o):[\w]+)\b([^>]*?)/?>", re.IGNORECASE)
ATTR_RE = re.compile(r'([\w:-]+)\s*=\s*"([^"]*)"')

INPUT_TAGS = {
    "p:inputtext", "p:inputtextarea", "p:password", "p:inputnumber",
    "p:selectonemenu", "p:selectonebutton", "p:selectbooleancheckbox",
    "p:selectmanycheckbox", "p:selectonelistbox", "p:calendar", "p:datepicker",
    "p:autocomplete", "p:inputmask", "p:spinner", "p:togglebutton", "p:chips",
    "h:inputtext", "h:inputsecret", "h:selectonemenu",
}
COMMAND_TAGS = {"p:commandbutton", "p:commandlink", "h:commandbutton", "h:commandlink"}
LINK_TAGS = {"p:link", "p:button", "h:link", "h:button"}


def attrs(s):
    return {k.lower(): v for k, v in ATTR_RE.findall(s)}


def bean_of(el):
    el = el.strip()
    m = re.match(r"([a-zA-Z_]\w*)\b", el)
    return m.group(1) if m else None


def extract(src, view_name):
    """Parse JSF/PrimeFaces .xhtml source into a first-pass spec dict.

    Importable (`from extract_bindings import extract`); `main()` is the thin CLI
    wrapper around it.
    """
    spec = {
        "view": view_name,
        "title": None,
        "backingBeans": [],
        "inputs": [],
        "actions": [],
        "columns": [],
        "navigation": [],
        "dialogs": [],
        "i18nKeys": [],
        "elBindings": [],
    }

    tm = re.search(r"<title>\s*(.*?)\s*</title>", src, re.IGNORECASE | re.DOTALL)
    if tm:
        spec["title"] = tm.group(1).strip()

    beans, i18n, el_all = set(), set(), set()
    # i18n bundle keys
    for var, key in BUNDLE_RE.findall(src):
        i18n.add(f"{var}:{key}")
    # all EL + backing beans (exclude bundle vars and built-ins)
    builtins = {"facesContext", "component", "cc", "request", "session", "view",
                "null", "true", "false", "empty", "and", "or", "not"}
    for expr in EL_RE.findall(src):
        el_all.add(expr.strip())
        b = bean_of(expr)
        if b and b not in builtins and "[" not in expr.split(".")[0]:
            beans.add(b)

    # tag-level extraction
    for tag, rest in TAG_RE.findall(src):
        t = tag.lower()
        a = attrs(rest)
        if t in INPUT_TAGS:
            spec["inputs"].append({
                "tag": tag,
                "valueBinding": a.get("value"),
                "label": a.get("placeholder") or a.get("label") or a.get("title"),
                "required": a.get("required", "false").lower() == "true",
                "disabled": a.get("disabled"),
            })
        elif t in COMMAND_TAGS:
            spec["actions"].append({
                "tag": tag,
                "label": a.get("value") or a.get("title"),
                "action": a.get("action") or a.get("actionlistener"),
                "update": a.get("update"),
                "process": a.get("process"),
                "oncomplete": a.get("oncomplete"),
            })
        elif t in LINK_TAGS:
            if a.get("outcome"):
                spec["navigation"].append({"tag": tag, "outcome": a.get("outcome"),
                                            "label": a.get("value")})
        elif t == "p:column":
            spec["columns"].append({"header": a.get("headertext"),
                                     "value": None, "sortBy": a.get("sortby"),
                                     "filterBy": a.get("filterby")})
        elif t == "p:dialog":
            spec["dialogs"].append({"widgetVar": a.get("widgetvar"),
                                    "header": a.get("header"), "id": a.get("id")})

    # ui:include dialog fragments
    for inc in re.findall(r'<ui:include\s+src="([^"]+)"', src):
        if "dialog" in inc.lower():
            spec["dialogs"].append({"include": inc})

    # faces-redirect navigation outcomes in EL (e.g. returned by bean methods)
    for expr in el_all:
        if "faces-redirect" in expr:
            spec["navigation"].append({"outcome": expr})

    spec["backingBeans"] = sorted(beans)
    spec["i18nKeys"] = sorted(i18n)
    spec["elBindings"] = sorted(el_all)
    return spec


def main(argv):
    if len(argv) != 2:
        sys.stderr.write("usage: extract_bindings.py <view.xhtml>\n")
        return 2
    path = argv[1]
    try:
        with open(path, "r", encoding="utf-8", errors="replace") as fh:
            src = fh.read()
    except OSError as e:
        sys.stderr.write(f"error: {e}\n")
        return 1

    spec = extract(src, os.path.basename(path))
    json.dump(spec, sys.stdout, indent=2)
    sys.stdout.write("\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
