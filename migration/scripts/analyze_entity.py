#!/usr/bin/env python3
"""Extract per-field validation/type facts from a JPA entity (.java) — deterministically.

Replaces the manual "read the entity and copy its Bean Validation annotations into the
spec" step of jsf-view-analyze. Those constraints (@NotBlank, @Size(max=…), @NotNull,
nullable/length on @Column) drive the required-flags and validators of the Vaadin form,
so reading them with a parser keeps the spec faithful and repeatable.

For each persistent field it reports:
  name, type, validations:[{annotation, attrs}], required (bool), maxLength (int|null),
  column:{name,nullable,length}, relation ("@ManyToOne"/… or null), enum (bool)

If a field's type is an enum found under <java-root>, its constants are resolved into
`options`. Superclass fields are not followed (pass the parent too if you need them).

Usage:
  analyze_entity.py <Entity.java> [<java-source-root>]
Pure stdlib. Importable: `from analyze_entity import analyze`.
"""
import json
import os
import re
import sys

# a field = run of annotations then `private/protected Type name;`
FIELD_RE = re.compile(
    r"((?:@\w+(?:\([^)]*\))?\s*)*)"          # leading annotations (group 1)
    r"(?:private|protected|public)\s+"
    r"([\w.]+(?:<[^>]+>)?)\s+(\w+)\s*[;=]")  # type (2), name (3)
ANNOT_RE = re.compile(r"@(\w+)(?:\(([^)]*)\))?")
VALIDATION_ANNOS = {"NotBlank", "NotNull", "NotEmpty", "Size", "Min", "Max",
                    "Email", "Past", "Future", "Positive", "PositiveOrZero",
                    "Negative", "DecimalMin", "DecimalMax", "Digits", "Pattern"}
RELATION_ANNOS = {"ManyToOne", "OneToMany", "ManyToMany", "OneToOne"}
SKIP_TYPES = {"Logger", "long", "serialVersionUID"}
COMMENT_RE = re.compile(r"/\*.*?\*/|//[^\n]*", re.DOTALL)


def _strip_comments(src):
    return COMMENT_RE.sub("", src)


def _attr(attrs, key):
    m = re.search(rf"\b{key}\s*=\s*([^,]+)", attrs)
    return m.group(1).strip() if m else None


def _resolve_enum(type_name, java_root):
    if not java_root:
        return None
    base = type_name.split("<")[0].split(".")[-1]
    for dirpath, _, files in os.walk(java_root):
        if f"{base}.java" in files:
            with open(os.path.join(dirpath, f"{base}.java"),
                      encoding="utf-8", errors="replace") as fh:
                txt = _strip_comments(fh.read())
            if not re.search(rf"\benum\s+{base}\b", txt):
                return None
            body = txt[txt.index("{") + 1:]
            # constant list ends at the first ';' (enums with members) or the final '}'
            seg = body.split(";", 1)[0] if ";" in body else body.rsplit("}", 1)[0]
            seg = re.sub(r"\([^)]*\)", "", seg)        # drop constructor args
            seg = re.sub(r"@\w+", "", seg)              # drop annotations on constants
            return re.findall(r"\b([A-Z][A-Z0-9_]+)\b", seg)
    return None


def analyze(path, java_root=None):
    with open(path, "r", encoding="utf-8", errors="replace") as fh:
        src = _strip_comments(fh.read())
    # start at the class declaration so the import block isn't mistaken for fields
    body = src[src.index("class "):] if "class " in src else src

    fields = []
    for annos_blob, ftype, fname in FIELD_RE.findall(body):
        if ftype in SKIP_TYPES or fname == "serialVersionUID":
            continue
        annos = ANNOT_RE.findall(annos_blob)
        validations, relation, column = [], None, None
        for aname, aargs in annos:
            if aname in VALIDATION_ANNOS:
                validations.append({"annotation": aname, "attrs": aargs or ""})
            elif aname in RELATION_ANNOS:
                relation = aname
            elif aname == "Column":
                column = {
                    "name": (_attr(aargs, "name") or "").strip('"') or None,
                    "nullable": _attr(aargs, "nullable"),
                    "length": _attr(aargs, "length"),
                }
        required = any(v["annotation"] in ("NotBlank", "NotNull", "NotEmpty")
                       for v in validations) or (column and column.get("nullable") == "false")
        max_len = None
        for v in validations:
            if v["annotation"] == "Size":
                max_len = _attr(v["attrs"], "max")
        if max_len is None and column and column.get("length"):
            max_len = column["length"]
        enum_opts = _resolve_enum(ftype, java_root) if ftype[:1].isupper() else None
        fields.append({
            "name": fname,
            "type": ftype,
            "validations": validations,
            "required": bool(required),
            "maxLength": int(max_len) if (max_len and str(max_len).isdigit()) else None,
            "column": column,
            "relation": relation,
            "enum": enum_opts is not None,
            "options": enum_opts,
        })
    return {"entity": os.path.basename(path), "fields": fields}


def main(argv):
    if len(argv) < 2:
        sys.stderr.write("usage: analyze_entity.py <Entity.java> [<java-source-root>]\n")
        return 2
    try:
        result = analyze(argv[1], argv[2] if len(argv) > 2 else None)
    except OSError as e:
        sys.stderr.write(f"error: {e}\n")
        return 1
    json.dump(result, sys.stdout, indent=2)
    sys.stdout.write("\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
