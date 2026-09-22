---
name: jsf-view-analyze
description: >
  Analyze a single JSF 2 / PrimeFaces view (its .xhtml plus backing bean) and distill it into a
  machine-readable "behavior spec" for re-implementation in Vaadin Flow. Extracts fields, bindings,
  validations, actions (and the service methods they call), list columns, dialogs, navigation,
  permissions, and i18n keys. Use when migrating JSF views to Vaadin and you need a precise spec of
  what a view does before implementing it. Produces migration/specs/<view>.json + .md.
user-invocable: true
argument-hint: "[view.xhtml ...]  (one or more; omit to batch-analyze all views)"
allowed-tools:
  - Read
  - Glob
  - Grep
  - Bash
---

# JSF View Analyze

Second step of the pipeline. Turns one JSF view into a **behavior spec** — the IR that lets
`jsf-view-to-vaadin` implement the Vaadin view from a contract instead of from raw markup or a
screenshot guess. Read the schema first: `references/spec-schema.md`.

`$ARGUMENTS`: zero or more target `.xhtml` paths (space-separated). Give **one** to analyze a single
view, **several** to analyze just those (run the per-view workflow for each, in order), or **omit**
to batch-analyze every view. The backing beans are resolved automatically, so you never hand-find
files.

## Resolve the worklist (no manual file hunting)

`resolve_views.py` maps every view to its `.xhtml` path and backing-bean `.java` file(s), so you
don't have to locate them:

```bash
python3 migration/scripts/resolve_views.py <webapp-root> <java-root> \
  > migration/views-resolved.json
```

Each entry: `{view, slug, beans:[{name, file}], unresolved:[…]}`. `unresolved` are usually EL loop
vars (e.g. iteration items), not beans — ignore them.
- **All-views mode** (no args): iterate the whole list and run the per-view steps below for each.
- **Subset mode** (one or more `.xhtml` given): run `resolve_views.py` once, then for **each** path in
  `$ARGUMENTS` look up its entry (by `view`) and run the per-view steps. Do them sequentially and
  report progress (`[n/total] <view>`) so a long list is followable.

## Per-view workflow

Two cheap, exact lookups are scripts (so you don't hand-trawl `.properties` or entity files); the
judgment — reading the bean's control flow, resolving permissions, mapping components — stays here.

1. **First-pass extraction** from the xhtml:
   ```bash
   python3 migration/scripts/extract_bindings.py <view.xhtml>
   ```
   Returns title, backing beans, inputs (+value bindings, labels, required), command actions,
   columns, navigation outcomes, dialogs, i18n keys, and all EL expressions. This is a STARTING
   POINT — it cannot see the backing bean or the entity.

2. **Read the backing bean(s)** — use the `.java` file(s) `resolve_views.py` already mapped for this
   view (no need to search). From the bean determine, for each action EL
   (`#{walletBean.doSave()}`), the **real service call** in the method body
   (e.g. `walletService.save(value)`), the navigation result, the model field (`value`), and any
   view-state modes (ADDING/EDITING/DELETING). This is the most important step — it's how the new UI
   will reach the retained service layer. Read the bean body yourself; method-body control flow is
   not something to regex.

3. **Pull entity validation (script).** For the entity the form binds to, extract Bean-Validation
   annotations, `@Column` constraints and enum options deterministically:
   ```bash
   python3 migration/scripts/analyze_entity.py <Entity.java> [<java-source-root>]
   ```
   Per field: `required`, `maxLength`, `validations`, and resolved enum `options`. Merge into each
   field's `validation`/`type`/`options`.

4. **Resolve i18n keys (script).** Turn the `var:key` references from step 1 into English text from
   the bundles (keep the keys too, so the Vaadin app can reuse them):
   ```bash
   python3 migration/scripts/resolve_i18n.py <i18n-dir> --keys-json <extract-output.json>
   # or pass keys directly:  resolve_i18n.py <i18n-dir> "messages:wallet.form.name" ...
   ```

5. **Resolve permissions** for the view from the security config (Shiro path rules /
   `rendered="#{...isPermitted(...)}"` guards). Config formats vary per app — read the rule yourself.

6. **Map components — by entity type first, JSF tag second.** Use the type-driven table in
   `references/spec-schema.md` (from the `analyze_entity.py` Java type): `LocalDate`→`DatePicker`,
   `BigDecimal`→`BigDecimalField`, `boolean`→`Checkbox`, `enum`/`@ManyToOne`→`ComboBox`, etc. Only
   fall back to the JSF-tag table when the type is ambiguous (e.g. plain `String`). **Don't inherit a
   too-literal legacy mapping** — legacy forms often use `inputText` for dates/numbers; a field named
   `expiration`/`*Date` is a `DatePicker` even if the xhtml rendered it as text. Sanity-check name +
   type together.

7. **Write the spec**: `migration/specs/<slug>.json` (full IR per the schema) and a scannable
   `migration/specs/<slug>.md` (title, kind, field table, actions→service-call list, permissions,
   baseline screenshot — resolved per *Baseline screenshot* below). Propose the Vaadin `@Route`.

## Baseline screenshot (capture if missing)

Each spec links a baseline screenshot. Resolve it per view:

1. Look for `screenshots/*/<slug>.png` (build the name with `migration/scripts/_slug.py`). If present,
   reference it and move on.
2. If absent, decide by **navigability**:
   - **Navigable view** — it appears in `migration/views.json` (the tour's inventory). It has a URL,
     so (re)capture it: invoke `/jsf-screenshot-tour` in **targeted mode** for this view's URL — it
     logs in and writes `<slug>.png`, then reference it. **Best-effort**: if the source app isn't
     running or no browser backend is available, record the screenshot as missing and continue —
     never block the spec on it.
   - **Non-navigable view** — a `resources/**` composite component or a `<ui:composition>`/dialog
     fragment, i.e. *not* in `migration/views.json`. It has no URL and no baseline by design. Set
     `screenshot: null` and note "non-navigable component" in the `.md`. Do **not** attempt capture.

In **batch mode**, collect all missing navigable slugs and capture them in **one** targeted tour pass
rather than launching the browser per view.

## Output
- `migration/specs/<view>.json` and `<view>.md`, conforming to `references/spec-schema.md`.

## Notes
- Generic and **read-only**. Works on any JSF/PrimeFaces view.
- The script is a heuristic; always cross-check against the backing bean and entity — the bean body
  is the source of truth for service calls and navigation.
- Run `jsf-screenshot-tour` first to capture baselines up front; if a navigable view is still missing
  one, this skill recaptures it on demand (see *Baseline screenshot*). Non-navigable composites/
  fragments have no baseline by design.
