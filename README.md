# Flux

A Spring Boot web application with Thymeleaf server-side rendering, Spring Security authentication backed by a MySQL `users` table, and JPA persistence.

## Tech Stack

- **Java 17** / **Spring Boot 4.0.6**
- **Spring Security** — form-based authentication against a database `users` table with BCrypt password hashing
- **Thymeleaf** — server-side HTML templates
- **Spring Data JPA** — database access via Hibernate
- **MySQL** — relational database (`flux`)

## What's Implemented

### Database-Backed Authentication

Login credentials are stored in a `users` table in MySQL. Spring Security authenticates against this table via a custom `UserDetailsService`.

- `User` JPA entity mapped to the `users` table (columns: `id`, `username`, `password`, `enabled`, `role`)
- `UserRepository` with `findByUsername` lookup
- `CustomUserDetailsService` loads user details from the database for Spring Security
- Passwords are hashed with BCrypt
- A default `admin` user (password: `password`, role: `ROLE_ADMIN`) is seeded via `data.sql`

### Login Page

Custom login page at `/login` with:

- Form-based username/password authentication
- Error message on invalid credentials ("Invalid username or password.")
- Logout confirmation message ("You have been logged out.")
- CSRF protection on all form submissions
- Unauthenticated requests redirect to the login page

### Home Page

Authenticated users are greeted at `/` with a welcome page that displays their username and a sign-out button.

### Admin Dashboard

Admins can visit `/users` to manage users and inspect the database. The dashboard shows user totals, table names, table row counts, and links to database GET endpoints.

### Database GET API

Admin-only database endpoints are available for read-only inspection:

- `GET /api/database/tables` lists visible tables and row counts
- `GET /api/database/tables/{tableName}` returns every row from one known table
- `GET /api/database` returns every row from every visible table

### Security Configuration

- `/login`, `/register`, and `/css/**` are publicly accessible
- `/users` and `/api/database/**` require `ROLE_ADMIN`
- All other routes require authentication
- Successful login redirects to `/`
- Failed login redirects to `/login?error=true`
- Logout redirects to `/login?logout=true`

## Running the App

```
cd flux
./mvnw spring-boot:run
```

The app starts on port **8080** and connects to MySQL at `localhost:3306/flux`.

### Default Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | password | ROLE_ADMIN |

## Running Tests

```
cd flux
./mvnw test
```

Tests use an H2 in-memory database so MySQL does not need to be running.

## Project Structure

```
flux/
├── src/main/java/com/example/flux/
│   ├── FluxApplication.java              # Entry point
│   ├── config/
│   │   └── SecurityConfig.java           # Security filter chain + BCrypt encoder
│   ├── controller/
│   │   ├── HomeController.java           # GET /
│   │   └── LoginController.java          # GET /login with error/logout params
│   ├── model/
│   │   └── User.java                     # JPA entity → users table
│   ├── repository/
│   │   └── UserRepository.java           # Spring Data JPA interface
│   └── service/
│       └── CustomUserDetailsService.java # Loads users from DB for Spring Security
├── src/main/resources/
│   ├── application.properties            # MySQL datasource config
│   ├── data.sql                          # Seeds default admin user
│   └── templates/
│       ├── home.html                     # Authenticated home page
│       └── login.html                    # Login form with error/logout alerts
└── src/test/
    ├── java/com/example/flux/
    │   ├── FluxApplicationTests.java
    │   └── controller/
    │       └── LoginControllerTest.java  # Login flow + error checking tests
    └── resources/
        ├── application.properties        # H2 config for tests
        └── data.sql                      # Seeds test user (H2-compatible)
```
