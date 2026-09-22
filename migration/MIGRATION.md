# MIGRATION.md — JSF 2 → Vaadin 25 (Flow / Spring Boot)

Exact steps. Run from the JSF project root. Skills live in `.claude/skills/`; helper scripts in
`migration/scripts/`. Phase A (steps 1–7): plan + decide architecture + create tracker issues.
Phase B (steps 8–11): resolve them.

## 0. Prerequisites + config

Dev container (Claude + Playwright MCP): "Reopen in Container". Required tools: java 21, maven,
python3, node 24 (handled by Vaadin). Target (fixed): Vaadin 25 + Spring Boot 4 + Java 21 +
Jakarta EE 11 + Flow.

Copy the example config to `migration/migration.config.json` and fill in your tracker token(s). It is
the one config for everything (URLs, repo/project, tokens, DB, BASE_URL). The scripts read it
automatically via `migration/scripts/_env.py`; any matching environment variable you export overrides
a value in the file.

```bash
cp migration/migration.config.example.json migration/migration.config.json
```

---
# Phase A — Plan & create issues

## 1. Start the source app

Needed on the host for screenshots + the final comparison. In the devcontainer the app is reached at
`BASE_URL` (`https://host.docker.internal:8443`), not localhost.

```bash
cd docker && docker compose up -d && cd ..
```

## 2. Assess the source

```bash
python3 migration/scripts/detect_stack.py . > migration/manifest.json
cat migration/manifest.json
```

## 3. Capture screenshots (baseline)

Uses `BASE_URL` from `migration.config.json` (or pass the URL / let it ask). Enter credentials when
prompted. Output: `screenshots/<stamp>/`.

```
/jsf-screenshot-tour
```

## 4. Build view inventory

`WEBAPP`/`BASE_URL` come from `migration.config.json`.

```bash
python3 migration/scripts/enumerate_views.py > migration/views.json
```

## 5. Analyze all views → behavior specs (automated, no manual file hunting)

First auto-map every view to its xhtml + backing-bean `.java` file(s) (`WEBAPP` from the config):

```bash
python3 migration/scripts/resolve_views.py > migration/views-resolved.json
```

Then batch-analyze. With no args, `/jsf-view-analyze` analyzes every view from
`views-resolved.json` and writes `migration/specs/<slug>.json` + `.md` (one per view).

```
/jsf-view-analyze
```

## 6. Decide the target architecture

Make the app-wide, decide-once choices the foundation bakes in and every view inherits — authentication
(form login vs OAuth2/OIDC/SAML/LDAP), authorization, theme & branding, layout & navigation,
localization, persistence, push, error handling, files/reports, build, testing. The skill interviews
you (seeding defaults from `manifest.json`) and writes `migration/architecture.json`; the next step
embeds it into the foundation ticket. (Can be run any time after step 2; must be before step 7.)

```
/migration-architecture
```

## 7. Create tracker issues

Step 5 must have run first — it populates `migration/specs/`. The `--specs migration/specs` flag on
`build_issue_graph.py` below then embeds each view's spec into its ticket body, so the ticket is
self-contained (the resolver works from the ticket, not from `migration/specs`).

Build the issue graph + drafts:

```bash
python3 migration/scripts/build_issue_graph.py \
  --views migration/views.json --specs migration/specs --out migration/issues
```

`build_issue_graph.py` also reads `migration/architecture.json` (step 6) and embeds the decided
architecture into the foundation ticket, so `/migration-resolve <foundation#>` scaffolds per your
choices.

Review the generated drafts in `migration/issues/drafts/`, then dry-run the tracker calls. The
tracker is chosen by `TRACKER` in `migration.config.json` (forgejo | vikunja | openproject; default
forgejo):

```bash
python3 migration/scripts/create_issues.py \
  --graph migration/issues/graph.json --drafts migration/issues/drafts --screenshots screenshots
```

Create live by adding `--apply`:

