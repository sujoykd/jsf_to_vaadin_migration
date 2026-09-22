#!/usr/bin/env python3
"""Find navigable views with no baseline screenshot, and print the commands to fix them.

Only navigable views (those in the inventory `migration/views.json`) can be screenshotted;
non-navigable composites/fragments aren't in that list, so they're never flagged. For each
inventory view it checks whether `screenshots/*/<slug>*.png` exists (the `*` covers the
`__id<n>` suffix on id-bearing views), then emits ready-to-paste invocations:

  1. one /jsf-screenshot-tour call (targeted mode) for the missing view URLs;
  2. one /jsf-view-analyze call per missing view to relink the baseline.

Usage:
  check_screenshots.py [--views migration/views.json] [--screenshots screenshots] [--json]
Exit code: 0 if nothing missing, 1 if some are missing (so it's CI-friendly).
Pure stdlib.
"""
import argparse
import glob
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import _slug


def main(argv):
    ap = argparse.ArgumentParser()
    ap.add_argument("--views", default="migration/views.json")
    ap.add_argument("--screenshots", default="screenshots")
    ap.add_argument("--json", action="store_true", help="emit the missing list as JSON")
    args = ap.parse_args(argv[1:])

    try:
        views = json.load(open(args.views, encoding="utf-8"))
    except OSError as e:
        sys.stderr.write(f"error: {e}\n")
        return 2

    missing = []
    for v in views:
        url = v["url"] if isinstance(v, dict) else v
        file = v.get("file") if isinstance(v, dict) else None
        slug = _slug.slug(url)
        if not glob.glob(os.path.join(args.screenshots, "*", f"{slug}*.png")):
            missing.append({"slug": slug, "url": url, "file": file or url})

    if args.json:
        json.dump(missing, sys.stdout, indent=2)
        sys.stdout.write("\n")
        return 0 if not missing else 1

    total = len(views)
    if not missing:
        print(f"All {total} navigable views have a baseline screenshot — nothing to do.")
        return 0

    print(f"{len(missing)} of {total} navigable views are missing a baseline:")
    for m in missing:
        print(f"  {m['slug']}")
    print()
    print("# 1) Capture the missing baselines (source app must be running, creds in config):")
    print("/jsf-screenshot-tour " + " ".join(m["url"] for m in missing))
    print()
    print("# 2) Re-analyze them to relink baselines (one invocation, all views):")
    print("/jsf-view-analyze " + " ".join(m["file"] for m in missing))
    return 1


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
