---
name: vaadin-scaffold
description: >
  Scaffold a self-contained Vaadin 25 (Flow) on Spring Boot project under migration/vaadin/ for a JSF
  migration, WITHOUT touching the existing JSF project. Unzips the Vaadin starter, copies the
  framework-neutral service layer (detected from the manifest), wires Postgres/Flyway/Envers and the
  app shell, runs the OpenRewrite mechanical pass, and leaves the judgement port (CDI→Spring,
  Deltaspike→Spring Data, Shiro→Spring Security) to the per-feature port issues. Use after assessing a
  JSF app, to stand up the Vaadin target before migrating views. Consumes migration/manifest.json.
user-invocable: true
argument-hint: "[manifest-path]"
allowed-tools:
  - Read
  - Glob
  - Grep
  - Write
  - Edit
  - Bash
---

# Vaadin Scaffold

Stands up the **Vaadin 25 + Spring Boot target as a self-contained project under `migration/vaadin/`**,
so the JSF app and the Vaadin app build and run **independently** (and can be compared side by side
against the same database). **The existing JSF project is never modified** — the service layer is
*copied*, not moved.

Everything is **app-agnostic**: the base package, the packages to copy, nav, and features all come
from detection / the manifest — nothing is hard-coded to a particular app.

Inputs: `migration/manifest.json` (from `jsf-migration-assess`), **`migration/architecture.json`** (the
decided cross-cutting choices from `migration-architecture` — auth, theme, layout, locales, push,
build; the foundation ticket carries a summary of it), the **curated `vaadin-starter.zip` committed at
the repo root**, the JSF source. If `architecture.json` is absent, fall back to the defaults in
`migration-architecture/references/decisions-catalog.md` and note it. **Never download a starter** — if `vaadin-starter.zip`
is missing, STOP and ask the user to restore it (it is the pinned Vaadin 25 walking skeleton; a
downloaded substitute would drift from the expected structure the scaffold patches).
Target is fixed: **Vaadin 25 + Spring Boot 4 + Java 21 + Jakarta EE 11 + Flow**. Read the reference:
`references/service-port-playbook.md`.

## Workflow

1. **Scaffold the module** (deterministic — use the helper):
   ```bash
   python3 migration/scripts/setup_vaadin_module.py \
     --manifest migration/manifest.json --starter vaadin-starter.zip --out migration/vaadin
   ```
   This unzips the starter into `migration/vaadin/`, **copies** the manifest's
   `source.retainPackages` (service/business/data layer) preserving package paths, copies the i18n
   `.properties` bundles + Flyway migrations, patches `application.properties`
   (`vaadin.allowed-packages` += detected `basePackage`, Postgres datasource, Flyway, Envers,
   `ddl-auto=none`, `server.port=8090`), and removes the Hilla starter (Flow-only). It **refuses** to
   write outside `migration/` or into the JSF `src/`.
2. **App shell** — build per `architecture.json` (write into `migration/vaadin/`):
   - `MainLayout`: `AppLayout` + `SideNav` *or* top `Tabs` per `layout.shell`; nav from the view
     inventory grouped by module, gated by authorities; user menu/logout per `layout.userMenu`;
     breadcrumbs if `layout.breadcrumbs`.
   - Register an empty `@Route` per view (routes from the specs' proposed routes) so navigation exists.
   - i18n: a Spring `MessageSource` + Vaadin `I18NProvider` over the copied bundles, for
     `localization.locales`/`default`; language switcher if `localization.languageSwitcher`.
   - Theme per `theme.*`: Lumo `variant`, `density`, `primaryColor`/`font`, `appName`/`logo`/`favicon`.
   - Login view per `authentication.mechanism` (form login skeleton, or OAuth2/OIDC/SAML redirect).

3. **Security skeleton** — per `authentication`/`authorization`: `VaadinSecurityConfigurer` plus, for
   `form-db`, a `UserDetailsService` loading users/authorities from the ported entities with a
   `PasswordEncoder` matching `authentication.passwordEncoder` so current passwords work; for
   `oauth2-oidc`/`saml-sso`/`ldap`, the corresponding Spring Security client config for the chosen
   `provider`. Default policy and annotation style per `authorization`. (The full per-entity port
   happens in the port issues.)

4. **The port is NOT done here.** javax→jakarta, CDI→Spring, Deltaspike→Spring Data, and the
   per-entity security wiring are tracked as the **per-feature port issues** (`jsf-migration-issues` →
   `[port] <feature>` issues), each rewriting one feature (logic unchanged). The scaffolded module
   will not fully compile until those are worked — expected mid-migration.

5. **Run independently:** once enough ports are done,
   `cd migration/vaadin && ./mvnw spring-boot:run` → `http://localhost:8090`, pointed at the **same
   Postgres** as the JSF app (`docker/`). Both apps run at once for comparison.

## Output
- `migration/vaadin/` — a self-contained Vaadin 25 + Spring Boot project (copied service layer, app
  shell, security/i18n skeleton, empty routes). The JSF project is unchanged.

## Notes
- **Agnostic & safe:** driven by `manifest.source.{basePackage,retainPackages}`; the helper refuses to
  touch anything outside `migration/`.
- Drive the bulk edits with OpenRewrite; reserve reasoning for the port issues.
- Both apps share only the **database**, which is what enables running and comparing them in parallel.
