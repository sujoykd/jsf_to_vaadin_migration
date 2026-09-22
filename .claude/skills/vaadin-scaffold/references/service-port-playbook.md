# Service-layer port playbook: Java EE / CDI → Spring Boot (for Vaadin 25)

How to re-host a JSF app's **retained service/business/data layer** onto the Spring Boot stack that
Vaadin 25 runs on. The goal is to keep the **business-logic content** intact while swapping the
**plumbing** (namespace, DI, persistence, transactions, security). Done per-feature in the `[port]`
issues — each rewrites one feature's classes (entity/repo/service/logics) in a single pass.

> Principle: a service/logic/entity class keeps its *body*; only annotations, imports, and base
> types change. Diff should be dominated by import lines and annotations, not logic.

## 1. Namespace: javax → jakarta (mandatory for Vaadin 24+)

`javax.*` → `jakarta.*` for: `persistence`, `validation`, `inject`, `enterprise.*`, `transaction`,
`annotation`, `ejb`, `servlet`, `mail` — done as part of rewriting each class for this feature.
`javax.sql`, `javax.crypto`, `javax.net` and other JDK packages stay `javax`.

## 2. Dependency injection: CDI → Spring

| CDI / Java EE | Spring |
|---------------|--------|
| `@ApplicationScoped` on a service | `@Service` (singleton) |
| `@Stateless` / `@Singleton` (EJB) | `@Service` |
| `@RequestScoped` / `@SessionScoped` bean (non-UI) | `@Component` + `@RequestScope`/`@SessionScope` (rare; UI scopes move to Vaadin) |
| `@Dependent` | default prototype `@Component`, or just a plain object |
| field `@Inject` | **constructor injection** (preferred) — final fields + one constructor |
| `@Inject @Qualifier Foo` | `@Qualifier("foo")` or distinct types |
| `@Produces` method | `@Bean` method in a `@Configuration` class |
| `Instance<T>` / `@Any` (plugin lists) | inject `List<T>` or `ObjectProvider<T>` (Spring collects all beans of the type) |
| `Event<T>` + `@Observes` | `ApplicationEventPublisher.publishEvent(..)` + `@EventListener` (or `@TransactionalEventListener`) |
| `@PostConstruct` / `@PreDestroy` | same annotations (now `jakarta.annotation.*`), supported by Spring |

The strategy-pattern `Instance<CardSavingLogic>` → `List<CardSavingLogic>` injected by Spring is the
single most common transform in a JSF app like this; it preserves the plugin design exactly.

## 3. Persistence: Deltaspike Data → Spring Data JPA

| Deltaspike | Spring Data JPA |
|------------|-----------------|
| `@Repository` interface `extends EntityRepository<T,ID>` / custom base | `interface ... extends JpaRepository<T,ID>` (+ `JpaSpecificationExecutor<T>` for criteria) |
| derived finders `findByNameAndActive(...)` | identical method-name syntax — usually copy verbatim |
| `@Query("...")` (Deltaspike) | `@Query("...")` (Spring Data) — JPQL mostly compatible |
| `criteria()` + `getRestrictions(filter)` lazy filtering | a `Specification<T>` builder (or `Querydsl`) |
| `LazyDefaultRepository` paging for `p:dataTable` | `Page<T> findAll(Specification, Pageable)` feeding a Vaadin `Grid` lazy `DataProvider` |
| `EntityManager` direct use | inject `EntityManager` with `@PersistenceContext` (unchanged) |

Entities themselves are nearly untouched: `@Entity`, `@Table`, relations, and **Bean Validation**
annotations carry over (just `javax.validation`→`jakarta.validation`). Hibernate **Envers**
(`@Audited`) continues to work on Hibernate 6.

## 4. Persistence config: persistence.xml/JNDI → Spring Boot

- Drop `persistence.xml` + JTA JNDI datasource. Configure in `application.properties`:
  `spring.datasource.url/username/password`, `spring.jpa.hibernate.ddl-auto`,
  `spring.jpa.properties.hibernate.dialect`, Envers props under
  `spring.jpa.properties.org.hibernate.envers.*`.
- Transactions: container-managed JTA → Spring `@Transactional` (same annotation name after the
  jakarta flip; switch the import to `org.springframework.transaction.annotation.Transactional` or
  keep `jakarta.transaction.Transactional` — prefer the Spring one for propagation control).
- Keep **Flyway** as-is (`spring.flyway.*`); reuse the existing migration SQL.

## 5. Security: Shiro → Spring Security (+ Vaadin)

- Realm that loads a user by username + checks password → implement `UserDetailsService` returning a
  `UserDetails` with authorities built from the app's permission entities.
- Password hashing: reuse the same algorithm via a matching `PasswordEncoder` (e.g. BCrypt) so
  existing password hashes keep working.
- Path rules in `PathSecurityConfiguration` (`/secured/x/** -> X_ACCESS`) → method/route security:
  `@RolesAllowed`/`@PermitAll` on Vaadin views, or `authorizeHttpRequests` matchers.
- Wire Vaadin with `VaadinSecurityConfigurer` (Vaadin 25) + a Vaadin `LoginView`. "Current user"
  (was `UserSessionBean`/Shiro `Subject`) → `AuthenticationContext` / `SecurityContextHolder`.

## 6. i18n: FacesContext-bound → Spring/Vaadin

- Reuse the `.properties` bundles verbatim (`messages_*`, `menu_*`, `enums_*`, etc.).
- Replace `MultiResourceBundle` (uses `FacesContext`) with Spring `MessageSource`
  (`ReloadableResourceBundleMessageSource`) and/or Vaadin `I18NProvider`. Locale comes from
  `VaadinSession`/`UI.getLocale()` instead of `FacesContext`.

## 7. What is discarded (UI plumbing, not service layer)

`application/controller/*` (JSF beans), `application/components/ui/*` (NavigationManager, AbstractBean),
`infrastructure/jsf/*` (FacesUtils, PrimeFaces renderers, exception handlers), `web.xml`,
`faces-config.xml`, `jboss-web.xml`, `beans.xml`. Domain **exceptions** are kept; their JSF
**handlers** are replaced by a Vaadin error handler + `Notification`.

## 8. Layout (isolated, strangler-fig)

The existing JSF project is **never modified**. The whole Vaadin app is a self-contained,
single-module Maven project under `migration/vaadin/`, with the service layer **copied** in (not
moved) and ported there:

```
<repo-root>/            # the existing JSF project — untouched, still builds & runs
  src/ ... pom.xml      #   (its domain stays as-is)
  migration/vaadin/     # self-contained Vaadin 25 + Spring Boot app
    src/main/java/<basePackage>/domain/...   # COPIED service layer, ported to Spring
    src/main/java/<basePackage>/vaadin/...    # Flow views/shell
    pom.xml, mvnw, application.properties
```

The two apps share **only the database** (the existing docker Postgres). JSF runs via `docker/`
(WildFly, `:8443`); Vaadin runs via `cd migration/vaadin && ./mvnw spring-boot:run` (`:8090`). Running
both against the same DB is what lets you migrate view-by-view and compare behavior directly.
