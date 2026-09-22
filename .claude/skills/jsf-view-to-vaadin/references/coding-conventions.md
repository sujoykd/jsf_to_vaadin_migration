# Vaadin 25 view coding conventions

The conventions every migrated view must follow, so 53 views come out uniform. Grounded in current
Vaadin 25 docs (verify specifics with the Vaadin MCP: `search_vaadin_docs`, `get_component_java_api`).
The behavior spec says *what* a view does; this says *how* to build it.

## Architecture — Presenter / MVP

Each view is split into a **passive View** and a **Presenter** that holds the logic. This mirrors the
JSF backing bean (the bean ≈ the presenter), keeps business logic out of the UI, and makes both
unit-testable.

```
<basePackage>/vaadin/<module>/<feature>/
  <Feature>View.java        @Route, builds UI, owns the Binder, wires events → presenter
  <Feature>Presenter.java   @SpringComponent prototype, injects the ported @Service(s)
```

**View (`<Feature>View`)**
- `@Route(value = ..., layout = MainLayout.class)`, `@PageTitle(...)`, security annotation (below).
- Extends a layout component (`Composite<VerticalLayout>` / `Main` / `Div`) — never a raw container
  with logic in it.
- Holds: the UI components, a `BeanValidationBinder<T>` (forms), and `private final <Feature>Presenter
  presenter;` — **constructor-injected**.
- Responsibilities only: construct components, bind them, wire component events to presenter methods,
  render what the presenter returns (Notifications, navigation, grid data). **No service calls, no
  business rules.**

**Presenter (`<Feature>Presenter`)**
- `@SpringComponent` + `@Scope(SCOPE_PROTOTYPE)` (a fresh presenter per view instance).
- **Constructor-injects** the ported `@Service`(s) — never the repository directly from the view.
- Exposes verbs mirroring the old backing bean: `save(T)`, `update(T)`, `delete(T)`, `load(long id)`,
  and the list fetch/count callbacks. Methods **return results or throw** (don't hold a back-reference
  to the view — avoids a circular dependency and keeps the presenter trivially testable with a mocked
  service). Navigation *targets* are decided here; the View performs the actual `navigate(...)`.

Example seam (form Save):
```java
// in the View
saveButton.addClickListener(e -> {
    if (binder.writeBeanIfValid(value)) {        // validation stays in the view (Binder)
        presenter.save(value);                   // business call lives in the presenter
        Notification.show(getTranslation("saved"), 3000, Position.BOTTOM_START)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        UI.getCurrent().navigate(ListView.class); // outcome per spec's `then`
    }
});
```

