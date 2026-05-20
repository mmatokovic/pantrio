# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Always use the Maven wrapper, not a local Maven installation. All examples below use `./mvnw` (works in bash on Windows too); use `mvnw.cmd` only when running from cmd.exe or PowerShell:

```bash
# Build
./mvnw clean package

# Run unit tests (pattern: **/*Test.java)
./mvnw test

# Run a single test class
./mvnw test -Dtest=ItemServiceImplTest

# Run integration tests (pattern: **/*IT.java)
./mvnw verify

# Run a single integration test class
./mvnw verify -Dit.test=ItemControllerIT

# Check code style
./mvnw checkstyle:check

# Validate Liquibase changesets
./mvnw liquibase:validate

# Inspect dependency tree / unused deps
./mvnw dependency:tree
./mvnw dependency:analyze

# Check for available dependency updates
./mvnw versions:display-dependency-updates

# Generate OpenAPI docs
./mvnw clean verify springdoc-openapi:generate
```

## Local development

Start all infrastructure services via Docker Compose:

```bash
cd docker
docker-compose -f compose.yaml up
```

This brings up Oracle DB, Keycloak (auth), Zipkin, Prometheus, Grafana, and the OpenTelemetry collector. The app itself is started separately by the IDE or Maven.

For Testcontainers reuse (faster integration tests locally), add `testcontainers.reuse.enable=true` to `~/.testcontainers.properties`.

The primary config file is `src/main/resources/application.properties`. Key property prefixes: `spring.datasource.*`, `security.user.*`, `security.prometheus.*`, `openfoodfacts.*`, `spring.quartz.*`, `spring.batch.*`.

## Architecture

Pantrio is a Spring Boot 4 / Java 21 pantry inventory management app. It applies **CQRS at the API layer** — controllers expose separate command (mutating) and query (read) operations — and the UI is driven by HTMX.

### Package layout (`dev.mmatokovic.pantrio`)

| Package | Responsibility |
|---------|---------------|
| `api/` | REST controllers — `ItemController`, `StorageController`, `ProductController`, `JobManagementController` |
| `app/` | Application services + MapStruct mappers (sub-packages: `item`, `storage`, `product`, `job`) |
| `domain/` | JPA entities, repository interfaces, request/response DTOs (sub-packages: `item`, `storage`, `instance`, `product`) |
| `infra/` | All infrastructure concerns: `error`, `exception`, `jackson`, `job`, `logging`, `openfoodfacts`, `rest`, `security`, `validation`, `web` |

Test packages mirror production packages (e.g., `infra/job/BatchConfigTest` lives in `src/test/.../infra/job/`).

### Key infrastructure details

- **Database**: Oracle with Spring Data JPA/Hibernate; schema migrations via **Liquibase**
- **Scheduling**: Quartz (JDBC-backed, clusterable) + Spring Batch — batch jobs enrich items with OpenFoodFacts data
- **Security**: Spring Security with HTTP Basic Auth; two in-memory users (`user` / `ADMIN` role, `prometheus` / `METRICS` role); passwords injected via `security.user.password` and `security.prometheus.password` env vars
- **External API**: `OpenFoodFactsAdapter` calls `world.openfoodfacts.org` via a configured `RestClient`; responses are mapped with MapStruct (`OpenFoodFactsMapper`)
- **Logging**: Structured JSON logs via Logback + Logstash encoder; `HttpLoggingFilter` and `RestClientRequestLogger` trace HTTP in/out
- **Validation**: Custom `@Barcode` constraint annotation (`BarcodeValidator`)
- **Code style**: Checkstyle runs during the `compile` phase; violations fail the build

### Testing strategy

**Naming convention**: test methods follow `should_<behavior>_when_<condition>` (e.g., `should_throw_when_storage_not_found`). The `_when_<condition>` part may be omitted for unambiguous happy-path tests where the condition is implied (e.g., `should_return_single_item`).

- **Unit tests** (`*Test.java`) use `@ExtendWith(MockitoExtension.class)` with `@Mock` / `@InjectMocks` — not `@SpringBootTest`. Always add explicit `verify()` calls for `void` methods because Mockito lenient mode won't catch missing invocations otherwise.
- **Integration tests** (`*IT.java`) extend `BaseIntegrationTest`, which spins up an Oracle XE TestContainer and a WireMock server; they use `@SpringBootTest(webEnvironment = RANDOM_PORT)`. Use `RestTestClient` (autowired) for HTTP calls. Because the app runs on a real port, authentication must be provided on each request via `.headers(h -> h.setBasicAuth("user", "userpwd"))` — `BaseIntegrationTest` sets `security.user.password=userpwd` as a test property. Clean relevant tables with `JdbcTemplate` in `@BeforeEach` — do not rely on transaction rollback.
- **ArchUnit tests** (`src/test/.../archunit/`) enforce structural rules (naming, layering, annotations, proxies, etc.). Review all rules using `.allowEmptyShould(true)` — the goal is to eliminate them.
- **Contract tests** (`ContractFreezeIT`) guard against unintentional API shape changes.

### Dependency management

All dependency versions are declared in the root `pom.xml`. Before adding a new dependency, check whether it is already present as a transitive dependency (`./mvnw dependency:tree`). Always verify Spring Boot compatibility via the official coordinates matrix for the current Boot version.