# Devcontainer image for the JSF->Vaadin migration: Claude Code + Playwright (drive the
# jsf-screenshot-tour skill) + JDK 21 (Zulu) & Maven via SDKMAN (build migration/vaadin).
#
# Base = official Playwright image: ships Node.js + all the system libraries the
# browsers need (fonts, libnss, etc.) — the painful part to get right by hand.
ARG PLAYWRIGHT_VERSION=1.49.1
FROM mcr.microsoft.com/playwright:v${PLAYWRIGHT_VERSION}-jammy

ENV DEBIAN_FRONTEND=noninteractive

# Python 3 (the skill's scripts/enumerate_views.py) + handy CLI tools.
# zip/unzip are needed by SDKMAN.
RUN apt-get update && apt-get install -y --no-install-recommends \
        python3 python3-pip python3-venv \
        git curl jq ca-certificates zip unzip \
    && rm -rf /var/lib/apt/lists/*

# SDKMAN + JDK 21 (Zulu) + Maven — to build the migrated Vaadin app (migration/vaadin).
ENV SDKMAN_DIR=/root/.sdkman
RUN curl -s "https://get.sdkman.io?rcupdate=false" | bash \
    && bash -c 'source "$SDKMAN_DIR/bin/sdkman-init.sh" \
        && JAVA_ID="$(sdk list java | grep -o "21\.[0-9]*\.[0-9]*-zulu" | head -1)" \
        && echo "installing java $JAVA_ID" \
        && sdk install java "$JAVA_ID" \
        && sdk install maven \
        && sdk flush archives && sdk flush temp'
# Put java + mvn on PATH for every shell (no need to source sdkman-init).
ENV JAVA_HOME=${SDKMAN_DIR}/candidates/java/current
ENV PATH=${JAVA_HOME}/bin:${SDKMAN_DIR}/candidates/maven/current/bin:${PATH}

# Browsers live here in the base image; install any extra ones to the same path.
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

# Claude Code CLI + the Playwright MCP server (the browser backend the skill uses).
RUN npm install -g \
        @anthropic-ai/claude-code \
        @playwright/mcp@latest

# Install the bundled Chromium the MCP server uses (.mcp.json sets `--browser chromium`,
# so we don't need Google Chrome). Run from inside the @playwright/mcp package so the
# Chromium revision matches the exact Playwright version it bundles, installed into
# PLAYWRIGHT_BROWSERS_PATH. --with-deps pulls any missing system libraries.
RUN cd "$(npm root -g)/@playwright/mcp" \
    && npx playwright install --with-deps chromium

WORKDIR /workspaces/web-budget
