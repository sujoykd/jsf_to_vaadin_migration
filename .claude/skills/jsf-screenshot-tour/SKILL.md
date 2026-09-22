---
name: jsf-screenshot-tour
description: >
  Navigate every page of a JSF 2 / PrimeFaces web application and capture a screenshot of each
  view. Use when the user asks to "screenshot all pages", "capture every view", "take screenshots
  of the whole app", "tour the JSF app", "visual catalog of the site", or wants every screen of a
  deployed JSF/PrimeFaces application captured. Prompts for the base URL and login credentials.
user-invocable: true
argument-hint: "[base-url]"
allowed-tools:
  - Read
  - Glob
  - Write
  - Bash
---

# JSF Screenshot Tour

Drive a browser through all views of a JSF 2 / PrimeFaces app and screenshot each one. This skill
is **generic** — it ships with defaults that fit the web-budget reference app but works on any
JSF/PrimeFaces site. Adapt selectors to the target app when they differ.

`$ARGUMENTS` may contain the base URL. If empty, ask for it.

**Targeted recapture.** This skill can also (re)capture a *specific* set of views instead of the full
crawl — e.g. when `jsf-view-analyze` finds a navigable view with no baseline. Given a list of view
URLs/slugs to capture, do **Phase 2** (launch + log in) then jump straight to **Phase 4** for just
those URLs — skip the Phase 1 enumerate and the Phase 3 id-harvest (unless a target needs an `?id=`,
in which case harvest one id for that entity first). Write into the most recent existing
`screenshots/<stamp>/` dir if there is one, else create a new stamped dir. Waits, cert handling, and
`_slug.py` naming are identical to a full tour.

## Browser tooling

This skill needs a browser-automation MCP server. Pick whichever is available, in this order:

1. **Playwright MCP** (`mcp__playwright__*`) — the backend provided by this project's devcontainer
   (`.devcontainer/` + `.mcp.json`). Preferred when present; it is configured with
   `--ignore-https-errors` so self-signed dev certs (e.g. `https://...:8443`) just work. Key tools:
   - `mcp__playwright__browser_navigate` — go to a URL
   - `mcp__playwright__browser_take_screenshot` — capture (pass a `filename`/path)
   - `mcp__playwright__browser_type` — type into a field; `mcp__playwright__browser_click` — click
   - `mcp__playwright__browser_evaluate` — run JS in the page (for crawling links / scraping ids)
   - `mcp__playwright__browser_snapshot` — accessibility snapshot (find elements without ids)
   - `mcp__playwright__browser_resize`, `mcp__playwright__browser_wait_for`
2. **`Claude_Preview`** (`mcp__Claude_Preview__preview_*`) — `preview_start`, `preview_screenshot`,
   `preview_click`, `preview_fill`, `preview_eval`, `preview_resize`, `preview_list`.
3. **`Claude_in_Chrome`** (`mcp__Claude_in_Chrome__navigate`, `__computer`, `__form_input`,
   `__find`, `__javascript_tool`).

All of these are *deferred* tools — load the chosen server's schemas at the start of a run with
`ToolSearch`, e.g. `select:mcp__playwright__browser_navigate,mcp__playwright__browser_take_screenshot,mcp__playwright__browser_type,mcp__playwright__browser_click,mcp__playwright__browser_evaluate,mcp__playwright__browser_snapshot`.

The phases below describe steps generically (navigate / fill / click / eval / screenshot) — map each
to the tool set you picked.

---

## Phase 0 — Gather inputs

Ask only for what you don't already have. Collect:

- **Base URL** — from `$ARGUMENTS`; else read `BASE_URL` from `migration/migration.config.json` if present;
  else ask. Strip any trailing `/`. (web-budget default: `https://localhost:8443`.) **When running
  inside the project's devcontainer**, an app started on the host is reached at
  `https://host.docker.internal:8443` instead of `localhost`.
- **Credentials** — read `APP_USERNAME` / `APP_PASSWORD` from `migration/migration.config.json` if
  present and non-empty; **otherwise ask the user** (AskUserQuestion or a plain prompt). That config
  file is the only sanctioned source — do **not** otherwise hunt for credentials: never read SQL seed
  files, source, git history, or other configs to guess a login, and never assume defaults like
  `admin`/`admin`. **Treat the credentials as secret: never echo them back, never write them into
  screenshots' filenames, the report, logs, or any file** (the config file holding them is gitignored).
- **Source path** (for the static scan) — default to the current working directory if it looks like
  a JSF project (a `src/main/webapp` dir containing `*.xhtml` exists). If none, skip the static scan
  and rely on the dynamic crawl only.
- **Output dir** — default `./screenshots/<stamp>/`. Generate `<stamp>` with a shell call
  (`date +%Y%m%d-%H%M%S`) — do not rely on any in-model clock.
- **Viewport** — default 1440×900 unless the user requests otherwise.

Create the output dir with `mkdir -p`.

---

## Phase 1 — Build the static view inventory

If a source path is available, run the enumerator (scripts live in `migration/scripts/`):

```bash
python3 migration/scripts/enumerate_views.py <webapp-root> <base-url> > <output>/views.json
```

(The webapp-root is typically `<source>/src/main/webapp`.)

It emits a JSON array of `{url, file, kind, needs_id}`. `kind` is one of
`public | dashboard | list | form-new | detail-needs-id | error | other`. It already excludes
`template/`, `dialog*`, `sidebar*`, and `ui:include` fragments.

