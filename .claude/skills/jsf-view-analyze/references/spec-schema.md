# Behavior Spec schema

The **behavior spec** is the intermediate representation (IR) that decouples *understanding* a JSF
view from *implementing* its Vaadin replacement. `jsf-view-analyze` produces one spec per view;
`jsf-view-to-vaadin` and `vaadin-migration-verify` consume it. One JSON file (machine-readable) plus
an optional Markdown rendering (human review), stored as `migration/specs/<view>.json` / `.md`.

A spec must describe a view completely enough to re-implement it **without the JSF runtime** — i.e.
from the spec + the baseline screenshot + the original source, never from guessing.

## JSON shape

```jsonc
{
  "view": "secured/registration/wallet/formWallet.xhtml", // source path (id)
  "route": "wallets/edit",            // proposed Vaadin @Route (see naming below)
  "title": "Wallet",                  // window/heading title (resolve i18n keys to text)
  "kind": "form",                     // list | form | detail | dashboard | login | error | other
  "module": "registration",           // top path segment after secured/ (for menu grouping)
  "backingBeans": ["walletBean"],     // CDI beans behind the view
  "screenshot": "screenshots/2026-.../secured__registration__wallet__formWallet.png",

  // LIST views ---------------------------------------------------------------
  "list": {
    "dataBinding": "#{walletBean.dataModel}",  // lazy data source
    "lazy": true,
    "columns": [
      { "header": "Name", "value": "name", "sortable": true, "filterable": true }
    ],
    "rowActions": [                    // per-row buttons/links
      { "label": "Edit",   "target": "formWallet.xhtml?id=", "permission": "WALLET_UPDATE" },
      { "label": "Detail", "target": "detailWallet.xhtml?id=", "permission": "WALLET_DETAIL" }
    ],
    "globalActions": [ { "label": "Add", "target": "formWallet.xhtml" } ]
  },

  // FORM views ---------------------------------------------------------------
  "form": {
    "modelType": "br.com.webbudget...Wallet",   // entity/DTO the form binds to
    "modelBinding": "#{walletBean.value}",
    "fields": [
      {
        "label": "Name",                 // resolved text (note the i18n key in i18nKeys)
        "binding": "name",               // property path on the model
        "component": "TextField",        // proposed Vaadin component (see mapping table)
        "type": "String",
        "required": true,
        "validation": ["@NotBlank", "max=45"],  // from bean validation on the entity + xhtml
        "converter": null,               // e.g. BigDecimal/date converter if any
        "readOnly": false,
        "options": null                  // for selects: source of items (enum/service)
      }
    ]
  },

  // ACTIONS (form/detail submit + navigation) --------------------------------
  "actions": [
    { "label": "Save",   "serviceCall": "walletService.save(value)",    "then": "list",  "permission": "WALLET_ADD" },
    { "label": "Update", "serviceCall": "walletService.save(value)",    "then": "list",  "permission": "WALLET_UPDATE" },
    { "label": "Back",   "serviceCall": null,                           "then": "list" }
  ],

  // DIALOGS ------------------------------------------------------------------
  "dialogs": [
    { "name": "confirmDelete", "header": "Confirm", "fragment": "dialogConfirm.xhtml",
      "confirmAction": "walletService.delete(value)" }
  ],

  // SERVICE SURFACE (what the new view must call) ----------------------------
  "services": [
    { "type": "WalletService", "methods": ["save(Wallet)", "delete(Wallet)"] },
    { "type": "WalletRepository", "methods": ["findById", "findAll(lazy)"] }
  ],

  // CROSS-CUTTING ------------------------------------------------------------
  "permissions": ["WALLET_ACCESS"],   // page-level permission (Shiro path -> Spring authority)
  "i18nKeys": ["wallet.name", "menu.save", "menu.back"],  // keys to reuse from .properties
  "navigation": { "in": ["listWallets.xhtml"], "out": ["listWallets.xhtml"] },
  "notes": "Anything non-obvious: conditional rendering, viewState=ADDING/EDITING modes, etc."
}
```

## Field-by-field rules

- **route** — derive a clean Vaadin route from the path: drop `secured/`, drop `.xhtml`, kebab-case,
  collapse `list*/form*/detail*` into `<entity>` + `/edit` + `/<id>`. Example:
  `secured/registration/wallet/listWallets.xhtml` → `wallets`;
  `formWallet.xhtml` → `wallets/edit`; `detailWallet.xhtml?id=` → `wallets/:id`.
