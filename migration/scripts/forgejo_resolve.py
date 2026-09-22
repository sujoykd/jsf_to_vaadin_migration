#!/usr/bin/env python3
"""Resolve-helper for Forgejo migration issues — works ONLY from the tracker.

Reads an issue and its native dependencies from Forgejo and reports whether it's ready to
work (all blocking issues closed); can also list ready issues of a kind, and close an issue.
It deliberately does NOT read anything under migration/ (no graph.json/state.json/specs) —
everything needed to solve an issue must live in the ticket itself. The `migration-resolve`
skill uses this to gate/close; the actual port/view work is driven from the issue BODY.

Modes (exactly one):
  --issue N                 print JSON: {number,title,state,kind,module,labels,body,
                                         dependencies:[{index,title,state}], ready, unmet}
  --ready KIND              list open issues whose derived kind is KIND and whose blockers are closed
  --close N [--comment ...] close issue N (optionally posting a comment first)

Config from env (populated by migration.config.json via _env, or your shell):
  FORGEJO_URL, FORGEJO_REPO (owner/repo), FORGEJO_TOKEN

Pure stdlib (urllib + json).
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


def api(method, path, body=None):
    url = API + path
    headers = {"Authorization": f"token {TOKEN}"}
    data = None
    if body is not None:
        data = json.dumps(body).encode()
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(req) as resp:
        raw = resp.read().decode()
        return json.loads(raw) if raw else {}


def kind_of(labels):
    names = [l.get("name", "") if isinstance(l, dict) else str(l) for l in labels]
    for name in names:                 # an explicit kind:<x> label always wins
        if name.startswith("kind:"):
            return name.split(":", 1)[1]
    for name in names:                 # fallbacks for issues without a kind: label
        if name == "epic":
            return "epic"
        if name == "service-port":     # legacy label once used for port issues
            return "port"
    return None


def module_of(labels):
    for l in labels:
        name = l.get("name", "") if isinstance(l, dict) else str(l)
        if name.startswith("module:"):
            return name.split(":", 1)[1]
    return None


def issue_record(index, include_body=True):
    iss = api("GET", f"/issues/{index}")
    labels = iss.get("labels", []) or []
    try:
        deps = api("GET", f"/issues/{index}/dependencies") or []
    except urllib.error.HTTPError:
        deps = []
    dep_list = [{"index": d.get("number"), "title": d.get("title"), "state": d.get("state")}
                for d in deps]
    unmet = [d for d in dep_list if d["state"] != "closed"]
    rec = {
        "number": iss.get("number"),
        "title": iss.get("title"),
        "state": iss.get("state"),
        "kind": kind_of(labels),
        "module": module_of(labels),
        "labels": [l.get("name") for l in labels],
        "dependencies": dep_list,
        "ready": len(unmet) == 0,
        "unmet": unmet,
    }
    if include_body:
        rec["body"] = iss.get("body", "")
    return rec


def main(argv):
    global API, TOKEN
    load_env()
    ap = argparse.ArgumentParser()
    g = ap.add_mutually_exclusive_group(required=True)
    g.add_argument("--issue", type=int, help="show one issue + readiness")
    g.add_argument("--ready", nargs="?", const="*", default=None, metavar="[KIND]",
                   help="list open issues that are ready (deps closed); optional KIND filters to "
                        "kind:KIND, otherwise lists all ready issues (epics excluded)")
    g.add_argument("--close", type=int, help="close an issue")
    ap.add_argument("--comment", default=None, help="comment to post before closing")
    args = ap.parse_args(argv[1:])

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

    if args.issue is not None:
        rec = issue_record(args.issue)
        print(json.dumps(rec, indent=2))
        return 0

    if args.ready is not None:
        kind = None if args.ready == "*" else args.ready
        # Fetch all open issues and filter by the *derived* kind (kind_of), not an API
        # `labels=kind:KIND` query — so issues whose kind label differs (e.g. ports created
        # with the legacy `service-port` label) are still matched.
        issues, page = [], 1
        try:
            while True:
                batch = api("GET", f"/issues?type=issues&state=open&limit=50&page={page}")
                if not isinstance(batch, list) or not batch:
                    break
                issues.extend(batch)
                if len(batch) < 50:
                    break
                page += 1
        except urllib.error.HTTPError as e:
            sys.stderr.write(f"error listing issues: HTTP {e.code}\n")
            return 1
        ready = []
        for iss in issues:
            rec = issue_record(iss["number"], include_body=False)
            if not rec["ready"]:
                continue
            if kind:
                if rec["kind"] != kind:
                    continue
            elif rec["kind"] == "epic":   # no-kind list excludes epics (close when children do)
                continue
            ready.append({"number": rec["number"], "kind": rec["kind"], "title": rec["title"]})
        ready.sort(key=lambda r: r["number"])
        print(json.dumps(ready, indent=2))
        # convenience: a ready-to-paste batch invocation (to stderr, so stdout stays pure JSON)
        if ready:
            ids = " ".join(str(r["number"]) for r in ready)
            sys.stderr.write(f">>> resolve these in order: /migration-resolve {ids}\n")
        return 0

    if args.close is not None:
        if args.comment:
            api("POST", f"/issues/{args.close}/comments", {"body": args.comment})
        api("PATCH", f"/issues/{args.close}", {"state": "closed"})
        print(f"closed #{args.close}")
        return 0

    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
