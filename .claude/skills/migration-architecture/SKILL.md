---
name: migration-architecture
description: >
  Decide the cross-cutting architecture of the target Vaadin app BEFORE generating issues — the
  decisions that the foundation scaffold bakes in and every view inherits: authentication (form login
  vs OAuth2/OIDC/SAML/LDAP), authorization model, theme & branding, app layout & navigation,
  localization, persistence/Flyway/auditing, server push, error handling, file/report handling, build
  & packaging, testing, and observability. Interviews the user and writes migration/architecture.json,
  which build_issue_graph embeds into the foundation ticket. Use right after assessing a JSF app and
  before creating migration issues, or when the user says "decide the architecture / theme / login".
user-invocable: true
argument-hint: "[manifest.json]"
allowed-tools:
  - Read
  - Glob
  - Grep
  - Write
  - Bash
---

# Migration Architecture

The **decisions step** of Phase A. These are the app-wide, decide-once choices that the foundation
scaffold builds in and every view inherits (security annotations, theme, layout, i18n). Capturing
them explicitly — instead of letting the scaffold guess — makes the migration intentional and lets the
**ticket-only resolver** scaffold exactly what was chosen: the decisions are written to
`migration/architecture.json` and `build_issue_graph.py` embeds them into the **foundation ticket**.

Generic by design — works for any JSF→Vaadin migration, not just the sample app. Read the full option
set and defaults in **`references/decisions-catalog.md`**.

`$ARGUMENTS`: an optional path to `migration/manifest.json` (default that). The manifest's detected
stack (auth=Shiro, persistence=Hibernate/Envers, DB, i18n bundles, etc.) **seeds the defaults**, so
most answers are a confirm.

## Workflow

1. **Load context.** Read `migration/manifest.json` (detected stack) and, if present,
   `migration/views.json` (to note things like multiple locales, file-upload/print views, dashboards
   that hint at push). Use these to propose smart defaults — don't ask what you can infer.

2. **Interview by category** (use `AskUserQuestion`; one question per decision or small group, each
   with a recommended default first). Cover every category in `references/decisions-catalog.md`:
   authentication, authorization, theme & branding, layout & navigation, localization, persistence,
   real-time/push, error handling, files & reports, build & packaging, testing, observability.
   - Pre-fill the **Recommended** option from the manifest (e.g. Shiro form-login over a DB user table
     → default authentication = `form-db` with "reuse the existing user table"; offer OAuth2/OIDC,
     SAML/SSO, LDAP as alternatives).
   - Skip a category only if it's clearly N/A for this app (say so), and record the default.
   - Keep it tight — batch related decisions; don't interrogate. The user can accept defaults wholesale.

3. **Write `migration/architecture.json`** conforming to the schema in the catalog. Include every
   category (even defaulted ones) so the foundation ticket is complete and self-contained. Record a
   one-line `rationale` per non-default choice.

4. **Summarize** the decisions back to the user (a short table), and tell them the next step:
   `build_issue_graph.py` will embed this into the foundation ticket, so run/refresh issue creation
   (Phase A step "Create tracker issues") after this.

## Output
- `migration/architecture.json` — the decided target architecture (tracked in git; it's a project
  decision, not a secret). Consumed by `build_issue_graph.py` (→ foundation ticket) and
  `vaadin-scaffold` (→ shell, security, theme, build).

## Notes
- **Decide here, apply at the foundation.** This skill only records decisions; the work happens when
  `/migration-resolve <foundation#>` (i.e. `vaadin-scaffold`) runs and reads `architecture.json`.
- **Re-runnable.** Re-invoke to revise; it rewrites `architecture.json`. If issues already exist,
  re-run issue creation so the foundation ticket body reflects the change.
- **Generic.** No app-specific assumptions — the catalog covers the common JSF→Vaadin concerns; seed
  from the manifest and let the user choose.
