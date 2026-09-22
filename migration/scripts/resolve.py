#!/usr/bin/env python3
"""Generic issue-resolve entry point — dispatches to the tracker chosen by $TRACKER.

  TRACKER = forgejo | vikunja | openproject   (default: forgejo)

Only the Forgejo resolver is implemented today; the others report "not implemented" until
we need them. All CLI args pass through unchanged to the selected backend
(e.g. forgejo_resolve.py), which reads its creds from migration.config.json (via _env).

Usage (same flags as the backend):
  python3 migration/scripts/resolve.py --issue 21
  python3 migration/scripts/resolve.py --ready port
  python3 migration/scripts/resolve.py --close 21 --comment "done"

Pure stdlib.
"""
import importlib
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _env import load_env

# tracker -> backend module (add entries here as resolvers are implemented)
ADAPTERS = {
    "forgejo": "forgejo_resolve",
}
KNOWN_TRACKERS = {"forgejo", "vikunja", "openproject"}


def main(argv):
    load_env()  # so TRACKER (and tracker creds) can come from migration.config.json
    tracker = (os.environ.get("TRACKER") or "forgejo").strip().lower()
    module = ADAPTERS.get(tracker)
    if not module:
        if tracker in KNOWN_TRACKERS:
            sys.stderr.write(
                f"error: resolve is not implemented for TRACKER={tracker} yet (only "
                f"{sorted(ADAPTERS)}). Implement a {tracker}_resolve.py and register it here when needed.\n")
        else:
            sys.stderr.write(
                f"error: TRACKER must be one of {sorted(KNOWN_TRACKERS)} (got '{tracker}')\n")
        return 2
    sys.stderr.write(f">>> tracker: {tracker} ({module}.py)\n")
    adapter = importlib.import_module(module)
    return adapter.main(argv)


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
