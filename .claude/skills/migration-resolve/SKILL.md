---
name: migration-resolve
description: >
  Resolve one or more JSF→Vaadin migration issues by their Forgejo tickets: for each, check the
  ticket's dependencies are met, do the work described IN THE TICKET (port a feature, or implement a
  view), commit, and close the issue. With multiple ids it loops them sequentially (one commit each).
  Works ONLY from the ticket (issue body + labels + native dependencies) — never from migration/ json
  files. Use when the user says "resolve issue N", "solve this ticket", "resolve issues N M O", or
  points at a Forgejo issue URL.
user-invocable: true
argument-hint: "<issue-number-or-URL> [more ids…]"
allowed-tools:
  - Read
  - Glob
  - Grep
  - Write
  - Edit
  - Bash
---

# Migration Resolve

Solve one migration ticket end-to-end: **gate → do the work from the ticket → commit → close.**
Everything needed comes from the **ticket itself** (Forgejo issue body, labels, dependencies) plus
the original JSF source (read-only) — **do NOT read `migration/graph.json`, `state.json`, or
`migration/specs/`.** If the ticket lacks what's needed, say so; don't reconstruct it from migration/.

`$ARGUMENTS`: one **or more** Forgejo issue numbers (or URLs like
`http://host:3000/owner/repo/issues/21` — take the trailing number). With several, resolve them **one
at a time, in the given order**, looping the workflow below for each (see *Multiple issues*). Each
issue still gets its own commit + close.

## Workflow (per issue)

1. **Fetch the ticket + readiness** (the helper hits only the Forgejo API):
   ```bash
   python3 migration/scripts/resolve.py --issue <N>
   ```
   Returns `{number, title, state, kind, module, labels, body, dependencies, ready, unmet}`.
   - If `state == "closed"` → already resolved; stop.
   - If `ready == false` → **requirements not met.** Print the `unmet` blockers (number + title) and
     **stop** — e.g. "Blocked by #76 [port] registration/wallet (open)." Do not start work.

2. **Sanity-check the ticket is self-contained.** The body must contain what you need:
   - view tickets → the fields/columns/actions(→service calls)/validation + the screenshot;
   - port tickets → the feature name + the conversion checklist.
   If a view ticket says "No behavior spec found" (or is missing the spec), report that the ticket is
   incomplete and ask for it to be regenerated **with the spec embedded** — do not pull it from
   `migration/specs/`.

3. **Do the work — dispatch by `kind`** (all output goes into `migration/vaadin/`; JSF source is
   read-only reference):
   - **`foundation`** → run the scaffold (`vaadin-scaffold` / `setup_vaadin_module.py`) so the
     Vaadin shell exists and compiles.
   - **`port`** → port the ticket's feature: find its entity/repository/service/logics in the
     original JSF source, and rewrite them into `migration/vaadin` as jakarta + Spring Data + Spring
     (`@Service`, constructor injection, `JpaRepository`(+`Specification`), `@EventListener`) — keep
     the logic, change only plumbing (see `../vaadin-scaffold/references/service-port-playbook.md`).
     Make it compile; add/run tests for the feature.
   - **`list` / `form` / `detail`** → implement the Vaadin Flow view **test-first (TDD)** from the
     ticket's spec + screenshot, calling the (already-ported, since the port dep is closed) services.
     Follow `../jsf-view-to-vaadin/SKILL.md` for the view patterns, but source the spec from the
     **ticket body**, not from `migration/specs/`. **Pick each field's component by its Java type, not
     the legacy JSF tag** (date→`DatePicker`, money→`BigDecimalField`, enum/reference→`ComboBox`,
     boolean→`Checkbox`) — don't carry over a too-literal mapping like a date rendered as a text field.
   - **`epic`** → not directly solvable; if all its child issues are closed, close it, else report
     what's left.

4. **Verify**: the relevant module compiles and the new tests pass
   (`cd migration/vaadin && ./mvnw test`). Don't proceed on red.
   - **For every view ticket, exercise the whole flow you built in a real browser** — browserless
     tests aren't enough. Run the app (`./mvnw spring-boot:run`) and use the Playwright MCP (or
     `/vaadin-migration-verify`) to drive end-to-end **everything the view does**, by `kind`:
     - **list** → it loads with data; pagination, sorting and filtering work; each row action and the
       global Add navigate to the right route.
     - **form** → create *and* edit paths; submit **empty/invalid** (blank required, bad date,
       out-of-range) → save **blocked** with a **clear field-level error** (no raw key/stack trace),
       each field enforces its type; submit **valid** → saves + success `Notification` + navigates;
       Back/Cancel discards.
     - **detail** → renders the loaded record; its actions (edit/delete/print/…) work.
     - **dialogs** (e.g. confirm-delete) confirm *and* cancel correctly; **notifications** show;
       **navigation** between the views works; the view + its menu entry are **permission-gated**.
     Walk the same paths the original JSF view supported. **Do not close the ticket** if any flow is
     broken, a validation doesn't fire / shows an unclear message, or a field has the wrong component
     for its type — fix it first.

5. **Commit** the work (one-line message referencing the issue, no co-author line).

6. **Close the ticket** with a comment noting what was done + the commit:
   ```bash
   python3 migration/scripts/resolve.py --close <N> \
     --comment "Resolved: <summary>. Commit <sha>."
   ```

## Multiple issues (batch)
When given several ids, process them **sequentially**, running the full per-issue workflow (gate → do
the work → verify → commit → **close**) for each before moving to the next. Rules:
- Print progress per item: `[n/total] #<id> <title>`.
- **Gate each independently.** If one is `closed` already → skip it (note it) and continue. If one is
  **blocked** (`ready == false`) → skip it with its `unmet` blockers and continue to the rest (a later
  id may have been unblocked by an earlier one you just closed).
- **Stop on a real failure** — if the work doesn't compile or tests go red on an issue, do **not**
  close it and do **not** continue to the rest; report what passed and what failed so the batch stays
  reviewable. Don't cascade past a broken build.
- One commit per issue (not one giant commit), so each resolution is independently reviewable.
- End with a summary: resolved (with commit shas), skipped-blocked (with blockers), and any failure.

## Doing the whole port phase first
Port the entire service layer before any UI work. List the ready port tickets, then resolve them in
one batch (in the returned, dependency-safe order):
```bash
python3 migration/scripts/resolve.py --ready port   # open port issues with deps met
```
Pass those numbers to this skill at once, e.g. `/migration-resolve 61 62 63 …`. Then move on to the
view tickets the same way (`--ready` for all remaining, login first).

## Config
`resolve.py` dispatches by **`TRACKER`** (`forgejo` | `vikunja` | `openproject`; default `forgejo`).
Only the Forgejo resolver is implemented today; other trackers report "not implemented." The Forgejo
backend reads `FORGEJO_URL`, `FORGEJO_REPO`, `FORGEJO_TOKEN` from `migration.config.json` (or your
shell); the token needs `write:issue` to close/comment.

## Notes
- **Ticket-only:** the gate (dependencies) and the spec both come from Forgejo — closing the `[port]`
  dependency *is* the "service port done" gate for a view, so no `migration/specs` lookup is needed.
- One **commit per issue** even in a batch, so each resolution is independently reviewable. A batch is
  just the per-issue workflow looped; it never merges work into one commit.
- Duplicates are prevented at **creation**: each issue carries a deterministic `migration-id` UUID
  (hidden body marker), and `forgejo_issues.py` skips creating an issue whose UUID already exists on
  the tracker — so re-running the create step never duplicates, with or without a state file.
