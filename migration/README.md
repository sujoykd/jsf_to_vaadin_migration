# JSF 2 → Vaadin 25 migration playbook

A generic, repeatable process (and Claude Code skill suite) for migrating a JSF 2 / PrimeFaces
application to **Vaadin 25 Flow on Spring Boot**, **retaining the service/business/data layers** and
rewriting only the UI. web-budget is the reference app, but the skills are written to work on any
JSF 2 app.

For the exact, copy-pasteable commands see **[MIGRATION.md](MIGRATION.md)**. This file is the
overview; that one is the runbook.

## Principles

- **Behavior-spec driven.** Each view is first distilled into a machine-readable *behavior spec*
  (visual half = screenshots; behavioral half = static analysis of the xhtml + backing bean). Views
  are implemented from the spec embedded in their ticket — not by eyeballing screenshots.
- **Retain logic, swap plumbing (re-host in Spring, in-process).** The business layer's *content* is
  kept; its *infrastructure* (namespace, DI, persistence, transactions, security) is ported to the
  Spring stack Vaadin runs on. No new network boundary — Vaadin views call the ported services as
  ordinary Spring beans.
- **Strangler-fig.** Old JSF (untouched) and new Vaadin (copied, ported service layer under
  `migration/vaadin/`) run independently against the **same database**, so the two can be compared
  screen-for-screen until JSF is retired.
- **Decide the architecture up front.** App-wide choices (auth, theme, layout, i18n, …) are made once
  and baked into the foundation, not improvised per view.
- **Pick components by type; verify in a real browser.** A field's Vaadin component is chosen from its
  entity Java type (not the legacy JSF tag), and a view ticket isn't closed until its whole flow is
  driven in an actual browser — passing unit tests is necessary, not sufficient.
- **Ticket-driven & ticket-only.** Once issues exist, the migration runs issue-by-issue; the resolver
  works **only from the ticket** (tracker body + labels + native dependencies), never from local
  `migration/` json.
- **Target is fixed:** Vaadin 25 + Spring Boot 4 + Java 21 + Jakarta EE 11 + Flow. (Vaadin 24 is also
  Jakarta-based, so a `javax` source needs the jakarta flip either way — land on the newest baseline.)

## The skill suite

| Skill | Does | Output |
|-------|------|--------|
| `jsf-migration-assess` | Detect stack, inventory views, size effort, list conversion tasks | `migration/manifest.json` |
| `jsf-screenshot-tour` | Capture a visual baseline of every view (recaptures missing navigable views on demand) | `screenshots/<stamp>/` |
| `jsf-view-analyze` | Distill each view into a behavior spec (component chosen by entity type) | `migration/specs/<view>.{json,md}` |
| `migration-architecture` | Interview the user on cross-cutting choices (auth, theme, layout, i18n, persistence, push, build, …) | `migration/architecture.json` |
| `jsf-migration-issues` | Build the dependency graph and create tracker issues (per view + epics + foundation + per-feature ports) with screenshot, spec, acceptance criteria, dependencies | `migration/issues/graph.json` + drafts; live issues on `--apply` |
| `vaadin-scaffold` | Stand up a self-contained Vaadin 25 app per `architecture.json` (copies service layer; JSF untouched) | `migration/vaadin/` |
| `jsf-view-to-vaadin` | Implement a Vaadin Flow view from its spec (Presenter/MVP, Binder-first, TDD) | Vaadin views |
| `vaadin-migration-verify` | Drive the new view's whole flow in a browser + compare to the JSF baseline | `migration/specs/<view>.verify.md` |
| `migration-resolve` | Resolve one (or several) tickets: gate on deps, do the work *from the ticket* (scaffold / port / view), browser-verify, commit, close | closed issues + committed code |

Helper scripts live in `migration/scripts/` (pure stdlib): `detect_stack.py`, `enumerate_views.py`,
`resolve_views.py`, `build_issue_graph.py`, the tracker dispatchers `create_issues.py` / `resolve.py`
(select the backend via `TRACKER`), and small deterministic lookups (`analyze_entity.py`,
`resolve_i18n.py`, `_slug.py`, `check_screenshots.py`, `forgejo_reset.py`).

