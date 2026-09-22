#!/usr/bin/env python3
"""Generic issue-creation entry point — dispatches to the tracker chosen by $TRACKER.

  TRACKER = forgejo | vikunja | openproject   (default: forgejo)

All CLI args are passed through unchanged to the selected adapter
(forgejo_issues.py / vikunja_issues.py / openproject_issues.py), which read their own
*_URL / *_TOKEN / *_PROJECT(_REPO) values from migration.config.json (via _env).

Usage (same flags as the adapters):
  python3 migration/scripts/create_issues.py --graph migration/issues/graph.json \
      --drafts migration/issues/drafts --screenshots screenshots [--apply]

Pure stdlib.
"""
import importlib
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _env import load_env

ADAPTERS = {
    "forgejo": "forgejo_issues",
    "vikunja": "vikunja_issues",
    "openproject": "openproject_issues",
}


def main(argv):
    load_env()  # so TRACKER (and tracker creds) can come from migration.config.json
    tracker = (os.environ.get("TRACKER") or "forgejo").strip().lower()
    module = ADAPTERS.get(tracker)
    if not module:
        sys.stderr.write(
            f"error: TRACKER must be one of {sorted(ADAPTERS)} (got '{tracker}')\n")
        return 2
    sys.stderr.write(f">>> tracker: {tracker} ({module}.py)\n")
    adapter = importlib.import_module(module)
    return adapter.main(argv)


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