## Components — pick by the property's Java type, not the legacy JSF tag
The single biggest correctness trap. Choose each field's component from the **entity property type**:
`LocalDate`→`DatePicker`, `LocalDateTime`/`Date`→`DateTimePicker`, `LocalTime`→`TimePicker`,
`BigDecimal`→`BigDecimalField`, `Integer`/`Long`→`IntegerField`, `Double`→`NumberField`,
`boolean`→`Checkbox`, `enum`→`ComboBox`/`Select`, `@ManyToOne`→`ComboBox` (items from the related
service), long/`@Lob` `String`→`TextArea`, short `String`→`TextField` (or `EmailField`/`PasswordField`
by name). **Never inherit a too-literal mapping** — legacy JSF often used a plain `inputText` for dates
and numbers; a field like "expiration date" must be a `DatePicker`, not a `TextField`. The right
component is itself a validation layer (a `DatePicker` can't hold "abc").

## State & validation — Binder-first, Signals sparingly

- **Forms:** always `BeanValidationBinder<T>` bound to the spec's `form.modelType`. It auto-applies
  the entity's JSR-303 annotations (`@NotBlank`/`@Size`/…); use `bindInstanceFields(this)` when field
  names match, otherwise explicit `binder.forField(...).withConverter(...).bind("prop")`. Add
  converters where the component type ≠ property type (e.g. `StringToIntegerConverter`;
  `BigDecimalField` binds `BigDecimal` natively, so prefer it for money fields). Commit with
  `writeBeanIfValid` / `writeBean`; never persist on invalid.
- **Validation must be visible.** Every constraint must surface a **clear, field-level error message**
  in the UI (Binder sets the field's error text from the JSR-303 `message`; resolve i18n keys to real
  text — never show a raw `{key}` or a stack trace). Invalid submit is blocked; the user sees *which*
  field is wrong and *why*. This is verified in a **real browser** (see the testing section), not only
  in browserless tests.
- **Signals — only when multiple components in one view must react to shared state** (e.g. a
  master-detail in a single view, or enabling a button from a selection). Then use `ValueSignal<T>`
  and `bindText`/`bindEnabled`/`bindVisible` / `Signal.effect(...)`; read without subscribing via
  `signal.peek()`. The CRUD pattern in these specs is **two routes** (list → form), so the id travels
  in the URL and **no signal is needed** — default to plain fields + Binder + presenter calls. Do not
  introduce Signals for plain CRUD. (Full reference: Vaadin docs `/flow/ui-state`.)

## Lists (Grid)
- `Grid<T>` with columns from `list.columns` (header, `setSortable`, filtering as the JSF table had).
- **Lazy** data via a `CallbackDataProvider` whose fetch/count delegate to the presenter, which calls
  the service's `Page<T> findAll(Specification, Pageable)`. Don't load all rows.
- Row actions (Edit/Detail) and the global Add navigate to the typed routes (below).

## Navigation
- **Typed routes only:** `UI.getCurrent().navigate(FormView.class, new RouteParameters("id", id))`.
  No string-literal route paths. Map each old JSF outcome to the corresponding `@Route` class.

## Security
- Annotate each view from the spec's `permissions`: `@RolesAllowed("<PERMISSION>")`, or `@PermitAll` /
  `@AnonymousAllowed` for public ones (login/error). The `MainLayout` menu item for the view must be
  gated to the same authority so navigation and menu stay consistent.

## Messages & dialogs
- Replace JSF growl/`p:messages` with `Notification` + Lumo theme variants
  (`LUMO_SUCCESS`/`LUMO_ERROR`) — success/error mirroring the old `addInfo`/`addError`.
- Deletes/confirmations → `ConfirmDialog`; other dialogs → `Dialog`, from the spec's `dialogs`.

## i18n
- Resolve labels with `getTranslation(key)` (Vaadin `I18NProvider`) using the spec's `i18nKeys`.
  **Reuse the existing `.properties` bundles** copied into the Vaadin module — don't hardcode strings.

## Styling
- Lumo defaults + `LumoUtility` helper classes for spacing/layout. Avoid custom CSS unless a
  screenshot-parity detail requires it; theme differences from PrimeFaces are expected and acceptable.

## DI & general
- **Constructor injection everywhere** (views, presenters); no field `@Autowired`, no static service
  lookups. `final` collaborators.
- One feature per package; `<Feature>View` / `<Feature>Presenter` naming.
- Keep methods small; the presenter is the only place that talks to services.

## Testing (TDD) follows the split
- **Presenter test** — plain unit test: mock the `@Service`, assert `save/update/delete/load` call the
  exact service method (`verify(walletService).save(...)`) and return/propagate correctly.
- **View test** — browserless Flow test (`browserless-test-junit6`): assert each field's component
  matches its type (a date property is a `DatePicker`, not a `TextField`), the Binder **rejects**
  invalid input, a Save click with valid input triggers the presenter and navigates per `then`. Mock
  the presenter (or use the real one with a `@MockitoBean` service).
- **Browser test (whole flow)** — browserless isn't enough. Drive the running view via the Playwright
  MCP and exercise **everything it does**, end to end: list load + paginate/sort/filter + row
  actions/Add navigation; form create *and* edit with invalid→blocked+clear field error and
  valid→save+notify+navigate; detail render + actions; dialogs confirm/cancel; notifications;
  navigation; permission gating. Walk the same paths the original JSF view supported. This is part of
  resolving a view ticket; `vaadin-migration-verify` does the full pass.
```
