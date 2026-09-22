#!/usr/bin/env python3
"""OpenProject work-package adapter for the JSF→Vaadin migration.

Consumes a graph.json (from build_issue_graph.py) + the drafts/ dir and creates OpenProject
work packages (one per migration issue) in a target project, using OpenProject's native
hierarchy and relations:
  - foundation  -> a Task
  - module epic -> an Epic (type), parent = foundation
  - per view    -> a Task, parent = its module epic
  - dependencies (login / list ordering) -> "blocked" relations (child blocked by blocker)
  - screenshots -> work-package attachments
Idempotent via a state file. Mirrors forgejo_issues.py / vikunja_issues.py.

DEFAULT = dry-run: prints the planned API calls, makes NO network requests.
Use --apply to actually call the OpenProject API.

Required env when --apply:
  OPENPROJECT_URL      e.g. http://10.88.14.29:9080
  OPENPROJECT_TOKEN    an API key (My account → Access tokens → API)
  OPENPROJECT_PROJECT  project id or identifier (e.g. 3 or "webbudget")
Optional:
  OPENPROJECT_TYPE_TASK  work-package type id for tasks (default 1)
  OPENPROJECT_TYPE_EPIC  work-package type id for epics (default 5)

Usage:
  openproject_issues.py --graph migration/issues/graph.json --drafts migration/issues/drafts \
                        [--screenshots screenshots] [--state migration/issues/state.openproject.json] [--apply]

Pure stdlib. Verified against OpenProject API v3 (HAL+JSON):
  POST /api/v3/projects/{id}/work_packages   {subject, description{format,raw}, scheduleManually,
                                              _links{type, parent?}}            -> {id}
  POST /api/v3/work_packages/{from}/relations {type:"blocked", _links{to}}      relation
  POST /api/v3/work_packages/{id}/attachments multipart(metadata json + file)   attachment
Note: OpenProject Community has no issue labels — module/kind live in the subject line.
"""
import argparse
import base64
import glob
import json
import os
import sys
import urllib.request
import urllib.error

DRY = True
API = ""
AUTH = ""


def log(msg):
    print(msg, flush=True)


def req(method, path, body=None, multipart=None):
    if DRY:
        extra = ""
        if body is not None:
            extra = "  <- " + json.dumps(body)
        elif multipart:
            extra = f"  <- file {multipart[1]}"
        log(f"    [dry-run] {method} {API}{path}{extra}")
        return {"id": 0}
    url = API + path
    headers = {"Authorization": AUTH}
    if multipart is not None:
        data, ctype = _encode_attachment(*multipart)
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
        sys.stderr.write(f"HTTP {e.code} on {method} {path}: {e.read().decode()[:300]}\n")
        raise


def _encode_attachment(field, filepath):
    """OpenProject attachment upload = multipart with a JSON 'metadata' part + a 'file' part."""
    boundary = "----jsfvaadinboundary7e3f"
    with open(filepath, "rb") as fh:
        content = fh.read()
    fname = os.path.basename(filepath)
    meta = json.dumps({"fileName": fname, "description": {"raw": "baseline screenshot"}})
    parts = []
    parts.append(f"--{boundary}\r\n".encode())
    parts.append(b'Content-Disposition: form-data; name="metadata"\r\n')
    parts.append(b"Content-Type: application/json\r\n\r\n")
    parts.append(meta.encode() + b"\r\n")
    parts.append(f"--{boundary}\r\n".encode())
    parts.append(f'Content-Disposition: form-data; name="file"; filename="{fname}"\r\n'.encode())
    parts.append(b"Content-Type: image/png\r\n\r\n")
    parts.append(content + b"\r\n")
    parts.append(f"--{boundary}--\r\n".encode())
    return b"".join(parts), f"multipart/form-data; boundary={boundary}"


def draft_body(drafts_dir, slug):
    m = sorted(glob.glob(os.path.join(drafts_dir, f"*-{slug}.md")))
    if m:
        with open(m[0]) as fh:
            return fh.read()
    return f"(no draft for {slug})"


def find_screenshot(screenshots_dir, slug):
    m = sorted(glob.glob(os.path.join(screenshots_dir, "*", f"{slug}.png")))
    return m[0] if m else None


sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _env import load_env


