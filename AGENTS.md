# Agents

## Cursor Cloud specific instructions

Single Spring Boot 4.0.6 app in `flux/`. Build/test/run commands are in the README.

- MySQL must be running before `./mvnw spring-boot:run`. In the Cloud Agent VM (no systemd), start it manually: `mysqld --user=mysql &`
- The `flux` database, user `flux`/`flux`, must exist. Create with: `mysql -u root -e "CREATE DATABASE IF NOT EXISTS flux; CREATE USER IF NOT EXISTS 'flux'@'localhost' IDENTIFIED BY 'flux'; GRANT ALL PRIVILEGES ON flux.* TO 'flux'@'localhost'; FLUSH PRIVILEGES;"`
- Tests use H2 in-memory (`src/test/resources/application.properties`) — no MySQL needed for `./mvnw test`.
- Java 21 on the VM is compatible with the `java.version=17` target.