- **title** — resolve i18n keys to actual English text (look them up in the `.properties`), but also
  keep the keys in `i18nKeys` so the Vaadin app can reuse the bundles.
- **fields[].validation** — merge two sources: Bean Validation annotations on the model entity
  (`@NotBlank`, `@Size`, `@NotNull`, `@Email`, …) **and** any `required`/validators in the xhtml.
- **fields[].component** — choose by the **entity property's Java type first** (the strongest signal —
  see the type-driven table below), then fall back to the JSF tag. Legacy JSF often uses a plain
  `inputText` for dates/numbers/enums, so a literal tag-only mapping is wrong. **Sanity-check the field
  name/label too** — e.g. a field named `expiration`/`*Date`/`day`/`month`/`year` is a date, `email`
  is an `EmailField`, even if the source rendered it as text. Record the chosen component; the
  implementer may refine.
- **actions[].serviceCall** — translate the JSF action EL (`#{walletBean.doSave()}`) to the
  underlying service method by reading the backing bean (the bean method body shows the real call,
  e.g. `walletService.save(value)`). This is the most important field — it's how the new UI reaches
  the retained service layer.
- **permissions** — from the Shiro path rules (`PathSecurityConfiguration`) and `rendered=` guards;
  map each to a Spring Security authority for the new app.

## Type-driven component selection (preferred — overrides a too-literal JSF tag)

Pick the component from the entity property's Java type (from `analyze_entity.py`). This is what
catches mismatches like an "expiration date" rendered as a `TextField` in the old JSF form.

| Java type (entity property) | Vaadin Flow component |
|-----------------------------|-----------------------|
| `LocalDate` / `java.sql.Date` | `DatePicker` |
| `LocalDateTime` / `Date` / `Instant` | `DateTimePicker` |
| `LocalTime` | `TimePicker` |
| `BigDecimal` / money | `BigDecimalField` |
| `Integer` / `Long` / `int` / `long` | `IntegerField` (or `NumberField`) |
| `Double` / `Float` / `BigInteger` | `NumberField` |
| `boolean` / `Boolean` | `Checkbox` |
| `enum` | `ComboBox<E>` / `Select<E>` (items = enum constants) |
| `@ManyToOne` / entity reference | `ComboBox<T>` (items from the related service) |
| `String` (short) | `TextField` (or `EmailField`/`PasswordField` by name/validation) |
| `String` (long / `@Lob` / large `@Size`) | `TextArea` |

When the type-driven choice and the JSF tag disagree, **trust the type** (and the field semantics) —
the legacy markup is the least reliable signal.

## JSF/PrimeFaces → Vaadin Flow component mapping (fallback when the type is ambiguous)

| JSF / PrimeFaces | Vaadin Flow |
|------------------|-------------|
| `p:inputText`, `h:inputText` | `TextField` |
| `p:inputTextarea` | `TextArea` |
| `p:password` | `PasswordField` |
| `p:inputNumber`, `p:spinner` | `NumberField` / `BigDecimalField` |
| `p:selectOneMenu`, `h:selectOneMenu` | `ComboBox` / `Select` |
| `p:selectBooleanCheckbox` | `Checkbox` |
| `p:selectManyCheckbox` | `CheckboxGroup` |
| `p:calendar`, `p:datePicker` | `DatePicker` / `DateTimePicker` |
| `p:autoComplete` | `ComboBox` (lazy items) |
| `p:dataTable` | `Grid` (with `DataProvider` for lazy paging) |
| `p:dialog` | `Dialog` |
| `p:confirmDialog` | `ConfirmDialog` |
| `p:commandButton` / `p:commandLink` | `Button` |
| `p:link` / `p:button` (outcome) | `RouterLink` / `UI.navigate(...)` |
| `p:messages` / growl | `Notification` |
| `p:fileUpload` | `Upload` |
| menu (`mainMenu.xhtml`) | `AppLayout` + `SideNav` / `Tabs` |
| form layout (`p:panelGrid`) | `FormLayout` |

## Markdown rendering (optional, for review)

A short human-readable summary alongside the JSON: title, kind, a table of fields
(label / component / required / validation), a list of actions (label → service call → next view),
permissions, and an embedded link to the baseline screenshot. Keep it scannable.
