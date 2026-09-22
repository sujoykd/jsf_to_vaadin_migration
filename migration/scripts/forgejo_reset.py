#!/usr/bin/env python3
"""Delete all issues (optionally labels/milestones) from the Forgejo repo — start fresh.

Why delete and not close: issue creation dedupes on the hidden `migration-id` UUID, and
that check ignores state — a *closed* issue still blocks its UUID from being recreated.
So to regenerate the backlog cleanly you must remove the issues outright.

DESTRUCTIVE and irreversible. Dry-run by default (lists what it would delete); pass
--apply to actually delete. Forgejo permanently deletes issues via DELETE /issues/{index}
(requires repo-admin/owner rights on the token).

  --apply            actually delete (default: just print the plan)
  --labels           also delete migration labels (migration, kind:*, module:*)
  --milestones       also delete milestones
  --all              issues + labels + milestones

Config from env (migration.config.json via _env, or your shell):
  FORGEJO_URL, FORGEJO_REPO (owner/repo), FORGEJO_TOKEN   (token needs write:issue / admin)

Pure stdlib.
"""
import argparse
import json
import os
import sys
import urllib.request
import urllib.error

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _env import load_env

API = ""
TOKEN = ""


def api(method, path):
    req = urllib.request.Request(API + path,
                                 headers={"Authorization": f"token {TOKEN}"},
                                 method=method)
    with urllib.request.urlopen(req) as resp:
        raw = resp.read().decode()
        return json.loads(raw) if raw else {}


def list_all(path):
    """Page through a Forgejo list endpoint."""
    out, page = [], 1
    while True:
        sep = "&" if "?" in path else "?"
        batch = api("GET", f"{path}{sep}limit=50&page={page}")
        if not isinstance(batch, list) or not batch:
            break
        out.extend(batch)
        if len(batch) < 50:
            break
        page += 1
    return out


def main(argv):
    global API, TOKEN
    load_env()
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true", help="actually delete (default: dry-run)")
    ap.add_argument("--labels", action="store_true", help="also delete migration labels")
    ap.add_argument("--milestones", action="store_true", help="also delete milestones")
    ap.add_argument("--all", action="store_true", help="issues + labels + milestones")
    args = ap.parse_args(argv[1:])
    do_labels = args.labels or args.all
    do_milestones = args.milestones or args.all

    url = os.environ.get("FORGEJO_URL")
    repo = (os.environ.get("FORGEJO_REPO") or "").strip().strip("/")
    TOKEN = os.environ.get("FORGEJO_TOKEN")
    missing = [k for k, v in [("FORGEJO_URL", url), ("FORGEJO_REPO", repo),
                              ("FORGEJO_TOKEN", TOKEN)] if not v]
    if missing:
        sys.stderr.write(f"error: set {', '.join(missing)} (migration.config.json or env)\n")
        return 2
    owner, _, name = repo.partition("/")
    if not owner or not name:
        sys.stderr.write(f"error: FORGEJO_REPO must be 'owner/repo' (got '{repo}')\n")
        return 2
    API = f"{url.rstrip('/')}/api/v1/repos/{owner}/{name}"
    mode = "DELETING" if args.apply else "DRY-RUN (nothing deleted)"
    print(f">>> {repo} — {mode}")

    issues = list_all("/issues?state=all&type=issues")
    print(f"issues: {len(issues)}")
    deleted = 0
    for iss in issues:
        n, title = iss.get("number"), iss.get("title", "")
        if not args.apply:
            print(f"  would delete #{n}  {title}")
            continue
        try:
            api("DELETE", f"/issues/{n}")
            deleted += 1
            print(f"  deleted #{n}  {title}")
        except urllib.error.HTTPError as e:
            sys.stderr.write(f"  FAILED #{n}: HTTP {e.code} {e.reason}\n")
            if e.code in (403, 404):
                sys.stderr.write("  (token may lack admin rights to delete issues)\n")

    if do_labels:
        labels = list_all("/labels")
        mig = [l for l in labels if l.get("name") == "migration"
               or str(l.get("name", "")).startswith(("kind:", "module:"))]
        print(f"migration labels: {len(mig)}")
        for l in mig:
            if not args.apply:
                print(f"  would delete label {l.get('name')}")
            else:
                api("DELETE", f"/labels/{l.get('id')}")
                print(f"  deleted label {l.get('name')}")

    if do_milestones:
        ms = list_all("/milestones?state=all")
        print(f"milestones: {len(ms)}")
        for m in ms:
            if not args.apply:
                print(f"  would delete milestone {m.get('title')}")
            else:
                api("DELETE", f"/milestones/{m.get('id')}")
                print(f"  deleted milestone {m.get('title')}")

    if args.apply:
        print(f"done — deleted {deleted} issue(s).")
    else:
        print("dry-run only; re-run with --apply to delete.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
