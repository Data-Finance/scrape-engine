# scrape-engine

Scraping service of the Data-Finance project. It fetches and parses web pages with jsoup, runs scrape jobs with Spring Batch, and stores data in MongoDB.

> **Status:** early stage. The project structure, architecture rules and tooling are in place; no scraping features yet.

## Tech stack

- Java 27, Spring Boot 4.1, Maven (wrapper included)
- Spring Batch, Spring Web MVC, RestClient
- Spring Data MongoDB (MongoDB Atlas)
- jsoup
- ArchUnit, Spotless (google-java-format)

## Getting started

### Prerequisites

- JDK 27
- Credentials for the project's MongoDB Atlas cluster

### Configuration

Create a `.env` file in the project root (it is git-ignored) with the MongoDB credentials:

```properties
DB_MONGO_USER=<user>
DB_MONGO_PASS=<password>
```

Spring Boot loads it automatically. The connection string itself is in `src/main/resources/application.properties`.

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

The `@SpringBootTest` tests need a valid `.env` to connect to MongoDB.

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
