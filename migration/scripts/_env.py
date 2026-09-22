"""Shared helper: load migration/migration.config.json directly into os.environ.

So the migration scripts can be run without exporting/sourcing anything. The config is a
flat JSON object keyed by the same names the scripts read from the environment
(FORGEJO_TOKEN, BASE_URL, ...). The real environment always wins — values from the file are
only applied to keys not already set (unless override=True). Empty values and keys starting
with "_" (comments) are ignored.

Search order for the file (first existing):
  $MIGRATION_CONFIG, ./migration/migration.config.json, <this dir>/../migration.config.json
"""
import json
import os
import sys


def _candidates(path):
    if path:
        return [path]
    here = os.path.dirname(os.path.abspath(__file__))
    return [
        os.environ.get("MIGRATION_CONFIG", ""),
        os.path.join(os.getcwd(), "migration", "migration.config.json"),
        os.path.join(here, "..", "migration.config.json"),
    ]


def load_env(path=None, override=False):
    """Load migration.config.json into os.environ. Returns the parsed dict (or {})."""
    target = next((p for p in _candidates(path) if p and os.path.isfile(p)), None)
    if not target:
        return {}
    try:
        with open(target, encoding="utf-8") as fh:
            data = json.load(fh)
    except (OSError, ValueError) as e:
        sys.stderr.write(f"warning: could not read {target}: {e}\n")
        return {}
    if not isinstance(data, dict):
        return {}
    for key, val in data.items():
        if key.startswith("_") or val in (None, ""):
            continue
        if override or key not in os.environ:
            os.environ[key] = str(val)
    return data


if __name__ == "__main__":  # quick check: print what would be loaded
    print(json.dumps(load_env(), indent=2))