def main(argv):
    global DRY, API, AUTH
    load_env()  # read migration/migration.config.json directly (real env still wins)
    ap = argparse.ArgumentParser()
    ap.add_argument("--graph", required=True)
    ap.add_argument("--drafts", required=True)
    ap.add_argument("--screenshots", default="screenshots")
    ap.add_argument("--state", default="migration/issues/state.openproject.json")
    ap.add_argument("--apply", action="store_true")
    args = ap.parse_args(argv[1:])

    DRY = not args.apply
    graph = json.load(open(args.graph))
    issues = {i["key"]: i for i in graph["issues"]}

    type_task = os.environ.get("OPENPROJECT_TYPE_TASK", "1")
    type_epic = os.environ.get("OPENPROJECT_TYPE_EPIC", "5")
    if args.apply:
        url = os.environ.get("OPENPROJECT_URL")
        token = os.environ.get("OPENPROJECT_TOKEN")
        project = os.environ.get("OPENPROJECT_PROJECT")
        missing = [k for k, v in [("OPENPROJECT_URL", url), ("OPENPROJECT_TOKEN", token),
                                  ("OPENPROJECT_PROJECT", project)] if not v]
        if missing:
            sys.stderr.write(f"error: --apply needs env: {', '.join(missing)}\n")
            return 2
        API = f"{url.rstrip('/')}/api/v3"
        AUTH = "Basic " + base64.b64encode(f"apikey:{token}".encode()).decode()
        log(f">>> APPLY mode: {API} (project {project})")
    else:
        project = "${OPENPROJECT_PROJECT}"
        API = "${OPENPROJECT_URL}/api/v3"
        log(">>> DRY-RUN (no network). Re-run with --apply + OPENPROJECT_* env to create work packages.")

    os.makedirs(os.path.dirname(args.state) or ".", exist_ok=True)
    state = {}
    if os.path.isfile(args.state):
        try:
            state = json.load(open(args.state))
        except ValueError:
            state = {}

    def type_href(kind):
        return f"/api/v3/types/{type_epic if kind == 'epic' else type_task}"

    # --- 1. create work packages (topological order: foundation -> epics -> views) ---
    log(">>> create work packages (topological order)")
    wid = dict(state)
    for key in graph["order"]:
        it = issues[key]
        if wid.get(key):
            log(f"    skip (exists #{wid[key]}): {it['title']}")
            continue
        links = {"type": {"href": type_href(it["kind"])}}
        # hierarchy: epic.parent = foundation; view.parent = its module epic
        parent_key = "foundation" if it["kind"] == "epic" else it.get("epic")
        if parent_key and wid.get(parent_key):
            links["parent"] = {"href": f"/api/v3/work_packages/{wid[parent_key]}"}
        body = {"subject": it["title"][:255],
                "description": {"format": "markdown", "raw": draft_body(args.drafts, it["slug"])},
                "scheduleManually": True,
                "_links": links}
        res = req("POST", f"/projects/{project}/work_packages", body)
        wid[key] = res.get("id", 0)
        log(f"    {'created' if args.apply else 'would create'} WP #{wid[key]}: {it['title']}")
        if it.get("view"):
            shot = find_screenshot(args.screenshots, it["slug"])
            if shot:
                req("POST", f"/work_packages/{wid[key]}/attachments", multipart=("file", shot))
                log(f"      + screenshot {'attached' if args.apply else 'would attach'}: {os.path.basename(shot)}")
            else:
                log(f"      (no screenshot for {it['slug']} — run jsf-screenshot-tour)")

    if args.apply:
        json.dump(wid, open(args.state, "w"), indent=2)

    # --- 2. dependencies as 'blocked' relations (skip foundation/epic — covered by hierarchy) ---
    log(">>> wire dependencies (blocked relations)")
    for key in graph["order"]:
        it = issues[key]
        child = wid.get(key, 0)
        for d in it.get("dependsOn", []):
            if d == "foundation" or d == it.get("epic"):
                continue  # expressed via parent/child hierarchy
            blocker = wid.get(d, 0)
            try:
                req("POST", f"/work_packages/{child}/relations",
                    {"type": "blocked", "_links": {"to": {"href": f"/api/v3/work_packages/{blocker}"}}})
                log(f"    #{child} blocked-by #{blocker}  ({key} <- {d})")
            except urllib.error.HTTPError as e:
                log(f"    ! skip rel #{child} <- #{blocker}: HTTP {e.code}")

    log(">>> done.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
