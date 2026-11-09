# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`scrape-engine` is a Spring Boot 4.1 service (Java 27, Maven) for the Data-Finance org. It is at an early, scaffolding stage. The dependency set signals the intended design:

- **jsoup**: HTML fetching/parsing for scraping
- **Spring Batch**: scrape jobs run as batch jobs
- **Spring Data MongoDB**: persistence (database `scrape-config` on a MongoDB Atlas cluster)
- **Spring Web MVC + RestClient**: REST API and outbound HTTP calls
- **ArchUnit (junit6)**: architecture/layering rules enforced as tests

Base package is `it.datafinance.scrape_engine`. The underscore replaces the hyphen in the artifact name because hyphens aren't valid in Java package names.

## Architecture

Onion/hexagonal layering, enforced by `ArchitectureTest` (ArchUnit):

- `..domain..` (`domain.model`, `domain.service`) must not depend on Spring, `jakarta.persistence` or adapters. It is plain Java.
- `..application..` has the same restrictions, except that it may use `org.springframework.transaction.annotation` (`@Transactional`, `Propagation`, `Isolation`). The `MongoTransactionManager` bean in `config` backs `@Transactional`.
- `..adapter.in.web..`, `..adapter.out.persistence..`: adapters, which may depend on the inner layers but not on each other.
- `..config..` is the only place where the layers are wired into Spring. It is excluded from the onion layer checks.

Because domain and application can't use Spring stereotypes such as `@Service` or `@Component`, they use the project's own marker annotations, `@DomainService` (`domain`) and `@ApplicationService` (`application`). `DomainServiceConfig` and `ApplicationServiceConfig` in `config` turn annotated classes into beans with `@ComponentScan` include filters. When you add a domain or application service, annotate it with one of these markers rather than a Spring stereotype.

Layers are optional and the rules allow empty packages, so the test passes before the packages exist. When you add a new adapter (e.g. batch or scraper), register it in `ArchitectureTest`.

## Commands

Use the Maven wrapper:

```bash
./mvnw clean verify                                     # build + run all tests
./mvnw test -Dtest=ScrapeEngineApplicationTests         # single test class
./mvnw test -Dtest=ScrapeEngineApplicationTests#contextLoads   # single test method
./mvnw spring-boot:run                                  # run the app
./mvnw package -DskipTests                              # build the jar only
./mvnw spotless:apply                                   # format code
```

Formatting is enforced by Spotless with google-java-format (2-space indent). `spotless:check` runs in the `compile` phase, so unformatted code fails the build. Run `./mvnw spotless:apply` before building.

## Configuration and secrets

- `application.properties` imports an optional `.env` file from the working directory (`spring.config.import=optional:file:.env[.properties]`), which is parsed as a properties file. `.env` is git-ignored.
- `.env` must define `DB_MONGO_USER` and `DB_MONGO_PASS`, which are interpolated into `spring.mongodb.uri`. Without them, the app and the `@SpringBootTest` context test cannot connect to MongoDB.
- Virtual threads are enabled (`spring.threads.virtual.enabled=true`).
- The `spring.jpa.*` properties in `application.properties` have no effect, because there is no JPA/Hibernate dependency. MongoDB access goes through Spring Data MongoDB.

## Git conventions

- Work on `feature/<name>` branches and merge into `master` via PR. The git remote is named `scrape-engine`, not `origin`.
- CI (`.github/workflows/maven.yml`) runs `mvn clean verify` with Java 27 on pushes and PRs to `master`.
- Commit messages follow a `type(scope): description` style, e.g. `conf(pom.xml): add dependencies - jsoup`.
