---
name: jsf-view-to-vaadin
description: >
  Implement a Vaadin 25 Flow view from a JSF view's behavior spec, test-driven, after verifying its
  service layer is ported. Gates on the feature's service port being done, then writes failing
  browserless Flow tests from the spec and implements the view to green: Grid for lists with lazy
  paging, Binder + Bean Validation for forms, Dialog/ConfirmDialog, Notification, @Route navigation.
  Use when migrating individual JSF views to Vaadin after scaffolding. One view at a time.
user-invocable: true
argument-hint: "<spec-path-or-view-name>"
allowed-tools:
  - Read
  - Glob
  - Grep
  - Write
  - Edit
  - Bash
---

# JSF View → Vaadin

Fourth step. Implements ONE Vaadin Flow view from its behavior spec, **test-driven**, and only after
its **service port is verified done**. Implement from the **spec** (the contract) — use the screenshot
and original xhtml only to resolve layout/visual details the spec doesn't capture. Read
`../jsf-view-analyze/references/spec-schema.md` for the spec, and **`references/coding-conventions.md`
for how to build it** — the Presenter/MVP split, Binder-first (Signals only for shared in-view state),
typed routes, security, i18n, and the testing split. Follow those conventions so every view is uniform.

`$ARGUMENTS`: a spec path (`migration/specs/<view>.json`) or a view name to resolve to one.

## Workflow

1. **Load the spec** and its `.md` summary. If missing, run `jsf-view-analyze` on the view first.
   Open the baseline screenshot (from `screenshots/`) and the original `.xhtml` for visual reference.

2. **GATE — verify the service port is done** (do not build a UI on an unported backend):
   ```bash
   python3 migration/scripts/check_service_port.py \
     --spec migration/specs/<view>.json --vaadin-root migration/vaadin
   ```
   Exit 0 = the spec's services exist in `migration/vaadin` and are Spring-ported (`@Service` /
   `JpaRepository`, no leftover CDI/Deltaspike). **If it exits non-zero, STOP** — the view is
   blocked-by its `[port] <feature>` issue; finish that port first
   (`../vaadin-scaffold/references/service-port-playbook.md`), then retry.

3. **Write the tests FIRST (TDD — red).** Derive tests from the spec's acceptance criteria using the
   starter's **browserless Flow testing** (`browserless-test-junit6`). Test the **Presenter** and the
   **View** separately per the split in `references/coding-conventions.md` (presenter: mock the ported
   service and `verify(...)` the exact call; view: assert components/columns + Binder rejects invalid
   input). Cover, by `kind`:
   - **list** → Grid exposes the spec's columns; the `DataProvider` queries the repository; a row
     action navigates to the form/detail route; the `Add` button navigates to the new-record route.
   - **form** → every `form.fields` entry is present as the mapped component; the `Binder` **rejects**
     invalid input per the spec's validation; **Save** invokes the exact `serviceCall`
     (e.g. `verify(walletService).save(...)`) and navigates per `then`; **Back** navigates without
     saving.
   - **detail** → fields render read-only from a loaded entity (by route id).
   Run them and confirm they **fail** (the view doesn't exist yet).

4. **Implement to make the tests pass (green)** — a **View + Presenter** per
   `references/coding-conventions.md` (presenter holds the service calls + navigation outcomes; the
   view builds UI, owns the Binder, and renders). By `kind`:
   - **list** → a `@Route` view with a `Grid<T>`; columns from `list.columns` (header/sortable/
     filterable); lazy `CallbackDataProvider` delegating to the presenter → repository's
     `Page<T> findAll(Specification, Pageable)`; row actions (`Edit`/`Detail`) and global `Add`
     navigating via **typed routes**. Mirror the data-table filtering the JSF list had.
   - **form** → a `FormLayout` whose component for each `form.fields` entry is chosen **by the field's
     Java type, not the legacy JSF tag** (date→`DatePicker`, datetime→`DateTimePicker`,
     money→`BigDecimalField`, int→`IntegerField`, enum/reference→`ComboBox`, boolean→`Checkbox`; see
     the type-driven table in `spec-schema.md`) — never leave a date/number/enum as a `TextField`
     just because the old form did. A `BeanValidationBinder<T>` bound to `form.modelType` auto-applies
     the entity's JSR-303 constraints; add converters where component type ≠ property type.
     Save/Update/Back call the presenter's method (which runs the spec's `serviceCall`,
     e.g. `walletService.save(value)`), then navigate per `then`.
   - **detail** → read-only rendering of the entity; load by route id parameter via the presenter.
   - **dashboard / other** → compose from the spec's regions.
   Default to Binder + presenter calls; reach for a `Signal` only if multiple components in this one
   view must react to shared state (see the conventions doc).

5. **Cross-cutting (still covered by tests where it matters):**
   - **Messages:** replace JSF growl/`p:messages` with Vaadin `Notification` (success/error per the
     old `addInfo`/`addError` calls).
   - **Dialogs:** `Dialog`/`ConfirmDialog` from the spec's `dialogs`.
   - **i18n:** resolve labels via the Vaadin `I18NProvider`/`MessageSource` using the spec's
     `i18nKeys` (reuse the existing bundles).
   - **Security:** annotate the view (`@RolesAllowed`/`@PermitAll`) from the spec's `permissions`;
     ensure the menu item is gated to match.
   - **Navigation:** `@Route` per the spec; use `RouterLink`/`UI.navigate(...)` for the old outcomes.

6. **Green + refactor:** run the tests until all pass (`cd migration/vaadin && ./mvnw test`), then
   refactor for clarity with the tests as the safety net. Do not move on with failing/!skipped tests.

7. **Browser smoke-test the whole flow:** browserless tests aren't enough — run the app and drive
   **everything the view does** via the Playwright MCP (see `../jsf-screenshot-tour/SKILL.md` for
   tooling), the same paths the original supported. **list** → loads, paginate/sort/filter, row
   actions + Add navigate; **form** → create *and* edit, submit empty/invalid → save **blocked** with
   a **clear field-level error** (no raw key/stack trace) and each field enforces its type, submit
   valid → saves + notifies + navigates, Back discards; **detail** → renders + its actions;
   **cross-cutting** → dialogs confirm/cancel, notifications, navigation, permission gating. Then hand
   to `vaadin-migration-verify` for full screenshot/behavior parity.

## Output
- **Tests first**, then a Vaadin Flow view (and any small supporting classes) in `migration/vaadin/`,
  calling the ported services, registered at the spec's route — all tests green.

## Notes
- **Gate before build:** never implement a view whose service port isn't done (`check_service_port.py`).
  This is the execution-time counterpart to the graph's `view → blocked-by → port` edge.
- **TDD:** tests are written from the spec *before* the view and must fail first, then pass. They are
  the per-view dev loop; `vaadin-migration-verify` is the higher-level screenshot/behavior parity check.
- Generic; one view per invocation for reviewable diffs. Loop back here to close any gaps verify finds.
- Prefer composing Vaadin's built-in components over custom widgets (see the primer's component list).
