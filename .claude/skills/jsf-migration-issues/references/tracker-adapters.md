# Tracker adapters

The graph builder (`build_issue_graph.py`) is tracker-agnostic; all tracker specifics live in the
adapters. Call them through the generic entry **`create_issues.py`**, which dispatches by the
**`TRACKER`** config value (`forgejo` | `vikunja` | `openproject`; default `forgejo`) — you don't run
the backends directly. Three backends ship: **`forgejo_issues.py`** (Forgejo/Gitea),
**`vikunja_issues.py`** (Vikunja), **`openproject_issues.py`** (OpenProject). This documents the
endpoints each uses and how other trackers (GitHub/GitLab) would differ, so new adapters can be added
against the same `graph.json` (register them in `create_issues.py`'s `ADAPTERS` map).

| Tracker | Adapter | Deps mechanism | Screenshots | Grouping |
|---------|---------|----------------|-------------|----------|
| Forgejo/Gitea | `forgejo_issues.py` | native issue dependencies (blocked-by) | issue asset upload | labels + milestones + epic task-list |
| Vikunja | `vikunja_issues.py` | task relations (`blocked`) | task attachments | labels + project + epic subtask relations |
| OpenProject | `openproject_issues.py` | work-package relations (`blocked`) | WP attachments | type Epic/Task + parent/child hierarchy |
| GitHub | _(notes only)_ | `Depends on #N` + epic task-list | commit to repo, raw link | labels + milestones |
| GitLab | _(notes only)_ | issue links (`blocks`) | uploads API | labels + milestones |

## Forgejo / Gitea (primary)

Base: `${FORGEJO_URL}/api/v1/repos/{owner}/{repo}`  · Auth: header `Authorization: token <TOKEN>`
Token scopes: `write:issue`, `write:repository`.

| Purpose | Call |
|---------|------|
| List labels | `GET /labels?limit=200` |
| Create label | `POST /labels` `{name, color}` → `{id}` |
| List milestones | `GET /milestones?state=all&limit=200` |
| Create milestone | `POST /milestones` `{title}` → `{id}` |
| Create issue | `POST /issues` `{title, body, labels:[<id>], milestone:<id>}` → `{number}` |
| Edit issue body | `PATCH /issues/{index}` `{body}` |
| Upload screenshot | `POST /issues/{index}/assets` multipart `attachment=@file.png` → `{browser_download_url}` |
| Add dependency | `POST /issues/{index}/dependencies` `{index, owner, repo}` (IssueMeta — the blocking issue; **owner+repo required**, else `IsErrRepoNotExist`. Note the field is `repo`, not `name`) |

Notes:
- **Labels are referenced by integer id**, not name — the adapter ensures each label exists and maps
  name→id first.
- **Issue dependencies are native** in Forgejo/Gitea — `dependencies` sets "blocked by", which is
  exactly the migration ordering. (Requires repo setting *Enable Dependencies*, on by default.)
- **Screenshots**: upload as an issue asset, then PATCH the body to embed the returned
  `browser_download_url`. No need to commit images into the repo.
- The adapter creates issues in `graph.json.order` (topological) so a blocker always exists before
  its dependent, then wires dependencies in a second pass.

## Vikunja (`vikunja_issues.py`) — verified against v2.3.0

Base: `${VIKUNJA_URL}/api/v1`  · Auth: header `Authorization: Bearer <TOKEN>` (an API token from
Settings → API Tokens, or a JWT from `POST /login`). Env: `VIKUNJA_URL`, `VIKUNJA_TOKEN`,
`VIKUNJA_PROJECT` (numeric project id all tasks are created in).

| Purpose | Call |
|---------|------|
| List labels | `GET /labels` |
| Create label | `PUT /labels` `{title, hex_color}` → `{id}` |
| Create task | `PUT /projects/{id}/tasks` `{title, description}` → `{id}` |
| Attach label | `PUT /tasks/{task}/labels` `{label_id}` |
| Upload screenshot | `PUT /tasks/{id}/attachments` multipart `files=@file.png` |
| Add relation | `PUT /tasks/{taskID}/relations` `{task_id, other_task_id, relation_kind}` |

Mapping of the migration model onto Vikunja:
- **Issue → task** in the target project (`VIKUNJA_PROJECT`).
- **Dependency** ("child depends on blocker") → on the child task,
  `relation_kind: "blocked"` with `other_task_id = blocker` (Vikunja auto-creates the inverse
  `blocking` on the blocker). `relation_kind` enum:
  `subtask, parenttask, related, duplicateof, duplicates, blocking, blocked, precedes, follows, …`.
- **Epic → views** → on the epic task, `relation_kind: "subtask"` with `other_task_id = child`.
- **Module/kind** → Vikunja **labels** (no milestones in Vikunja). Optionally create a sub-project per
  module via `PUT /projects {title, parent_project_id}` instead of labels.
- **Screenshots** → task attachments (shown in the task; not embedded inline in the description —
  Vikunja descriptions are rich-text/HTML, so the markdown draft renders mostly as-is).
- **Idempotency** → separate state file `migration/issues/state.vikunja.json` (key → task id) so
  Forgejo and Vikunja runs don't collide.

## OpenProject (`openproject_issues.py`) — verified against API v3

Base: `${OPENPROJECT_URL}/api/v3`  · Auth: HTTP Basic with username `apikey` and password = the API
key (My account → Access tokens → API) → `Authorization: Basic base64("apikey:<key>")`. Env:
`OPENPROJECT_URL`, `OPENPROJECT_TOKEN`, `OPENPROJECT_PROJECT` (id or identifier),
optional `OPENPROJECT_TYPE_TASK` (default 1), `OPENPROJECT_TYPE_EPIC` (default 5).

| Purpose | Call |
|---------|------|
| Create work package | `POST /projects/{id}/work_packages` `{subject, description{format:"markdown",raw}, scheduleManually:true, _links{type, parent?}}` → `{id}` |
| Add relation | `POST /work_packages/{from}/relations` `{type:"blocked", _links{to}}` |
| Upload screenshot | `POST /work_packages/{id}/attachments` multipart: a JSON `metadata` part + a `file` part |

HAL+JSON specifics (verified live):
- **Type is required** and must be **enabled for the project** (Project settings → Work package
  types). Default mapping uses **Task (id 1)** for foundation + views and **Epic (id 5)** for module
  epics — enable Epic in the project or override `OPENPROJECT_TYPE_EPIC`.
- **Hierarchy** via `_links.parent`: epic.parent = foundation, view.parent = its module epic. Set at
  create time (topological order guarantees the parent exists first).
- **Dependencies** → `POST .../{child}/relations {type:"blocked", to: blocker}`. OpenProject
  canonicalizes this to "blocker **blocks** child" (correct direction — verified). The adapter
  **skips** deps that are the issue's epic or foundation (already expressed by the parent/child
  hierarchy; posting them would raise "circular dependency"). So only login/list ordering becomes
  relations.
