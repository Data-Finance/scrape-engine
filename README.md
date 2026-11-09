# scrape-engine

Scraping service of the Data-Finance project. It fetches and parses web pages with jsoup, runs scrape jobs with Spring Batch, and stores data in MongoDB.

> **Status:** early stage. The project structure, architecture rules and tooling are in place; no scraping features yet.

## Tech stack

- Java 27, Spring Boot 4.1, Maven (wrapper included)
- Spring Batch, Spring Web MVC, RestClient
- Spring Data MongoDB (MongoDB Atlas)
- Spring Security with JWT (OAuth2 resource server)
- jsoup
- ArchUnit, Spotless (google-java-format)

## Getting started

### Prerequisites

- JDK 27
- OpenSSL, to generate the JWT signing keys
- Credentials for the project's MongoDB Atlas cluster, to run the app

### Configuration

**1. MongoDB credentials.** Create a `.env` file in the project root (it is git-ignored):

```properties
DB_MONGO_USER=<user>
DB_MONGO_PASS=<password>
```

Spring Boot loads it automatically. The connection string itself is in `src/main/resources/application.properties`. Ask a project admin for the credentials; never share them in chat or commit them.

**2. JWT signing keys.** The app signs login tokens with an RSA key pair stored in `certs/` (git-ignored, one pair per machine). Generate it once from the project root:

```bash
mkdir certs && cd certs
openssl genrsa -out keypair.pem 2048
openssl rsa -in keypair.pem -pubout -out public.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem
rm keypair.pem && cd ..
```

`certs/private.pem` must start with `-----BEGIN PRIVATE KEY-----`. Anyone holding it can create valid tokens, so keep it private.

### Build and run

```bash
./mvnw clean verify        # build and run all tests
./mvnw spring-boot:run     # start the application
```

Run a single test class or method:

```bash
./mvnw test -Dtest=ArchitectureTest
./mvnw test -Dtest=ScrapeEngineApplicationTests#contextLoads
```

Locally, `ScrapeEngineApplicationTests` starts the app, so it needs `.env` and `certs/`. In CI (`.github/workflows/maven.yml`) neither exists: the workflow generates a throwaway key pair and points MongoDB at a dummy address with index creation off, so the test never connects to Atlas.

## Authentication

The API is protected with JWT. A user logs in once with username and password, receives a token, and sends that token with every request. Users never need the database credentials.

There is no sign-up: an account exists only if it is in the `users` collection of the `scrape-config` database.

### Add a user

Passwords are stored as BCrypt hashes, never in plain text.

1. The **new user** generates the hash of their own password and sends only the hash to an admin, so nobody else learns the password:

   ```bash
   htpasswd -bnBC 10 "" 'their-password' | tr -d ':\n'; echo
   ```

   The output starts with `$2y$10$` and is 60 characters long. Copy all of it, including the leading `$`.

2. An **admin** inserts the user in MongoDB (Compass: `scrape-config` → `users` → *Add data* → *Insert document*):

   ```json
   {
     "username": "luca",
     "passwordHash": "$2y$10$...",
     "roles": ["USER"]
   }
   ```

To revoke access, delete the user's document. Tokens already issued stay valid until they expire.

### Log in and call the API

Start the app (`./mvnw spring-boot:run`, or *Run* on `ScrapeEngineApplication` in the IDE), then:

```bash
# 1. log in: returns {"token": "eyJ..."}
TOKEN=$(curl -s -X POST localhost:8080/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"luca","password":"their-password"}' \
  | sed 's/.*"token":"\([^"]*\)".*/\1/')

# 2. call a protected endpoint with the token
curl localhost:8080/api/secure/hello -H "Authorization: Bearer $TOKEN"
# → Hello, luca
```

A token is valid for **8 hours**. After that, log in again.

| Endpoint | Access | Description |
|---|---|---|
| `POST /auth/token` | public | Exchanges username and password for a token |
| `GET /api/secure/hello` | token | Test endpoint: returns `Hello, <username>` |

Every new endpoint requires a token by default (see `SecurityConfig`).

### Troubleshooting

| Response | Cause |
|---|---|
| `401 Invalid username or password` on `/auth/token` | Wrong password, unknown user, or a malformed hash in MongoDB (check the leading `$` and the field names `passwordHash` and `roles`) |
| `401` with `Bearer token is malformed` | The login failed, so no token was sent, or the token was copied incompletely |
| `401` on a protected endpoint | Missing or expired token |
| `400` on `/auth/token` | Username or password is empty |
| App does not start: `Failed to bind properties under 'rsa'` | `certs/` is missing or the keys are in the wrong format |

### Code formatting

Code is formatted with google-java-format through Spotless. The check runs during compilation, so unformatted code fails the build. Format before building:

```bash
./mvnw spotless:apply
```

## Architecture

The code follows an onion (hexagonal) architecture under `it.datafinance.scrape_engine`:

| Package | Contents | Rules |
|---|---|---|
| `domain` (`domain.model`, `domain.service`) | Business model and domain logic | Plain Java: no Spring, no persistence, no adapters |
| `application` | Use cases | Plain Java: no Spring, no persistence, no adapters |
| `adapter.in.web` | REST controllers | May depend on inner layers, not on other adapters |
| `adapter.out.persistence` | MongoDB repositories | May depend on inner layers, not on other adapters |
| `config` | Spring wiring | The only place where layers are connected to Spring |

Domain and application services are marked with the project's own `@DomainService` and `@ApplicationService` annotations instead of `@Service`. The configuration classes in `config` register them as Spring beans.

These rules are checked by `ArchitectureTest` (ArchUnit) on every build.

## Contributing

- Create a `feature/<name>` branch and open a pull request to `main`.
- Commit messages use the `type(scope): description` format, e.g. `conf(pom.xml): add dependencies - jsoup`.