```bash
python3 migration/scripts/create_issues.py \
  --graph migration/issues/graph.json --drafts migration/issues/drafts --screenshots screenshots --apply
```

---
# Phase B — Resolve the issues (ticket-driven; one issue at a time)

Each ticket is solved with `/migration-resolve <issue#>`: it checks the ticket's dependencies, does
the work described IN THE TICKET (no migration/ jsons), commits, and **closes the issue**. If
dependencies aren't met it tells you which. Order: foundation → all ports → views.

In Phase B you never hunt for issue numbers: ask what's ready, then resolve it.
`resolve.py --ready` lists open tickets whose dependencies are all closed (epics excluded).

## 8. Foundation — scaffold migration/vaadin (once; JSF project untouched)

List the foundation ticket and take its `number` (it's the only entry — everything else is blocked
by it):

```bash
python3 migration/scripts/resolve.py --ready foundation
```

Resolve it with that number — this scaffolds the Vaadin shell (equivalent to
`/vaadin-scaffold migration/manifest.json`):

```
/migration-resolve <foundation#>
```

## 9. Port phase — resolve every [port] ticket (entire service layer, before any UI)

List the port tickets whose deps are met — it also prints a ready-to-paste
`/migration-resolve <ids…>` line (to stderr):

```bash
python3 migration/scripts/resolve.py --ready port
```

Resolve them all in one batch (ports each feature to jakarta+Spring, compiles, tests, closes — one
commit per issue):

```
/migration-resolve <id1> <id2> …
```

## 10. View phase — resolve view tickets (TDD)

List all remaining ready tickets, dependency-ordered (login first, then dashboard, lists,
forms/details, …) — it prints a ready-to-paste `/migration-resolve <ids…>` line too:

```bash
python3 migration/scripts/resolve.py --ready
```

Resolve them (TDD-implements each view from the ticket spec, verifies, closes — one commit per
issue). For a big list, do it in a few smaller batches (e.g. per module) to keep each run lean, and
re-run `--ready` between batches as dependencies close:

```
/migration-resolve <id1> <id2> …
```

## 11. Run target (alongside JSF, same DB, for comparison)

Starts on `http://localhost:8090`. JSF stays on docker (`:8443`); both hit the same Postgres. The
existing project is untouched.

```bash
cd migration/vaadin && ./mvnw spring-boot:run
```

---
## Order

```
Phase A:  1 → 2 → 3 → 4 → 5 → 6 (architecture) → 7 (create issues)   (once, all views)
Phase B:  8 (foundation) → 9 (all ports) → 10 (views) → 11 (run)
          each via /migration-resolve <issue#> — it gates on deps and closes the ticket
```

## Dependency rules (issue/implementation order)

```
foundation        blocks all
[port] <feature>  blocks that feature's list/form/detail views   (service ported before its UI)
login (index)     blocks all secured views
list*             blocks form*/detail* in same folder
dashboard         depends on login
```

---
## FAQ

### A view's spec says "No screenshot" — do I re-run step 5?

No — don't re-run the whole analyze. The spec *content* doesn't depend on the screenshot; only the
baseline link does. First find out whether it's a real gap:

```bash
python3 migration/scripts/check_screenshots.py
```

It checks every **navigable** view in `migration/views.json` for a `screenshots/*/<slug>.png`.

- **"All N navigable views have a baseline — nothing to do."** → the "No screenshot" notes are all
  **non-navigable** views (a `resources/**` composite component or a `<ui:composition>`/dialog
  fragment). Those have no URL and no baseline *by design* — proceed to step 6.
- **It lists missing views** → it also prints ready-to-paste invocations: one `/jsf-screenshot-tour`
  (targeted mode) to capture the missing URLs (source app must be running, creds in the config), then
  one `/jsf-view-analyze <file>` per view to relink the baseline. (Exit code is `1` when anything is
  missing, so you can gate on it.)

During step 5 itself, `/jsf-view-analyze` also recaptures a missing baseline for a navigable view on
the fly (best-effort, if the app is up) — so this is mainly for after-the-fact checking.
