### Development environment configurations

Use this docker-compose setup to run the full application stack locally: Postgres, PgAdmin4, and WildFly.

#### Running the full stack

**Step 1 — Build the application WAR** (requires Java 17+ and Maven):

```bash
./mvnw clean package -DskipTests
```

**Step 2 — Start all services:**

```bash
cd docker
docker-compose up
```

On first start, WildFly will download the PostgreSQL JDBC driver (~1MB). This is cached in a named Docker volume and won't repeat on subsequent starts.

You will get:

- **WildFly** at *https://localhost:8443* (app) and *http://localhost:9990* (management console)
- **Postgres 12** at *localhost:5432*
- **PgAdmin4** at *http://localhost:6060*

> **Note:** The app enforces HTTPS. HTTP on port 8080 redirects to HTTPS on 8443. Your browser will show a self-signed certificate warning — this is expected in development.

Database credentials: user/password = `sa_webbudget`, database = `webbudget`

#### Re-deploying after a code change

Rebuild the WAR and restart only the app container:

```bash
./mvnw clean package -DskipTests
docker-compose restart webbudget_app
```

#### Restoring a previous database dump

```bash
cat your-dump.sql | docker exec -i webbudget_postgres psql -d webbudget -U sa_webbudget
```

#### Importing PgAdmin4 server configurations

Write your configurations in *servers.json*, then:

```bash
docker cp servers.json webbudget_pgadmin4:/tmp/servers.json
docker exec -it webbudget_pgadmin4 python /pgadmin4/setup.py --load-servers /tmp/servers.json --user admin@webbudget.com
```

Or configure the server directly through the PgAdmin4 web interface at *http://localhost:6060*.
