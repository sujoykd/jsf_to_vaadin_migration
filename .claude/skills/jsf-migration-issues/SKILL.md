---
name: jsf-migration-issues
description: >
  Turn a JSF→Vaadin migration into tracker issues — one issue per view plus per-module epics and a
  foundation issue — with screenshot, what-to-implement, acceptance criteria, and dependency
  references. Builds a dependency graph from the view inventory + behavior specs and creates the
  issues on Forgejo (Gitea API), Vikunja, or OpenProject, with adapter notes for GitHub/GitLab. Use
  when the user wants to "create migration issues", "track the migration in Forgejo/Vikunja/
  OpenProject", or "make an issue / task / work package per view".
user-invocable: true
argument-hint: "[--apply]"
allowed-tools:
  - Read
  - Glob
  - Grep
  - Write
  - Bash
---

# JSF Migration Issues

Generates a tracked, ordered backlog for the JSF→Vaadin migration: **one issue per view**, plus
**per-module epics** and a **foundation issue** that blocks everything. Each issue carries a
screenshot, a description of what to implement, acceptance criteria, and dependency references.
Tracker-agnostic graph + a **Forgejo** adapter (Gitea REST API). Default is **dry-run** — it only
creates live issues with `--apply` and a token.

## Inputs it uses
- View inventory — from `jsf-screenshot-tour/scripts/enumerate_views.py` (kind/module/needs-id + slug).
- Behavior specs (optional but recommended) — `migration/specs/<slug>.json` from `jsf-view-analyze`;
  they enrich each issue with fields, columns, actions→service-calls, validations, permissions.
- Screenshots — `screenshots/<stamp>/<slug>.png` from `jsf-screenshot-tour` (embedded as assets).

## Workflow

1. **Build the view inventory** (if not already present):
   ```bash
   python3 migration/scripts/enumerate_views.py <webapp-root> <base-url> \
     > migration/views.json
   ```

2. **Build the issue graph + drafts** (read-only, no tracker):
   ```bash
   python3 migration/scripts/build_issue_graph.py \
     --views migration/views.json --specs migration/specs --out migration/issues
   ```
   Produces `migration/issues/graph.json` (issues + dependency edges + topological order) and
   `migration/issues/drafts/NNN-<slug>.md` (one issue body each). Review the drafts.
   - **Dependency rules:** foundation blocks everything; login blocks every secured view; each
     `form`/`detail` depends on its folder's `list`; dashboard depends on login; each view belongs to
     its module epic. See `references/issue-template.md`.

3. **Dry-run** (no network) to preview the API calls. The tracker is chosen by **`TRACKER`** in
   `migration.config.json` (`forgejo` | `vikunja` | `openproject`; default `forgejo`):
   ```bash
   python3 migration/scripts/create_issues.py \
     --graph migration/issues/graph.json --drafts migration/issues/drafts --screenshots screenshots
   ```

4. **Create the issues live** (only when the user asks). Same command + `--apply`. `create_issues.py`
   dispatches to the selected tracker's adapter; each reads its own creds from `migration.config.json`
   and is idempotent (skips issues whose `migration-id` UUID already exists):
   ```bash
   python3 migration/scripts/create_issues.py \
     --graph migration/issues/graph.json --drafts migration/issues/drafts --screenshots screenshots --apply
   ```
   Per-tracker behavior (backends behind the dispatcher):
   - **Forgejo/Gitea** (`FORGEJO_URL/REPO/TOKEN`) — labels + milestones, native dependencies
     (blocked-by), screenshot asset upload, epic task-lists.
   - **Vikunja** (`VIKUNJA_URL/TOKEN/PROJECT`) — tasks, labels, attachments, `blocked` relations,
     epic→child subtasks.
   - **OpenProject** (`OPENPROJECT_URL/TOKEN/PROJECT`) — work packages (Task/Epic), parent/child
     hierarchy, attachments, `blocked` relations (enable the Epic type for the project first).
   ```

## Output
- `migration/issues/graph.json`, `migration/issues/drafts/*.md`, and (after `--apply`)
  `migration/issues/state.json` mapping graph keys → created issue numbers.

## Notes
- **Generic / adapter-based.** The graph is tracker-agnostic; tracker specifics live in the adapters.
  Forgejo, Vikunja, and OpenProject adapters ship; see `references/tracker-adapters.md` for their
  endpoints plus GitHub/GitLab notes.
- **Forgejo token** needs `write:issue` + `write:repository`; target the *migration* repo (e.g. the
  the migration tracker project/repo), not necessarily the JSF source repo.
- Run `jsf-view-analyze` first for rich issues; without specs, issues fall back to a kind-based stub
  and say so. Run `jsf-screenshot-tour` first so screenshots exist to attach.
- The Python adapter uses only the stdlib (`urllib`+`json`) — portable, no `jq`/`gh`/`tea` needed.
