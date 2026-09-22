#!/usr/bin/env python3
"""Gate for view migration: verify a view's service layer has been ported to Spring.

A view must not be implemented until the services it calls (from its behavior spec's
`services` block) exist in the Vaadin module AND have been converted from CDI/Deltaspike
to Spring. This checks that, structurally, and exits non-zero if the port isn't done — so
`jsf-view-to-vaadin` stops and points at the `[port] <feature>` issue instead of building
a UI against a backend that isn't ready.

Checks, per referenced service/repository type:
  - the .java exists under <vaadin-root>/src/main/java
  - it carries a Spring marker (@Service / @Component / extends JpaRepository / Spring @Repository)
  - it carries NO leftover CDI/Java-EE markers (@ApplicationScoped, @Stateless, javax.inject,
    javax.enterprise, deltaspike, FacesContext)

Usage:
  check_service_port.py --spec migration/specs/<view>.json --vaadin-root migration/vaadin

Exit 0 = ported (safe to implement the view); 2 = not ported / missing; 1 = bad input.
App-agnostic. Pure stdlib.
"""
import argparse
import glob
import json
import os
import sys

SPRING_MARKERS = ("@Service", "@Component", "JpaRepository",
                  "org.springframework.stereotype.Repository", "@org.springframework")
CDI_MARKERS = ("@ApplicationScoped", "@Stateless", "@Singleton", "javax.inject",
               "jakarta.inject.Inject", "javax.enterprise", "deltaspike", "FacesContext",
               "EntityRepository")  # Deltaspike base


def find_class(java_root, type_name):
    simple = type_name.split(".")[-1]
    hits = glob.glob(os.path.join(java_root, "**", simple + ".java"), recursive=True)
    return hits[0] if hits else None


def main(argv):
    ap = argparse.ArgumentParser()
    ap.add_argument("--spec", required=True)
    ap.add_argument("--vaadin-root", default="migration/vaadin")
    args = ap.parse_args(argv[1:])

    try:
        spec = json.load(open(args.spec))
    except (OSError, ValueError) as e:
        sys.stderr.write(f"error reading --spec: {e}\n")
        return 1

    java_root = os.path.join(args.vaadin_root, "src", "main", "java")
    if not os.path.isdir(java_root):
        sys.stderr.write(f"not ported: {java_root} does not exist — run vaadin-scaffold first\n")
        return 2

    services = spec.get("services") or []
    types = []
    for s in services:
        t = s.get("type")
        if t:
            types.append(t)
    if not types:
        print("WARN: spec lists no services — cannot verify the port automatically.")
        print("      Confirm manually that this view's backend is ported before implementing.")
        return 0

    ok, problems = [], []
    for t in types:
        path = find_class(java_root, t)
        if not path:
            problems.append(f"{t}: NOT FOUND in {java_root}")
            continue
        txt = open(path, encoding="utf-8", errors="replace").read()
        has_spring = any(m in txt for m in SPRING_MARKERS)
        leftover = [m for m in CDI_MARKERS if m in txt]
        if not has_spring:
            problems.append(f"{t}: found but no Spring annotation (@Service/JpaRepository) — not ported")
        elif leftover:
            problems.append(f"{t}: still has CDI/Java-EE markers {leftover} — port incomplete")
        else:
            ok.append(t)

    for t in ok:
        print(f"  ✓ {t}: ported")
    for p in problems:
        print(f"  ✗ {p}")

    if problems:
        feat = spec.get("view", "?")
        sys.stderr.write(
            f"\nnot ported: {len(problems)} service(s) for {feat} are not Spring-ported yet.\n"
            f"Finish the [port] issue for this feature before migrating the view.\n")
        return 2
    print(f"\nOK: all {len(ok)} service(s) ported — safe to implement the view.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
