# Spring_JWT

A minimal **Spring Boot 4** demo project showing how to secure a REST API with **JWT (JSON Web Token)** authentication instead of session-based login. Users register and log in against a PostgreSQL-backed user store, receive a signed JWT, and use it as a Bearer token to access protected endpoints.

## Features

- User registration with **BCrypt** password hashing
- Login endpoint that authenticates credentials and issues a JWT
- Stateless session management (`SessionCreationPolicy.STATELESS`)
- Custom `JwtFilter` (`OncePerRequestFilter`) that validates the `Authorization: Bearer <token>` header on every request
- HMAC-SHA256 signed tokens via [`jjwt`](https://github.com/jwtk/jjwt) 0.12.6
- Spring Data JPA + PostgreSQL persistence for users
- A couple of sample endpoints (`/hello`, `/about`, `/students`) to demonstrate open vs. protected routes

## Tech Stack

| Layer          | Technology                          |
|----------------|--------------------------------------|
| Language       | Java 21                              |
| Framework      | Spring Boot 4.0.6                    |
| Security       | Spring Security                      |
| Tokens         | jjwt (jjwt-api / jjwt-impl / jjwt-jackson) |
| Persistence    | Spring Data JPA, PostgreSQL          |
| Build Tool     | Maven                                |
| Boilerplate    | Lombok                               |

## Project Structure

```
src/main/java/com/lavish/Spring_JWT/
├── config/
│   ├── SecurityConfig.java     # Security filter chain, auth provider, password encoder
│   └── JwtFilter.java          # Validates JWT on each request, populates SecurityContext
├── controller/
│   ├── UserController.java     # /register, /login
│   ├── HelloController.java    # /hello, /about (sample endpoints)
│   └── StudentController.java  # /students (sample CRUD-ish endpoint)
├── dao/
│   └── UserRepo.java            # Spring Data JPA repository for User
├── model/
│   ├── User.java                # JPA entity (id, username, password)
│   ├── UserPrincipal.java       # UserDetails implementation wrapping User
│   └── Student.java             # Sample DTO
├── service/
│   ├── JwtService.java          # Generates/parses/validates JWTs
│   ├── MyUserDetailsService.java# Loads users for Spring Security
│   └── UserService.java         # Registration logic (password hashing)
└── SpringJwtApplication.java    # Main entry point
```

## Prerequisites

- JDK 21+
- Maven 3.9+ (or use the bundled `mvnw` / `mvnw.cmd` wrapper)
- PostgreSQL running locally with a database available for the app

## Setup

1. **Clone the repo**

   ```bash
   git clone https://github.com/lavishtembhare/Spring_JWT.git
   cd Spring_JWT
   ```

2. **Create the database**

   ```sql
   CREATE DATABASE "StudentDB";
   ```

3. **Configure `src/main/resources/application.properties`**

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/StudentDB
   spring.datasource.username=<your-db-username>
   spring.datasource.password=<your-db-password>
   server.port=8090
   ```

   > ⚠️ The checked-in `application.properties` contains hardcoded local credentials for convenience during development. Replace them with your own values (and never commit real credentials) before deploying anywhere.

4. **Run the app**

   ```bash
   ./mvnw spring-boot:run
   ```

   The app starts on `http://localhost:8090`.

## API Endpoints

| Method | Endpoint       | Auth required | Description                          |
|--------|----------------|----------------|---------------------------------------|
| POST   | `/register`   | No             | Create a new user (password is hashed) |
| POST   | `/login`      | No             | Authenticate and receive a JWT        |
| GET    | `/hello`      | Yes            | Sample protected endpoint             |
| GET    | `/about`      | Yes            | Sample protected endpoint             |
| GET    | `/students`   | Yes            | List sample students                  |
| POST   | `/students`   | Yes            | Add a sample student                  |

### Register

```bash
curl -X POST http://localhost:8090/register \
  -H "Content-Type: application/json" \
  -d '{"username":"lavish","password":"secret123"}'
```

### Login

```bash
curl -X POST http://localhost:8090/login \
  -H "Content-Type: application/json" \
  -d '{"username":"lavish","password":"secret123"}'
```

This returns a raw JWT string. Use it as a Bearer token for protected endpoints:

```bash
curl http://localhost:8090/hello \
  -H "Authorization: Bearer <token-from-login>"
```

## How Authentication Works

1. `POST /register` hashes the incoming password with `BCryptPasswordEncoder` and saves the user via `UserRepo`.
2. `POST /login` delegates to Spring Security's `AuthenticationManager`, which uses `MyUserDetailsService` + `DaoAuthenticationProvider` to verify credentials against the stored hash.
3. On success, `JwtService` issues a token signed with an HMAC-SHA256 key, containing the username as the subject and a short expiration window.
4. On subsequent requests, `JwtFilter` reads the `Authorization` header, extracts the username from the token, loads the corresponding `UserDetails`, and — if valid — sets the `Authentication` on the `SecurityContext` so the request proceeds as authenticated.
5. `SecurityConfig` disables CSRF (appropriate for a stateless, token-based API), sets session policy to `STATELESS`, and permits `/register` and `/login` without authentication while requiring authentication for everything else.

## Known Limitations

This is a learning/demo project, so a few things are simplified and worth knowing about before using it as a base for anything real:

- `JwtService.validateToken()` currently always returns `true` — it does not actually check token expiration or a username match against the provided `UserDetails`.
- The secret key used to sign tokens is regenerated randomly every time the app restarts (it isn't persisted), so tokens issued before a restart become invalid.
- Database and default `spring.security.user.*` credentials are committed in `application.properties`; move these to environment variables or a secrets manager for anything beyond local development.
- No role-based authorization is implemented — every authenticated user is granted a single `USER` authority.
- No refresh-token flow; access tokens expire after 3 minutes with no renewal mechanism.

## License

This project is licensed under the [MIT License](LICENSE).
