# Architecture decisions catalog (generic JSF → Vaadin)

The full set of cross-cutting decisions for a JSF→Vaadin migration, the options for each, the
**recommended default**, what seeds it from the manifest, and where it's applied. The skill walks
these and writes `migration/architecture.json`. Everything here is generic — applies to any
JSF/PrimeFaces app, not just the sample.

Legend: **applied at** = which later step consumes the decision (`scaffold` = vaadin-scaffold /
foundation ticket; `views` = jsf-view-to-vaadin; `port` = service-layer port).

---

## 1. Authentication  → applied at: scaffold, port
The biggest fork. JSF apps usually have container/Shiro/Spring-Security **form login** over a DB user
table.

- `mechanism`: **`form-db`** (recommended when legacy is form login over a user table) · `oauth2-oidc`
  · `saml-sso` · `ldap` · `container-jaas` · `custom`
- `provider`: for oauth2/saml — e.g. `keycloak` · `google` · `azure-ad` · `okta` · `auth0` (else null)
- `reuseUserTable`: **true** for `form-db` — authenticate against the existing users/roles tables
- `passwordEncoder`: match the legacy hash so existing passwords keep working — e.g. `bcrypt` ·
  `shiro-sha256` (legacy Shiro) · `pbkdf2` · `argon2` · `noop` (dev only). *Seed from the detected
  security lib.*
- `rememberMe`: true/false · `sessionTimeoutMin`: **30** · `mfa`: **false**
- Maps to: Spring Security config in the scaffold (`SecurityConfig` + `VaadinSecurityConfigurer`),
  the login view, and the user/auth service port.

## 2. Authorization  → applied at: scaffold, views
- `model`: **`role-based`** · `permission-based` (choose what the legacy used — Shiro is usually
  permission-based)