Keep this list as the baseline inventory. Entries with `needs_id: true` are deferred to Phase 3
(they need a real record id appended as `?id=<n>`).

If no source is available, start with an empty inventory and rely on Phase 2's crawl.

---

## Phase 2 — Launch the browser and log in

1. Start a preview session at the base URL (the welcome file `index.xhtml` / login page). Set the
   viewport. **Expect a self-signed cert** on HTTPS dev deployments — if the tool surfaces a TLS /
   certificate error, tell the user and suggest the explicit `https://<host>:8443` URL and that the
   cert may need to be accepted once; try the Chrome fallback which can proceed past cert warnings.
2. Screenshot the login page → `<output>/index.png`.
3. Log in. The JSF login form often has **no stable input ids** (e.g. web-budget uses
   `<h:form prependId="false">` with auto-generated ids), so **match by input type, not id**:
   - username → the visible `input[type=text]`
   - password → the `input[type=password]`
   - submit → the primary button (`button.btn-primary`, or `[type=submit]`, or the only command
     button in the form)
   Fill the two fields and click submit.
4. Verify success: the URL now contains `secured/` (or the app's authenticated area) / the dashboard
   is visible. If login failed, capture the on-page error message and **stop** — report it to the
   user rather than screenshotting error states blindly.
5. Screenshot the dashboard → `<output>/secured__dashboard.png`.

### Dynamic crawl (union into inventory)

Once authenticated, read the rendered navigation links and merge any new ones into the inventory.
The PrimeFaces menu renders real GET anchors (`<p:link>` → `<a href="*.xhtml">`):

```js
[...document.querySelectorAll('a[href$=".xhtml"], a[href*=".xhtml?"]')]
  .map(a => a.href)
  .filter((v, i, arr) => arr.indexOf(v) === i)
```

Run that with `preview_eval`. Add URLs not already present. Coverage depends on the logged-in
user's permissions (menu items are permission-gated) — note this in the report if the account is
not a full admin.

---

## Phase 3 — Harvest record ids (detail / edit depth)

Detail, statistics, balance, and edit-mode form pages need `?id=<record>`. Best-effort harvest:

For each `list` view in the inventory:
1. Navigate to it, wait for load, screenshot it (this also covers the list page itself).
2. Scrape candidate ids from the rendered data table with `preview_eval`, e.g.:
   ```js
   [...document.querySelectorAll('a[href*="id="]')]
     .map(a => (a.href.match(/[?&]id=(\d+)/) || [])[1])
     .filter(Boolean)
   ```
   Also inspect row click handlers / `data-rk` (PrimeFaces row keys) if no `id=` links exist.
3. Take up to **2** distinct ids per entity. For each id, materialize the matching `needs_id` URLs
   for that entity folder (e.g. `detailCard.xhtml?id=<n>`, `formCard.xhtml?id=<n>`,
   `cardStatistics.xhtml?id=<n>`) and add them to the capture list.
4. If a list has **no rows**, log `no records — skipped detail views for <entity>` and continue.
   Do not fail the run.

---

## Phase 4 — Screenshot every resolved view

For each URL in the (deduped) capture list that hasn't been shot yet:

1. Navigate. Wait for the page to settle — network idle, and the PrimeFaces/`pace.js` loader bar
   gone (`document.querySelector('.pace-running') === null`). A short fixed wait after that is fine.
2. `preview_screenshot` → `<output>/<slug>.png`. Get the canonical filename from the shared helper
   (one definition reused by the spec/verify steps, so names never drift):
   ```bash
   python3 migration/scripts/_slug.py --png "<url>"   # id is read from ?id=, or pass it as a 2nd arg
   ```
   e.g. `secured/registration/card/detailCard.xhtml?id=7` → `secured__registration__card__detailCard__id7.png`.
3. On any failure (timeout, 401/403/500, blank page), record the URL + reason and **keep going**.
   If you get redirected back to the login page, the session expired — re-run Phase 2 login once,
   then continue.

Be considerate: navigate sequentially, don't hammer the server.

---

## Phase 5 — Report

Write `<output>/index.md`:
- A short header: base URL, account used (username only — never the password), viewport, timestamp,
  and totals (captured N, skipped M, failed K).
- Screenshots grouped by module — infer the group from the path segment after `secured/`
  (`registration`, `journal`, `financial`, `configuration`) plus a `public` group (login/error) and
  `dashboard`. List each as a relative link to its `.png`.
- A **Failed / skipped** section listing each URL with its reason.

Optionally also emit a simple `index.html` gallery (`<img>` thumbnails) if the user wants to browse
visually.

Finally, print a concise summary to the user and the path to `index.md`. **Do not** include the
password anywhere.

---

## Generic-app notes & limitations

- **Selectors are defaults, not contracts.** If the target app's login form, loader, or menu differ,
  inspect the DOM (`preview_eval` / `preview_inspect`) and adapt.
- **Permission-gated menus** mean a non-admin account sees fewer pages — coverage reflects the
  account's roles.
- **POST-navigated views** (JSF command links that post a form rather than expose a GET URL) can't be
  deep-linked. This skill captures GET-addressable views (`p:link` anchors and `?id=` URLs). Note any
  such gaps in the report.
- **Dialogs / fragments** (`dialog*.xhtml`, `ui:include` partials) are intentionally excluded — they
  render inside a host page, not standalone.
