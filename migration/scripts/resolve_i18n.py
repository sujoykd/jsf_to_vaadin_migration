#!/usr/bin/env python3
"""Resolve JSF message-bundle keys to their text — deterministically.

extract_bindings.py emits i18n references as `var:key` (e.g. `messages:wallet.form.name`,
where `var` is the EL bundle variable). This maps each to the human label from the
matching `.properties` bundle, so the spec (and the Vaadin view) carries real labels
instead of opaque keys — and the keys stay, so the Vaadin app can reuse the same bundles.

Bundle lookup: a var `messages` is served by files like `messages_en_US.properties` /
`messages.properties` under the i18n dir. Preferred locale first (default en_US), then
the base bundle, then any other locale (so nothing is silently dropped).

Usage:
  resolve_i18n.py <i18n-dir> <var:key> [<var:key> ...]   # explicit keys
  resolve_i18n.py <i18n-dir> --stdin                       # newline-delimited keys on stdin
  resolve_i18n.py <i18n-dir> --keys-json <spec.json>       # read .i18nKeys[] from a spec
Options: --locale en_US (default)
Output: JSON {"var:key": {"text": "...", "key": "...", "bundle": "messages", "locale": "en_US"}}
Pure stdlib. Importable: `from resolve_i18n import resolve`.
"""
import json
import os
import re
import sys


def _load_bundle(path):
    props = {}
    with open(path, "r", encoding="utf-8", errors="replace") as fh:
        for raw in fh:
            line = raw.strip()
            if not line or line.startswith(("#", "!")):
                continue
            if "=" not in line:
                continue
            k, _, v = line.partition("=")
            props[k.strip()] = v.strip()
    return props


def _bundles_for(i18n_dir, var, locale):
    """Candidate bundle files for an EL var, in resolution priority."""
    files = os.listdir(i18n_dir) if os.path.isdir(i18n_dir) else []
    ordered = []
    for suffix in (f"_{locale}.properties", ".properties"):
        name = f"{var}{suffix}"
        if name in files:
            ordered.append(name)
    # any other locale of the same bundle, last
    for f in sorted(files):
        if f.startswith(f"{var}_") and f.endswith(".properties") and f not in ordered:
            ordered.append(f)
    return ordered


def resolve(i18n_dir, keys, locale="en_US"):
    cache = {}
    out = {}
    for ref in keys:
        var, _, key = ref.partition(":")
        if not key:                       # bare key with no bundle var
            var, key = "messages", ref
        text, used_locale = None, None
        for fname in _bundles_for(i18n_dir, var, locale):
            if fname not in cache:
                cache[fname] = _load_bundle(os.path.join(i18n_dir, fname))
            if key in cache[fname]:
                text = cache[fname][key]
                m = re.search(r"_(\w+)\.properties$", fname)
                used_locale = m.group(1) if m else None
                break
        out[ref] = {"text": text, "key": key, "bundle": var, "locale": used_locale}
    return out


def main(argv):
    args = argv[1:]
    locale = "en_US"
    if "--locale" in args:
        i = args.index("--locale")
        locale = args[i + 1]
        del args[i:i + 2]
    if len(args) < 2:
        sys.stderr.write("usage: resolve_i18n.py <i18n-dir> <var:key>...| --stdin | "
                         "--keys-json <spec.json>\n")
        return 2
    i18n_dir, rest = args[0], args[1:]
    if rest[0] == "--stdin":
        keys = [l.strip() for l in sys.stdin if l.strip()]
    elif rest[0] == "--keys-json":
        with open(rest[1], encoding="utf-8") as fh:
            keys = json.load(fh).get("i18nKeys", [])
    else:
        keys = rest
    json.dump(resolve(i18n_dir, keys, locale), sys.stdout, indent=2, ensure_ascii=False)
    sys.stdout.write("\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