## Process: Phase A (plan) → Phase B (resolve)

```
Phase A — once, all views
  1. jsf-migration-assess        -> manifest.json
  2. jsf-screenshot-tour         -> screenshots/<stamp>/
  3. enumerate_views.py          -> views.json (inventory)
  4. jsf-view-analyze            -> specs/<view>.{json,md}
  5. migration-architecture      -> architecture.json   (auth/theme/layout/i18n/…)
  6. jsf-migration-issues        -> graph.json + tracker issues  (embeds specs + architecture)

Phase B — ticket-driven, via migration-resolve <issue#>
  7. foundation ticket           -> scaffold migration/vaadin per architecture.json
  8. all [port] tickets          -> port the ENTIRE service layer to Spring (before any UI)
  9. view tickets                -> implement + browser-verify each view (TDD)
 10. run both apps on the same DB and compare
```

In Phase B you never hunt for issue numbers: `resolve.py --ready [kind]` lists the tickets whose
dependencies are all closed and prints a ready-to-paste `/migration-resolve <ids…>` line.
`migration-resolve` accepts multiple ids and loops them (one commit per issue).

## Dependency rules (issue / implementation order)

```
foundation        blocks everything
[port] <feature>  blocks that feature's list/form/detail views  (service ported before its UI)
login (index)     blocks all secured views
list*             blocks form*/detail* in the same folder
dashboard         depends on login
```

Epics are **grouping only** (parent + task list), never blockers — a child blocked by its own epic
would deadlock (the epic closes only after its children do). Each issue carries a deterministic
`migration-id` UUID, so dedupe is tracker-sourced: deleting issues and re-creating is clean.

## Trackers

Issue creation and resolution are tracker-agnostic, dispatched by the `TRACKER` config value. Three
adapters ship, all live-verified: **Forgejo/Gitea** (native dependencies + asset upload), **Vikunja**
(task relations + attachments), **OpenProject** (work-package relations + hierarchy). GitHub/GitLab
adapter notes are in `.claude/skills/jsf-migration-issues/references/tracker-adapters.md`.

## Artifacts layout

```
migration/
  manifest.json              # assessment (stack, inventory, conversion tasks)
  architecture.json          # decided cross-cutting choices (auth/theme/layout/…)
  views.json                 # view inventory (enumerate_views.py)
  views-resolved.json        # views mapped to xhtml + backing-bean files
  specs/<view>.json|.md      # behavior specs (the IR)
  specs/<view>.verify.md     # parity reports
  issues/graph.json + drafts # issue graph + draft bodies
  vaadin/                    # the new Vaadin 25 app (copied, ported service layer)
screenshots/<stamp>/         # JSF baselines + Vaadin captures   (gitignored)
```

`migration/migration.config.json` (gitignored) holds all config — URLs, repo/project, tokens, DB,
credentials, `TRACKER` — read by every script via `scripts/_env.py`. Per-tracker `state.*.json` files
are gitignored runtime caches (not authoritative; dedupe is UUID-based).

## Supporting references

- Behavior-spec schema + component mapping (type-driven): `.claude/skills/jsf-view-analyze/references/spec-schema.md`
- View coding conventions (Presenter/MVP, Binder-first, browser verification): `.claude/skills/jsf-view-to-vaadin/references/coding-conventions.md`
- Architecture decision catalog: `.claude/skills/migration-architecture/references/decisions-catalog.md`
- Service-layer port playbook (javax→jakarta, CDI→Spring, Deltaspike→Spring Data, Shiro→Spring
  Security, i18n): `.claude/skills/vaadin-scaffold/references/service-port-playbook.md`
- Tracker adapters (Forgejo/Vikunja/OpenProject + GitHub/GitLab notes): `.claude/skills/jsf-migration-issues/references/tracker-adapters.md`

## Tooling

The screenshot/verify and browser-verification steps drive a browser via the **Playwright MCP**
provided by the project devcontainer (`.devcontainer/` + `.mcp.json`), which uses bundled Chromium and
handles the self-signed dev cert. The assess/analyze/issue steps use stdlib Python helpers
(`scripts/`) — no extra setup. The devcontainer also installs JDK 21 + Maven (via SDKMAN) so the
generated Vaadin module builds.
