---
name: jsf-migration-assess
description: >
  Assess a JSF 2 / PrimeFaces application for migration to Vaadin 25 (Flow) on Spring Boot. Detects
  the source stack (javax/jakarta namespace, Java version, DI framework, persistence, security, UI
  libs), inventories the views, sizes the effort, and lists the conversion tasks (namespace, Java,
  DI, persistence, security). Use when the user wants to "assess/plan a JSF to Vaadin migration",
  "analyze a JSF app for Vaadin", or before scaffolding a Vaadin target. Produces migration/manifest.json.
user-invocable: true
argument-hint: "[project-root]"
allowed-tools:
  - Read
  - Glob
  - Grep
  - Bash
---

# JSF Migration Assess

First step of the JSF → Vaadin 25 migration pipeline. Characterizes the **source** app so later steps
can size the work and pick automation. The **target is always Vaadin 25 + Spring Boot + Flow**
(Java 21, Jakarta EE 11) — this skill does not choose a target version, it measures the gap to it.

`$ARGUMENTS` may contain the project root; default to the current directory.

## Workflow

1. **Run the stack detector:**
   ```bash
   python3 migration/scripts/detect_stack.py <project-root> > migration/manifest.json
   ```
   It reports namespace, Java, build, packaging, DI, persistence, security, UI libs, a view count,
   raw javax/jakarta import counts, the conversion tasks, and notes.

2. **Verify & enrich the manifest by reading key files** (don't trust the heuristic blindly):
   - `pom.xml` / `build.gradle` — confirm versions (Java, Hibernate, JSF/PrimeFaces, Deltaspike,
     Shiro), packaging, and Maven profiles.
   - `src/main/webapp/WEB-INF/{web.xml,faces-config.xml,beans.xml,*jboss*,*shiro*}` and any
     `shiro.ini` / security config classes — capture how DI, navigation, and security are wired.
   - `src/main/resources/META-INF/persistence.xml` — datasource (JNDI?), dialect, Envers, ddl.
   - i18n: locate the resource bundles (`*_*.properties`) and any `FacesContext`-bound message code.

3. **Inventory the views.** Reuse the screenshot skill's enumerator for an accurate, classified list:
   ```bash
   python3 migration/scripts/enumerate_views.py <webapp-root> https://placeholder
   ```
   Record the views (list/form/detail/dashboard/error) and group them by module (the path segment
   after `secured/`). Note detail/edit pages that need a record id.

4. **Confirm the service-layer boundary.** Identify the framework-neutral business layer to retain
   (typically `domain/services`, `domain/logics`, `domain/entities`, `domain/repositories`,
   `domain/events`, `domain/calculators`, `domain/exceptions`) versus the JSF-coupled UI to rewrite
   (`application/controller`, `application/components/ui`, `infrastructure/jsf`). Spot-check a couple
   of services for any UI imports (there should be none).

5. **Write the manifest + a short report.** Augment `migration/manifest.json` with the confirmed
   versions, the view inventory (grouped by module), and the retain-vs-rewrite package lists. Then
   summarize for the user: the gap to Vaadin 25 (namespace flip? Java bump? DI port? security port?),
   the conversion tasks, view counts per module, and a suggested first vertical slice
   (login + dashboard + one CRUD module) to migrate first.

## Output

- `migration/manifest.json` — the machine-readable assessment, consumed by `vaadin-scaffold` and the
  per-view skills.
- A human summary in the chat: effort drivers, conversion tasks, view inventory, recommended slice.

## Notes
- Generic: works on any JSF 2 app. The detector is heuristic — always reconcile against the real POM
  and config files before reporting.
- This skill is **read-only**; it does not modify the source app.
- See `../vaadin-scaffold/references/service-port-playbook.md` for what the
  detected gaps imply downstream.
