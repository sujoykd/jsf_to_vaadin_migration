#!/usr/bin/env python3
"""Forgejo/Gitea issue adapter for the JSF→Vaadin migration.

Consumes a graph.json (from build_issue_graph.py) + the drafts/ dir, and creates
labels, milestones, issues (in topological order), uploads screenshot assets, wires
native issue dependencies, and fills epic task-lists. Idempotent via the tracker itself:
each issue carries a deterministic migration-id UUID and re-runs skip a UUID that already
exists on the tracker — so deleting issues and re-running recreates them cleanly. The
`--state` file is only a written record of key→number; it is NOT read for dedupe.

DEFAULT = dry-run: prints the planned API calls, makes NO network requests.
Use --apply to actually call the Forgejo API.

Required env when --apply:
  FORGEJO_URL    e.g. http://10.88.14.29:3000
  FORGEJO_TOKEN  access token (scopes: write:issue, write:repository)
  FORGEJO_REPO   owner/repo  e.g. binarycodes/web-budget-vaadin

Usage:
  forgejo_issues.py --graph migration/issues/graph.json --drafts migration/issues/drafts \
                    [--screenshots screenshots] [--state migration/issues/state.json] [--apply]

Pure stdlib (urllib + json). Portable across macOS/Linux — no jq, no bash version issues.

Gitea/Forgejo API used (see references/tracker-adapters.md):
  POST   /api/v1/repos/{repo}/labels            create label
  GET    /api/v1/repos/{repo}/labels            list labels
  POST   /api/v1/repos/{repo}/milestones        create milestone
  GET    /api/v1/repos/{repo}/milestones        list milestones
  POST   /api/v1/repos/{repo}/issues            create issue {title,body,labels[],milestone}
  PATCH  /api/v1/repos/{repo}/issues/{index}    edit issue body
  POST   /api/v1/repos/{repo}/issues/{index}/assets       multipart screenshot upload
  POST   /api/v1/repos/{repo}/issues/{index}/dependencies {index, owner, repo}  (IssueMeta)
"""
import argparse
import glob
import json
import os
import re
import sys
import urllib.parse
import urllib.request
import urllib.error

DRY = True
API = ""

# Forgejo returns asset URLs using its configured ROOT_URL (often http://localhost:3000),
# which won't load when the issue is viewed from a different host. Embedding the asset as a
# host-RELATIVE path (/attachments/<uuid>) makes it resolve against whatever host the user
# is on, regardless of ROOT_URL.
ATTACH_ABS_RE = re.compile(r"https?://[^/)\s]+(/attachments/)")
CAPTION = "_(adapter replaces this with the uploaded asset URL)_\n"


def rel_attachment(url):
    """Return the host-relative path of a Forgejo asset URL (/attachments/<uuid>)."""
    path = urllib.parse.urlsplit(url or "").path
    return path or (url or "")
TOKEN = ""


def log(msg):
    print(msg, flush=True)


def req(method, path, body=None, multipart=None, quiet=False):
    """HTTP call (apply mode) or print (dry-run). Returns parsed JSON (or {} in dry-run).
    quiet=True suppresses the stderr error dump (for expected/tolerated failures)."""
    if DRY:
        extra = ""
        if body is not None:
            extra = "  <- " + json.dumps(body)
        elif multipart:
            extra = f"  <- file {multipart[1]}"
        log(f"    [dry-run] {method} {API}{path}{extra}")
        return {"number": 0, "id": 0, "browser_download_url": "<asset-url>"}
    url = API + path
    headers = {"Authorization": f"token {TOKEN}"}
    if multipart is not None:
        field, filepath = multipart
        data, ctype = _encode_multipart(field, filepath)
        headers["Content-Type"] = ctype
    elif body is not None:
        data = json.dumps(body).encode()
        headers["Content-Type"] = "application/json"
    else:
        data = None
    r = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(r) as resp:
            raw = resp.read().decode()
            return json.loads(raw) if raw else {}
    except urllib.error.HTTPError as e:
        if not quiet:
            sys.stderr.write(f"HTTP {e.code} on {method} {path}: {e.read().decode()[:300]}\n")
        raise


def _encode_multipart(field, filepath):
    boundary = "----jsfvaadinboundary7e3f"
    with open(filepath, "rb") as fh:
        content = fh.read()
    fname = os.path.basename(filepath)
    pre = (f"--{boundary}\r\n"
           f'Content-Disposition: form-data; name="{field}"; filename="{fname}"\r\n'
           f"Content-Type: image/png\r\n\r\n").encode()
    post = f"\r\n--{boundary}--\r\n".encode()
    return pre + content + post, f"multipart/form-data; boundary={boundary}"


