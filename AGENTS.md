# Agents

## Cursor Cloud specific instructions

### Project overview

Single Spring Boot 4.0.6 web application (Java 17) located in `flux/`. Uses Maven wrapper (`./mvnw`). No monorepo, no additional services.

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

### Key caveats

- The project uses an **H2 in-memory database** (`jdbc:h2:mem:fluxdb`) for development. No external database is needed.
- The scaffold has no controllers or Thymeleaf templates yet — hitting `/` after auth returns a 404 Whitelabel Error, which is expected.
- Spring Security blocks unauthenticated requests; use Basic Auth (`-u user:password`) for `curl` testing or the form login at `/login`.
- The H2 web console is enabled at `/h2-console` but may require a custom `SecurityFilterChain` bean to allow frame rendering if accessed via browser.
- Java 21 is installed on the VM and is compatible with the `java.version=17` target.
