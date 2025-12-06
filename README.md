# GyanMarg Library - Spring Boot Demo

Run locally:

```bash
mvn spring-boot:run
```

Build jar:

```bash
mvn -DskipTests package
java -jar target/gyan-marg-library-0.0.1-SNAPSHOT.jar
```

Deploy on Render (manual steps):

1. Commit and push the repository to GitHub.
2. Create a new Web Service on Render (https://render.com) and connect your GitHub repository.
3. Choose:
   - Environment: `Java`
   - Plan: `Free` (or your preferred plan)
   - Build command: `mvn -DskipTests package`
   - Start command: `java -Dserver.port=$PORT -jar target/gyan-marg-library-0.0.1-SNAPSHOT.jar`
4. (Optional) Add environment variables in Render: `JAVA_TOOL_OPTIONS = -Xmx512m`.
5. Deploy — Render will build the app and start it. The application will be reachable on the Render-provided URL.

Alternatively you can add `render.yaml` to the repo for Render to pick up auto-deploy configuration.

Notes:
- The H2 database is in-memory. For persistent data across deploys you should connect to an external database and update `spring.datasource` settings.
- Admin login: `admin` / `admin123`. WhatsApp sending is simulated on the success page.