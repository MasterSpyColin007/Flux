# Agents

## Cursor Cloud specific instructions

### Project overview

Single Spring Boot 4.0.6 web application (Java 17) located in `flux/`. Uses Maven wrapper (`./mvnw`). No monorepo, no additional services.

### Prerequisites

- **Java 21** (pre-installed on VM, compatible with `java.version=17` target)
- **MySQL 8** — the `flux` database must exist. Start and seed it with:
  ```
  mkdir -p /var/run/mysqld && chown mysql:mysql /var/run/mysqld
  mysqld --user=mysql --datadir=/var/lib/mysql &
  sleep 3
  mysql -u root -e "CREATE DATABASE IF NOT EXISTS flux; CREATE USER IF NOT EXISTS 'flux'@'localhost' IDENTIFIED BY 'flux'; GRANT ALL PRIVILEGES ON flux.* TO 'flux'@'localhost'; FLUSH PRIVILEGES;"
  ```

### Running the app

```
cd flux
./mvnw spring-boot:run
```

Starts on port **8080**. Spring Security is active with default in-memory credentials: `user` / `password`.

### Running tests

```
cd flux
./mvnw test
```

Tests use an **H2 in-memory database** (configured in `src/test/resources/application.properties`) so MySQL does not need to be running for tests to pass.

### Key caveats

- The app connects to MySQL at `localhost:3306` with user `flux` / password `flux` and database `flux`. Make sure MySQL is running before starting the app.
- The scaffold has no controllers or Thymeleaf templates yet — hitting `/` after auth returns a 404 Whitelabel Error, which is expected.
- Spring Security blocks unauthenticated requests; use Basic Auth (`-u user:password`) for `curl` testing or the form login at `/login`.
- In the Cloud Agent VM, MySQL must be started manually (`mysqld --user=mysql &`) since there is no systemd.