- `legacyMapping`: how legacy authorities map to Spring — e.g. `shiro-perms→authorities` (1:1 string)
- `defaultPolicy`: **`deny-by-default`** (every route requires an authority unless `@AnonymousAllowed`)
- `annotationStyle`: **`@RolesAllowed`** · `@PermitAll`/`@AnonymousAllowed` for public/error views
- Maps to: per-view security annotations (from each ticket's `permissions`) + menu gating.

## 3. Theme & branding  → applied at: scaffold, views
- `base`: **`lumo`** (Vaadin's built-in)
- `variant`: **`light`** · `dark` · `both-with-toggle`
- `primaryColor`, `font`, `density`: **`default`** · `compact` (compact suits data-dense business apps)
- `appName`, `logo` (path or `none`), `favicon`
- `approximateLegacyTheme`: false — if true, tune Lumo to resemble the old PrimeFaces theme
- Maps to: the generated theme (`frontend/themes/<app>/`), `MainLayout` header/logo, `@PageTitle`.

## 4. Layout & navigation  → applied at: scaffold
- `shell`: **`app-layout-side-nav`** (drawer) · `app-layout-tabs` (top tabs)
- `navSource`: **`derive-from-views`** (group routes by module from the inventory) · `mirror-jsf-menu`
- `userMenu`: **`drawer-footer`** · `top-right` (avatar + logout)
- `breadcrumbs`: true/false (many JSF/PrimeFaces apps had them)
- `responsive`: **true** (drawer collapses on mobile)
- Maps to: `MainLayout` (`AppLayout` + `SideNav`/`Tabs`), nav entries gated by authority.

## 5. Localization (i18n)  → applied at: scaffold, views
- `locales`: list — **seed from the detected `*_xx_YY.properties` bundles** (e.g. `["en_US","pt_BR"]`)
- `default`: **the app's current default locale**
- `languageSwitcher`: true/false · `dateNumberFormats`: **`locale-driven`**
- Maps to: `I18NProvider`/`MessageSource` over the **reused** bundles; switcher in `MainLayout`.

## 6. Persistence & data  → applied at: scaffold, port
- `schema`: **`reuse-existing`** (both apps share the DB — the strangler-fig comparison)
- `flyway`: **`baseline-existing`** (baseline the live schema, don't recreate) · `validate-only` · `off`
- `auditing` (Envers): **`keep`** if detected · `drop`
- `optimisticLocking`: **`keep`** (`@Version` columns) · `none`
- `idGeneration`, `connectionPool` (**HikariCP** default)
- Maps to: `application.properties` (datasource, Flyway, Hibernate ddl-auto=none, Envers), copied JPA.

## 7. Real-time / push  → applied at: scaffold, views
- `push`: **false** unless the app has live dashboards/notifications · `true`
- `pushFor`: views needing it (e.g. `["dashboard"]`) · `transport`: `websocket`
- Maps to: `@Push` on the shell, `UI.access(...)` in the affected views.

## 8. Error handling  → applied at: scaffold, views
- `globalErrorView`: **true** (`HasErrorParameter` for 404/500; map the JSF `error/*.xhtml`)
- `notificationPosition`: **`bottom-start`** · `notificationDurationMs`: **3000**
- `exceptionStrategy`: **`service-exception→notification`** (map domain exceptions to user messages)
- Maps to: error views, the conventions' `Notification` usage.

## 9. Files & reports  → applied at: scaffold, views
- `uploads`: **`vaadin-upload`** (replace `p:fileUpload`) · `downloads`: **`stream-resource`**
- `reports`: how to handle JSF print/report views (`*Print.xhtml`, statistics) —
  **`keep-jasper`** (reuse existing JasperReports, stream the PDF) · `server-pdf` (generate) ·
  `vaadin-charts` (for statistics dashboards) · `none`
- Maps to: download/print buttons in the relevant views; the service port if reporting is server-side.

## 10. Build & packaging  → applied at: scaffold
- `java`: **seed from target** (21) · `packaging`: **`jar`** (Spring Boot embedded) · `war`
- `productionModeProfile`: **true** (a `production` Maven profile for the frontend prod bundle)
- `docker`: true/false · `hilla`: **`removed`** (Flow-only)
- Maps to: `pom.xml` profiles, `application.properties`, optional `Dockerfile`.

## 11. Testing  → applied at: scaffold, views
- `flowBrowserless`: **true** (`browserless-test-junit6` — the conventions' TDD harness)
- `testbench`: **false** (commercial end-to-end; enable only if licensed)
- `coverageGate`: optional (e.g. JaCoCo threshold)
- Maps to: test deps in `pom.xml`; the view/presenter test split in the conventions.

## 12. Observability  → applied at: scaffold
- `actuator`: **true** (health/metrics) · `logging`: **`slf4j`** · `tracing`: optional
- Maps to: `spring-boot-starter-actuator`, logging config.

---

## architecture.json schema (shape)

```json
{
  "authentication": { "mechanism": "form-db", "provider": null, "reuseUserTable": true,
                      "passwordEncoder": "bcrypt", "rememberMe": false, "sessionTimeoutMin": 30,
                      "mfa": false, "rationale": "" },
  "authorization":  { "model": "permission-based", "legacyMapping": "shiro-perms→authorities",
                      "defaultPolicy": "deny-by-default", "annotationStyle": "@RolesAllowed" },
  "theme":          { "base": "lumo", "variant": "light", "primaryColor": null, "font": null,
                      "density": "default", "appName": "", "logo": "none", "favicon": null,
                      "approximateLegacyTheme": false },
  "layout":         { "shell": "app-layout-side-nav", "navSource": "derive-from-views",
                      "userMenu": "drawer-footer", "breadcrumbs": false, "responsive": true },
  "localization":   { "locales": ["en_US"], "default": "en_US", "languageSwitcher": false,
                      "dateNumberFormats": "locale-driven" },
  "persistence":    { "schema": "reuse-existing", "flyway": "baseline-existing", "auditing": "keep",
                      "optimisticLocking": "keep", "connectionPool": "hikaricp" },
  "realtime":       { "push": false, "pushFor": [], "transport": "websocket" },
  "errors":         { "globalErrorView": true, "notificationPosition": "bottom-start",
                      "notificationDurationMs": 3000, "exceptionStrategy": "service-exception→notification" },
  "files":          { "uploads": "vaadin-upload", "downloads": "stream-resource", "reports": "keep-jasper" },
  "build":          { "java": "21", "packaging": "jar", "productionModeProfile": true,
                      "docker": true, "hilla": "removed" },
  "testing":        { "flowBrowserless": true, "testbench": false, "coverageGate": null },
  "observability":  { "actuator": true, "logging": "slf4j", "tracing": null }
}
```

Keep every category present (defaulted is fine) so the foundation ticket is self-contained. The
resolver and scaffold read this file; the foundation ticket carries a rendered summary of it.
