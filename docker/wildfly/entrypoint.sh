#!/bin/bash
set -e

POSTGRES_MODULE_DIR=$JBOSS_HOME/modules/org/postgresql/main
POSTGRES_DRIVER_VERSION=42.6.0

# Download PostgreSQL JDBC driver on first run (persisted via named volume)
if [ ! -f "$POSTGRES_MODULE_DIR/postgresql.jar" ]; then
    echo ">>> Downloading PostgreSQL JDBC driver $POSTGRES_DRIVER_VERSION..."
    mkdir -p "$POSTGRES_MODULE_DIR"
    curl -fsSL "https://jdbc.postgresql.org/download/postgresql-${POSTGRES_DRIVER_VERSION}.jar" \
        -o "$POSTGRES_MODULE_DIR/postgresql.jar"
    cp /opt/config/module.xml "$POSTGRES_MODULE_DIR/module.xml"
    echo ">>> Driver installed."
fi

# Configure datasource in standalone.xml (idempotent — safe to run on every start)
echo ">>> Configuring WildFly datasource..."
$JBOSS_HOME/bin/jboss-cli.sh --file=/opt/config/configure.cli

# Deploy the application WAR
WAR=$(ls /opt/app/web-budget-*.war 2>/dev/null | head -1)
if [ -z "$WAR" ]; then
    echo "WARNING: No WAR found in /opt/app. Build the project first:"
    echo "  ./mvnw clean package -DskipTests"
else
    cp "$WAR" "$JBOSS_HOME/standalone/deployments/web-budget.war"
    echo ">>> Deployed: $(basename $WAR)"
fi

echo ">>> Starting WildFly..."
exec $JBOSS_HOME/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0
