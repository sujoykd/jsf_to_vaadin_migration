# Devcontainer: Claude Code + Playwright for `jsf-screenshot-tour`

This devcontainer packages everything the [`jsf-screenshot-tour`](../.claude/skills/jsf-screenshot-tour/SKILL.md)
skill needs to navigate a JSF / PrimeFaces app and screenshot every view:

| Tool | Why |
|------|-----|
| **Claude Code CLI** (`@anthropic-ai/claude-code`) | runs the skill |
| **Playwright + Playwright MCP** (`@playwright/mcp`) | the browser backend that navigates & screenshots |
| **Chromium + system libs** | from the `mcr.microsoft.com/playwright` base image |
| **Python 3** | the migration helper scripts (`migration/scripts/`) |
| **SDKMAN + JDK 21 (Zulu) + Maven** | to build & run the migrated Vaadin app (`migration/vaadin`) |

The Playwright MCP server is registered for the project in [`../.mcp.json`](../.mcp.json) with
`--ignore-https-errors` (so self-signed dev certs on `:8443` work) and a 1440×900 viewport.

## Using it

1. **Open in container.** In VS Code: *Dev Containers: Reopen in Container* (or the JetBrains/CLI
   equivalent). First build compiles the image and installs the toolchain.

2. **Authenticate Claude Code** (once; persisted in the `web-budget-claude-config` volume):
   - Interactive: run `claude` and follow the login prompt, **or**
   - API key: set `ANTHROPIC_API_KEY` in your *host* environment before opening the container — it's
     forwarded via `remoteEnv`.

3. **Make the target app reachable.** The skill drives a *running* app; it does not build one.
   Start the app however you like — e.g. the existing stack in [`../docker/`](../docker):
   ```bash
   # on the HOST (not inside this container)
   cd docker && docker compose up
   ```
   From inside the container the host-published app is at **`https://host.docker.internal:8443`**
   (the container maps `host.docker.internal` to the host gateway).

4. **Run the skill** from the project root inside the container:
   ```
   /jsf-screenshot-tour https://host.docker.internal:8443
   ```
   Provide credentials when asked (web-budget seed account: `admin` / `admin`). Screenshots and an
   `index.md` gallery land in `./screenshots/<timestamp>/` (gitignored).

## Notes

- Approve the `playwright` MCP server when Claude Code prompts on first use (project-scoped servers
  from `.mcp.json` require a one-time trust confirmation).
- Pin a different Playwright version via the `PLAYWRIGHT_VERSION` build arg in
  [`devcontainer.json`](devcontainer.json) (keep the base image and `@playwright/mcp` in step).
- Java (JDK 21 Zulu) + Maven are installed via **SDKMAN** (`/root/.sdkman`), on `PATH` for all shells
  (`java -version`, `mvn -v`). Bump the JDK by editing the SDKMAN step in the Dockerfile.
  Build the WAR on the host or in the app's own Docker build.
