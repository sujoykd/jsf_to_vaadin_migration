---
name: vaadin-migration-verify
description: >
  Verify a migrated Vaadin Flow view against its JSF baseline for visual and behavioral parity.
  Screenshots the new Vaadin view, compares it to the baseline JSF screenshot, and checks it against
  the behavior spec's checklist (fields, columns, actions/service calls, validations, navigation,
  permissions), then reports gaps to feed back into implementation. Use after migrating a view with
  jsf-view-to-vaadin to confirm it matches the original.
user-invocable: true
argument-hint: "<view-name-or-spec-path> [vaadin-base-url]"
allowed-tools:
  - Read
  - Glob
  - Grep
  - Write
  - Bash
---

# Vaadin Migration Verify

Fifth step. Checks that a migrated view reproduces the original's behavior, and produces a gap report
that loops back into `jsf-view-to-vaadin`. Two dimensions: **visual** (screenshot vs baseline) and
**behavioral** (against the spec checklist).

`$ARGUMENTS`: the view name / spec path, and optionally the running Vaadin app's base URL.

## Workflow

1. **Load the spec** (`migration/specs/<view>.json`) and the **baseline** JSF screenshot it
   references (under `screenshots/`). These define expected behavior + appearance.

2. **Capture the new Vaadin view.** Drive the running Vaadin app with the browser MCP backend (the
   Playwright MCP from the devcontainer — see `../jsf-screenshot-tour/SKILL.md` for tooling and the
   self-signed-cert note). Log in if needed, navigate to the view's `@Route`, and screenshot it to
   `screenshots/<stamp>/vaadin__<slug>.png`.

3. **Behavioral checklist** — walk the spec and verify each item against the running view (DOM via
   the browser MCP) and/or the implementation source. Cover, without skipping any:
   - every `form.fields` entry is present with the **right component for its type** — actively check
     this: a date property must be a `DatePicker` (not a `TextField`), money a `BigDecimalField`, an
     enum/reference a `ComboBox`, a boolean a `Checkbox`. Flag any field whose component doesn't match
     its Java type or its label/semantics (e.g. an "expiration date" rendered as text);
   - every `list.columns` entry is present; lazy paging + sort/filter work;
   - every `action` is present and wired to the correct `serviceCall`, with the right post-action
     navigation (`then`);
   - dialogs exist and confirm/cancel behave;
   - permissions gate the view + menu item as specified;
   - labels resolve via i18n (correct language).

3b. **Drive the whole flow in the browser.** Don't trust browserless tests alone — interact with the
    live view via the Playwright MCP and exercise **everything it does**, end to end (the same paths
    the original JSF view supported), by `kind`:
    - **list** → loads with data; pagination, sorting, filtering work; each row action and the global
      Add navigate to the correct route.
    - **form** → both create and edit; **submit empty/invalid** (blank required, bad date,
      out-of-range, malformed email) → save **blocked** with a **clear error next to each offending
      field** (not a stack trace, raw key, or silent failure — screenshot it); each field enforces its
      type (a `DatePicker` rejects free text, `@Size` max enforced); **submit valid** → saves + success
      `Notification` + navigates per `then`; Back/Cancel discards.
    - **detail** → renders the loaded record; its actions (edit/delete/print/…) work.
    - **cross-cutting** → confirm-dialogs confirm *and* cancel; notifications appear; navigation
      between views works; the view + its menu entry are permission-gated.
    Record anything broken, any validation that doesn't fire or shows an unclear message, and any
    field whose component accepts input its type shouldn't — these are gaps, not passes.

4. **Visual comparison.** Compare the new screenshot to the baseline for layout/labels/columns.
   Note intended differences (Vaadin Lumo styling ≠ PrimeFaces theme) vs real regressions (missing
   field, wrong column, absent button).

5. **Write the gap report** `migration/specs/<view>.verify.md`: a checklist table (item → present?
   → notes), the two screenshots side by side (paths), and a prioritized list of fixes. State a
   verdict: PASS (parity) or list of gaps.

6. **Loop.** If gaps exist, hand them to `jsf-view-to-vaadin` to fix, then re-run this skill until
   PASS.

## Output
- `migration/specs/<view>.verify.md` — parity checklist, screenshots, verdict, and fix list.

## Notes
- Generic. Visual diffs are advisory — themes differ by design; focus the verdict on behavioral
  parity (fields, actions/service calls, validation, navigation, permissions).
- Reuses the devcontainer's Playwright MCP backend; the same self-signed-cert handling applies.