- **`scheduleManually: true`** is set on every WP so `blocked`/`follows` relations don't trigger
  date-scheduling errors.
- **No labels** in OpenProject Community — module/kind live in the subject line (`[module] kind: …`).
- **Idempotency** → state file `migration/issues/state.openproject.json` (key → work-package id).

## GitHub (adapter notes — not yet implemented)

- Base `https://api.github.com/repos/{owner}/{repo}`; auth `Authorization: Bearer <PAT>`.
- Labels referenced **by name** (no id lookup needed). Milestones by number.
- **No native issue dependencies** in the REST API → express dependencies as `Depends on #N` text
  (already in the body) plus a **task-list in the epic** issue; optionally use the newer sub-issues
  feature if available.
- **Screenshots**: the REST API has no simple attachment upload. Either commit PNGs into the repo
  (e.g. `docs/migration/screenshots/`) and reference the raw URL, or attach via the web UI. The
  adapter should rewrite the embed to the committed raw path.
- `gh` CLI (`gh issue create`, `gh api`) is a convenient alternative if installed.

## GitLab (adapter notes — not yet implemented)

- Base `https://gitlab.example/api/v4/projects/{id}`; auth `PRIVATE-TOKEN: <token>`.
- Labels by name; milestones by id.
- **Issue links** express dependencies: `POST /issues/{iid}/links` with `link_type=blocks` /
  `is_blocked_by` (some link types are tier-gated).
- **Screenshots**: `POST /uploads` returns a markdown snippet to embed in the issue description.
- `glab` CLI is an alternative if installed.

## State / idempotency

Dedupe is **UUID-only and sourced from the tracker** — the live tracker is the single source of
truth. Each issue carries a deterministic `migration-id` UUID (a hidden body marker); on every run the
Forgejo adapter pages all existing issues, reads their markers, and skips any UUID that already
exists. So re-runs never duplicate, and **deleting issues on the tracker and re-running recreates them
cleanly**.

The `migration/issues/state.json` file (and the `state.vikunja.json` / `state.openproject.json`
variants) is only a **written record** of each graph **key** → issue number for that run; it is **not
read for dedupe**. It is a per-environment runtime cache and is **gitignored** — tracking it once let
a stale cache sync between machines and wrongly skip recreated issues. The `graph.json` keys
(`foundation`, `epic:<module>`, `view:<slug>`) and their UUIDs are stable, so a regenerated graph
maps cleanly onto existing issues by UUID.