def draft_body(drafts_dir, slug):
    matches = sorted(glob.glob(os.path.join(drafts_dir, f"*-{slug}.md")))
    if matches:
        with open(matches[0]) as fh:
            return fh.read()
    return f"(no draft for {slug})"


def find_screenshot(screenshots_dir, slug):
    matches = sorted(glob.glob(os.path.join(screenshots_dir, "*", f"{slug}.png")))
    return matches[0] if matches else None


MIGRATION_ID_RE = re.compile(r"migration-id:\s*([0-9a-fA-F-]{36})")


def fetch_uuid_map():
    """Map each existing issue's embedded migration-id (UUID) -> issue number, by paging all
    issues (any state) and reading the hidden marker in each body. Tracker-sourced dedupe."""
    mapping = {}
    page = 1
    while True:
        batch = req("GET", f"/issues?type=issues&state=all&limit=50&page={page}")
        if not isinstance(batch, list) or not batch:
            break
        for iss in batch:
            m = MIGRATION_ID_RE.search(iss.get("body") or "")
            if m:
                mapping.setdefault(m.group(1).lower(), iss.get("number"))
        if len(batch) < 50:
            break
        page += 1
    return mapping


sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _env import load_env


def main(argv):
    global DRY, API, TOKEN
    load_env()  # read migration/migration.config.json directly (real env still wins)
    ap = argparse.ArgumentParser()
    ap.add_argument("--graph", required=True)
    ap.add_argument("--drafts", required=True)
    ap.add_argument("--screenshots", default="screenshots")
    ap.add_argument("--state", default="migration/issues/state.json")
    ap.add_argument("--apply", action="store_true")
    args = ap.parse_args(argv[1:])

    DRY = not args.apply
    graph = json.load(open(args.graph))
    issues = {i["key"]: i for i in graph["issues"]}

    if args.apply:
        url = os.environ.get("FORGEJO_URL")
        TOKEN = os.environ.get("FORGEJO_TOKEN")
        repo = os.environ.get("FORGEJO_REPO")
        missing = [k for k, v in [("FORGEJO_URL", url), ("FORGEJO_TOKEN", TOKEN),
                                  ("FORGEJO_REPO", repo)] if not v]
        if missing:
            sys.stderr.write(f"error: --apply needs env: {', '.join(missing)}\n")
            return 2
        repo = repo.strip().strip("/")
        repo_owner, _, repo_name = repo.partition("/")
        if not repo_owner or not repo_name or "/" in repo_name:
            sys.stderr.write(
                f"error: FORGEJO_REPO must be 'owner/repo' (got '{os.environ.get('FORGEJO_REPO')}')\n")
            return 2
        API = f"{url.rstrip('/')}/api/v1/repos/{repo_owner}/{repo_name}"
        log(f">>> APPLY mode: {API}  (owner={repo_owner}, repo={repo_name})")
    else:
        repo_owner, repo_name = "<owner>", "<repo>"
        API = "${FORGEJO_URL}/api/v1/repos/${FORGEJO_REPO}"
        log(">>> DRY-RUN (no network). Re-run with --apply + FORGEJO_* env to create issues.")

    os.makedirs(os.path.dirname(args.state) or ".", exist_ok=True)

    # --- 1. labels ---
    log(">>> ensure labels")
    label_id = {}
    label_names = sorted({l for i in graph["issues"] for l in i["labels"]})
    existing_labels = req("GET", "/labels?limit=200") if args.apply else []
    existing_labels = existing_labels if isinstance(existing_labels, list) else []
    for name in label_names:
        found = next((x for x in existing_labels if x.get("name") == name), None)
        if found:
            label_id[name] = found["id"]
        else:
            res = req("POST", "/labels", {"name": name, "color": "#0366d6"})
            label_id[name] = res.get("id", 0)

    # --- 2. milestones (one per module) ---
    log(">>> ensure milestones")
    mile_id = {}
    miles = sorted({i["milestone"] for i in graph["issues"]})
    existing_ms = req("GET", "/milestones?state=all&limit=200") if args.apply else []
    existing_ms = existing_ms if isinstance(existing_ms, list) else []
    for ms in miles:
        found = next((x for x in existing_ms if x.get("title") == ms), None)
        if found:
            mile_id[ms] = found["id"]
        else:
            res = req("POST", "/milestones", {"title": ms})
            mile_id[ms] = res.get("id", 0)

    # --- 3. issues in topological order ---
    log(">>> create issues (topological order)")
    # Dedupe is tracker-sourced and UUID-only: the live tracker is the single source of truth.
    # An issue deleted on the tracker is therefore re-created even if a stale state file still
    # references it. (fetch is read-only, so we run it for dry-runs too for an accurate preview.)
    try:
        by_uuid = fetch_uuid_map()
    except urllib.error.URLError:
        by_uuid = {}
    num = {}  # key -> current issue number on the tracker
    for key in graph["order"]:
        it = issues[key]
        if it.get("uuid") and it["uuid"] in by_uuid:
            num[key] = by_uuid[it["uuid"]]
            log(f"    skip (exists #{num[key]}): {it['title']}")
            continue
        body = draft_body(args.drafts, it["slug"])
        payload = {"title": it["title"], "body": body,
                   "labels": [label_id.get(l, 0) for l in it["labels"]]}
        if mile_id.get(it["milestone"]):
            payload["milestone"] = mile_id[it["milestone"]]
        res = req("POST", "/issues", payload)
        num[key] = res.get("number", 0)
        log(f"    {'created' if args.apply else 'would create'} #{num[key]}: {it['title']}")
        # screenshot asset for view issues
        if it.get("view"):
            shot = find_screenshot(args.screenshots, it["slug"])
            if shot:
                asset = req("POST", f"/issues/{num[key]}/assets", multipart=("attachment", shot))
                rel = rel_attachment(asset.get("browser_download_url")) or "<asset-url>"
                newbody = body.replace(f"screenshots/<stamp>/{it['slug']}.png", rel).replace(CAPTION, "")
                req("PATCH", f"/issues/{num[key]}", {"body": newbody})
                log(f"      + screenshot {'attached' if args.apply else 'would attach'}: {os.path.basename(shot)}")
            else:
                log(f"      (no screenshot found for {it['slug']} — run jsf-screenshot-tour)")

    if args.apply:
        json.dump(num, open(args.state, "w"), indent=2)

    # --- 4. native dependencies (blocked-by) ---
    log(">>> wire dependencies (blocked-by)")
    for key in graph["order"]:
        for d in issues[key].get("dependsOn", []):
            child, blocker = num.get(key, 0), num.get(d, 0)
            # Gitea/Forgejo expects an IssueMeta {index, owner, name}; sending only
            # {index} leaves owner/name empty -> "IsErrRepoNotExist".
            try:
                # Forgejo IssueMeta fields are {index, owner, repo} (NOT "name").
                req("POST", f"/issues/{child}/dependencies",
                    {"index": blocker, "owner": repo_owner, "repo": repo_name}, quiet=True)
                log(f"    #{child} blocked-by #{blocker}  ({key} <- {d})")
            except urllib.error.HTTPError as e:
                # tolerate deps that already exist (re-runs) — don't abort the whole pass
                note = "already exists" if e.code in (409, 500) else f"HTTP {e.code}"
                log(f"    . dep #{child} <- #{blocker}: {note}")

    # --- 4b. repair screenshot embeds on existing issues -------------------------
    # Existing issues created before the relative-path fix embed assets as
    # http://localhost:3000/attachments/... (Forgejo ROOT_URL). Rewrite any absolute
    # /attachments/ URL in the body to a host-relative path so it renders inline.
    if args.apply:
        log(">>> repair screenshot embeds (absolute -> relative)")
        fixed = 0
        for key in graph["order"]:
            if not issues[key].get("view"):
                continue
            n = num.get(key, 0)
            if not n:
                continue
            try:
                cur = req("GET", f"/issues/{n}").get("body") or ""
            except urllib.error.HTTPError:
                continue
            newbody = ATTACH_ABS_RE.sub(r"\1", cur).replace(CAPTION, "")
            if newbody != cur:
                req("PATCH", f"/issues/{n}", {"body": newbody})
                fixed += 1
        log(f"    repaired {fixed} issue body/bodies")

    # --- 5. epic task-lists ---
    log(">>> update epic task-lists")
    for key, it in issues.items():
        if it["kind"] != "epic":
            continue
        children = [k for k, c in issues.items() if c.get("epic") == key]
        tasklist = "\n".join(f"- [ ] #{num.get(c, 0)}" for c in children)
        base = draft_body(args.drafts, it["slug"])
        req("PATCH", f"/issues/{num.get(key, 0)}", {"body": base + "\n\n### Views\n" + tasklist})
        log(f"    epic {key}: {len(children)} children linked")

    log(">>> done.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
