#!/usr/bin/env python3
"""Vikunja task adapter for the JSF→Vaadin migration.

Consumes a graph.json (from build_issue_graph.py) + the drafts/ dir and creates Vikunja
tasks (one per migration issue) in a target project, with labels, screenshot attachments,
blocked-by relations (the dependency graph), and epic→child subtask relations. Idempotent
via a state file. Mirrors forgejo_issues.py — same graph, different tracker.

DEFAULT = dry-run: prints the planned API calls, makes NO network requests.
Use --apply to actually call the Vikunja API.

Required env when --apply:
  VIKUNJA_URL      e.g. http://10.88.14.29:3456
  VIKUNJA_TOKEN    an API token (Settings → API Tokens) or a JWT from /login
  VIKUNJA_PROJECT  numeric project id that tasks are created in

Usage:
  vikunja_issues.py --graph migration/issues/graph.json --drafts migration/issues/drafts \
                    [--screenshots screenshots] [--state migration/issues/state.vikunja.json] [--apply]

Pure stdlib (urllib + json). Verified against Vikunja v2.3.0 API (/api/v1):
  GET  /labels                         list labels
  PUT  /labels                 {title,hex_color}            create label -> {id}
  PUT  /projects/{id}/tasks    {title,description}          create task  -> {id}
  PUT  /tasks/{task}/labels    {label_id}                   attach label
  PUT  /tasks/{id}/attachments multipart files=@png         upload screenshot
  PUT  /tasks/{taskID}/relations {task_id,other_task_id,relation_kind}
       relation_kind "blocked"  => this task is blocked by other_task_id (dependency)
       relation_kind "subtask"  => other_task_id is a subtask of this task (epic→child)
"""
import argparse
import glob
import json
import os
import sys
import urllib.request
import urllib.error

DRY = True
API = ""
TOKEN = ""


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
    headers = {"Authorization": f"Bearer {TOKEN}"}
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
    global DRY, API, TOKEN
    load_env()  # read migration/migration.config.json directly (real env still wins)
    ap = argparse.ArgumentParser()
    ap.add_argument("--graph", required=True)
    ap.add_argument("--drafts", required=True)
    ap.add_argument("--screenshots", default="screenshots")
    ap.add_argument("--state", default="migration/issues/state.vikunja.json")
    ap.add_argument("--apply", action="store_true")
    args = ap.parse_args(argv[1:])

    DRY = not args.apply
    graph = json.load(open(args.graph))
    issues = {i["key"]: i for i in graph["issues"]}

    project = None
    if args.apply:
        url = os.environ.get("VIKUNJA_URL")
        TOKEN = os.environ.get("VIKUNJA_TOKEN")
        project = os.environ.get("VIKUNJA_PROJECT")
        missing = [k for k, v in [("VIKUNJA_URL", url), ("VIKUNJA_TOKEN", TOKEN),
                                  ("VIKUNJA_PROJECT", project)] if not v]
        if missing:
            sys.stderr.write(f"error: --apply needs env: {', '.join(missing)}\n")
            return 2
        API = f"{url.rstrip('/')}/api/v1"
        log(f">>> APPLY mode: {API} (project {project})")
    else:
        API = "${VIKUNJA_URL}/api/v1"
        project = "${VIKUNJA_PROJECT}"
        log(">>> DRY-RUN (no network). Re-run with --apply + VIKUNJA_* env to create tasks.")

    os.makedirs(os.path.dirname(args.state) or ".", exist_ok=True)
    state = {}
    if os.path.isfile(args.state):
        try:
            state = json.load(open(args.state))
        except ValueError:
            state = {}

    # --- 1. labels ---
    log(">>> ensure labels")
    label_id = {}
    names = sorted({l for i in graph["issues"] for l in i["labels"]})
    existing = req("GET", "/labels") if args.apply else []
    existing = existing if isinstance(existing, list) else []
    for name in names:
        found = next((x for x in existing if x.get("title") == name), None)
        if found:
            label_id[name] = found["id"]
        else:
            res = req("PUT", "/labels", {"title": name, "hex_color": "4a90d9"})
            label_id[name] = res.get("id", 0)

    # --- 2. create tasks (topological order) ---
    log(">>> create tasks (topological order)")
    tid = dict(state)
    for key in graph["order"]:
        it = issues[key]
        if tid.get(key):
            log(f"    skip (exists #{tid[key]}): {it['title']}")
            continue
        body = draft_body(args.drafts, it["slug"])
        res = req("PUT", f"/projects/{project}/tasks", {"title": it["title"], "description": body})
        tid[key] = res.get("id", 0)
        log(f"    {'created' if args.apply else 'would create'} task #{tid[key]}: {it['title']}")
        for l in it["labels"]:
            req("PUT", f"/tasks/{tid[key]}/labels", {"label_id": label_id.get(l, 0)})
        if it.get("view"):
            shot = find_screenshot(args.screenshots, it["slug"])
            if shot:
                req("PUT", f"/tasks/{tid[key]}/attachments", multipart=("files", shot))
                log(f"      + screenshot {'attached' if args.apply else 'would attach'}: {os.path.basename(shot)}")
            else:
                log(f"      (no screenshot for {it['slug']} — run jsf-screenshot-tour)")

    if args.apply:
        json.dump(tid, open(args.state, "w"), indent=2)

    # --- 3. dependencies: child 'blocked' by blocker ---
    log(">>> wire dependencies (blocked-by)")
    for key in graph["order"]:
        child = tid.get(key, 0)
        for d in issues[key].get("dependsOn", []):
            blocker = tid.get(d, 0)
            req("PUT", f"/tasks/{child}/relations",
                {"task_id": child, "other_task_id": blocker, "relation_kind": "blocked"})
            log(f"    #{child} blocked-by #{blocker}  ({key} <- {d})")

    # --- 4. epics: child as subtask of epic ---
    log(">>> link epic subtasks")
    for key, it in issues.items():
        if it["kind"] != "epic":
            continue
        epic = tid.get(key, 0)
        children = [k for k, c in issues.items() if c.get("epic") == key]
        for c in children:
            req("PUT", f"/tasks/{epic}/relations",
                {"task_id": epic, "other_task_id": tid.get(c, 0), "relation_kind": "subtask"})
        log(f"    epic #{epic}: {len(children)} subtasks linked")

    log(">>> done.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
