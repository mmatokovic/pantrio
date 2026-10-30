# AGENTS.md

Guidance for coding agents working in this repository. What the service
is, how to run it and its endpoints are in README.md.

## Commands

Always use the Maven wrapper, not a local Maven installation. All examples below use `./mvnw` (works in bash on Windows too); use `mvnw.cmd` only when running from cmd.exe or PowerShell. The build needs JDK 25 (the enforcer plugin fails on anything older):

```bash
# Build: checkstyle (compile phase), Error Prone, unit + ArchUnit tests, integration tests
./mvnw clean verify

# Run unit + ArchUnit tests (patterns: **/*Should.java, **/*Rule.java)
./mvnw test

# Run a single unit test class
./mvnw test -Dtest=ItemServiceImplShould

# Run integration tests (pattern: **/*IT.java) — needs Docker for the Oracle Testcontainer
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

Always run `./mvnw clean verify` before saying something works. If `clean` fails with "Failed to delete … target", the IDE holds `target/` open — ask the user to close it or delete `target/`; don't work around it.

## Local development

Start all infrastructure services via Docker Compose:

```bash
cd docker
docker-compose -f compose.yaml up
```

This brings up Oracle DB, Zipkin, Prometheus, Grafana, the OpenTelemetry collector, and Keycloak (running, but not used by the app yet). The app itself is started separately by the IDE or Maven, with the `local` profile (console-only logging).

For Testcontainers reuse (faster integration tests locally), add `testcontainers.reuse.enable=true` to `~/.testcontainers.properties`.

The primary config file is `src/main/resources/application.properties`. Key property prefixes: `spring.datasource.*`, `security.user.*`, `security.prometheus.*`, `openfoodfacts.*`, `batch.job.*`, `pantrio.logging.http.*`, `spring.quartz.*`, `spring.batch.*`. Environment-specific values are `${ENV_VAR}`; secrets never get a default.

## Architecture

Pantrio is a Spring Boot 4.1 / Java 25 pantry inventory management app. It applies **CQRS at the API layer** — controllers expose separate command (mutating) and query (read) operations. It is an API only, with no UI. It follows the Crosig integration-team conventions (the team's `spring-boot` skill; the `household` service is the reference implementation).

### Package layout (`dev.mmatokovic.pantrio`)

| Package | Responsibility |
|---------|---------------|
| `api/` | REST controllers — `ItemController`, `StorageController`, `ProductController`, `JobManagementController`; methods return `ResponseEntity` |
| `app/` | `*Service` interface + `*ServiceImpl` in the same package, MapStruct mappers (sub-packages: `item`, `storage`, `product`, `job`) |
| `domain/` | JPA entities, repository interfaces (ports), request/response records, validation (sub-packages: `item`, `storage`, `instance`, `product`, `validation`) |
| `infra/` | All infrastructure concerns: `config`, `error`, `exception`, `jackson`, `job`, `logging`, `openfoodfacts`, `rest`, `security`, `web` |

Test packages mirror production packages exactly (e.g. `infra/job/BatchConfigShould` lives in `src/test/.../infra/job/`).

### Key infrastructure details

- **API versioning**: Spring Framework 7 path-segment versioning. `WebConfig` prefixes every pantrio `@RestController` with `pantrio.api.base-path` + `/{version}` and resolves the version from that segment only for `/api/**` (springdoc, actuator and static files stay unversioned). Controllers declare `@RequestMapping(path = "...", version = "v1+")` (served from v1 upwards); supported versions are `pantrio.api.supported-versions` (`v1,v2`). A breaking change is a method-level mapping for the new version (`version = "v2+"`) next to the old one pinned to `version = "v1"`, never a change to `v1`. v2 differs from v1 only in `PUT /items/{id}` (full replace instead of partial update) and drops `GET /pull/{barcode}` (use `/products/{code}`). A path that exists only in another version answers 400
- **Database**: Oracle with Spring Data JPA/Hibernate; schema migrations via **Liquibase**. Never edit an applied changeset — add a new one. Oracle does not index foreign key columns: every new FK needs an index whose leading column is the FK column (its own index, or a unique/composite index that starts with it), added in the same PR and guarded with an `indexExists` precondition (`onFail="MARK_RAN"`). `idx_item_instance_storage` was dropped because `uk_item_instance_storage_item (storage_id, item_id)` already covers `fk_item_instance_storage`
- **Entities**: `@Getter` only, `@NoArgsConstructor(access = PROTECTED)`, a static `create(...)` factory and behaviour methods (`update`, `replace`, `enrich`, `restock`) — no setters or builders. `createdAt`/`updatedAt` are passed in from the service's `Clock` (`LocalDateTime.now(clock)`), not `@CreationTimestamp`/`@UpdateTimestamp`. IDs come from Oracle sequences (`*_SEQ`, `allocationSize = 50` = `INCREMENT BY 50`) so Hibernate can batch inserts
- **Scheduling**: Quartz (JDBC job store, **clustered**, `instanceId=AUTO`, so only one node fires the trigger) + Spring Batch — the enrichment job fills items from OpenFoodFacts and stamps `ENRICHED_AT`; run IDs come from the `Clock` bean. `batch.job.enabled` (`PANTRIO_BATCH_JOB_ENABLED`) switches the scheduler on or off; with `overwrite-existing-jobs=false` a cron or pause set through the API survives restarts. Jobs launch on the `@BatchTaskExecutor` (async, one at a time). The chunk step runs on a `ResourcelessTransactionManager` so the OpenFoodFacts call in the processor holds no database connection; the writer saves each chunk in its own `TransactionTemplate` transaction. A second start answers 409. On shutdown the batch executor waits up to 55 s for a running job (compose gives the container 70 s). A run killed mid-flight (crash, `kill -9`) stays `STARTED` in `BATCH_JOB_EXECUTION` and blocks every later start (409) until it is marked failed by hand, for example with `JobOperator.recover`
- **Security**: Spring Security with HTTP Basic Auth; two in-memory users (`user` / `ADMIN` role, `prometheus` / `METRICS` role); passwords bound through `SecurityUsersProperties` from `security.user.password` and `security.prometheus.password`, encoded with the delegating password encoder. CSRF protection is off: pantrio is meant for non-browser clients (a browser holding Basic credentials could be used cross-site)
- **External API**: `OpenFoodFactsAdapter` (`@Repository`, implements the `ProductRepository` port) calls OpenFoodFacts API v2 (`world.openfoodfacts.org/api/v2`) through a `RestClient` built from Boot's `RestClient.Builder` and `ClientHttpRequestFactoryBuilder.detect()`; timeouts are `openfoodfacts.connect-timeout`/`read-timeout`. `@Retryable` (with jitter) retries 5xx/429/IO errors and `@ConcurrencyLimit(policy = REJECT)` answers 503 instead of queueing callers; responses are mapped with MapStruct (`OpenFoodFactsMapper`)
- **Errors**: every error body is `ErrorResponse(status, message, errors)`. `ErrorHandler` builds it for pantrio exceptions; the `ProblemDetail` Spring creates for its own MVC exceptions (malformed body, wrong method, …) is mapped to it in `handleExceptionInternal`, which also logs the failure. 500s never expose the exception message
- **JSON**: Boot's auto-configured `JsonMapper`, customised by the `JsonMapperBuilderCustomizer` in `JacksonConfig` — never define a `JsonMapper` bean, it switches off Boot's Jackson auto-configuration and `spring.jackson.*` (Jackson 3 — databind is `tools.jackson.*`, annotations stay `com.fasterxml.jackson.annotation.*`). Only `HttpLoggingUtils` uses `JsonMapper.shared()`, because it just compacts raw JSON for the log
- **Configuration**: `@ConfigurationProperties` records (`*Properties`), constructor injection, no `@Value` fields; `*Config` classes are `@Configuration(proxyBeanMethods = false)`
- **Logging**: plain `@Slf4j` with the fluent key-value API (`log.atInfo().addKeyValue(...)`), no log topics. `HttpLoggingFilter` logs inbound requests/responses and puts `request.id` into MDC; `RestClientRequestLogger` logs outbound calls; both read `pantrio.logging.http.*` (`HttpLoggingProperties`). `logback-spring.xml` writes a console pattern with `%kvp`, plus a rolling file outside the `local` profile; the pattern includes Boot's `${LOG_CORRELATION_PATTERN:-}` (`[traceId-spanId]`) and `[request.id]` from the MDC — keep both when editing it, because a custom pattern otherwise drops them silently
- **Validation**: Custom `@Barcode` constraint annotation (`BarcodeValidator`). Controllers rely on Spring 7's built-in method validation — no class-level `@Validated`; parameter violations answer 422 via `handleHandlerMethodValidationException`
- **Code style**: Checkstyle runs during the `compile` phase and Error Prone runs in the compiler; both fail the build. Error Prone's `MissingJavadoc` is off — the team writes no Javadoc. NullAway treats every `dev.mmatokovic.pantrio` package as annotated (`-XepOpt:NullAway:AnnotatedPackages`, no `package-info.java`) and fails the build on main code (off for tests, which build partial objects); mark anything that can be null with JSpecify `@Nullable`. Shared Error Prone flags live in the `error-prone.flags` pom property

### Code conventions

- Fields are named after their type in full (`itemRepository`, not `repo`); no `final` on locals, use `var`
- No comments that repeat the code and no Javadoc. A comment is only for a non-obvious *why* or an actionable TODO
- Dead code is deleted, not suppressed with `@SuppressWarnings("unused")`
- `toLowerCase`/`toUpperCase` take `Locale.ROOT`; time comes from the injected `Clock`, never `now()` without one

### Testing strategy

**Naming convention**: unit test classes are `<ClassUnderTest>Should` with methods `methodUnderTest_doSomething[_whenCondition]` (`ItemServiceImplShould.getItem_throwEntityNotFound_whenItemDoesNotExist`); integration test methods name the API operation as the subject (`getItem_returns404_whenItemDoesNotExist`). No `should_` prefix, no `*Test` suffix.

- **Unit tests** (`*Should.java`) use `@ExtendWith(MockitoExtension.class)` with `@Mock` / `@InjectMocks` — not `@SpringBootTest`. Service tests use the real generated mappers (`new ItemMapperImpl()`), so responses are compared whole. Stub with exact arguments and don't `verify` a stubbed call (strict stubs already check it); `verify` only void calls. End every test with `verifyNoMoreInteractions(...)` / `verifyNoInteractions(...)`. Build test data inside each test (no `@BeforeEach` fixtures, no per-type helpers) and assert whole records with `isEqualTo`.
- **Integration tests** (`*IT.java`) extend `BaseIntegrationTest`, which spins up an Oracle Free 23 Testcontainer (same database as `docker/compose.yaml`) and a WireMock server on `RANDOM_PORT` and points RestAssured at the app. Call the API with `RestAssured.given().auth().basic(USER_NAME, USER_PWD)`, send request records as bodies, read responses with `.extract().as(...)` and compare them whole (`usingRecursiveComparison().ignoringFields(...)` only for DB-generated ids/timestamps). Test properties live in `src/test/resources/application-default.properties`. Clean relevant tables with `JdbcTemplate` in `@BeforeEach` — do not rely on transaction rollback. The ITs run with production's transaction settings (Hikari `auto-commit=false`); the test `JdbcTemplate` from `BaseIntegrationTest` uses its own auto-commit connections to the container, so seed data commits without loosening the app's pool.
- **ArchUnit tests** (`src/test/.../archunit/`) enforce structural rules (naming, layering, annotations, proxies, etc.). Never delete or loosen a rule to get green.
- **Contract tests** (`ContractFreezeIT`) guard against unintentional API shape changes.

### Deliberate deviations from team standards

| Standard | Here | Why |
|---|---|---|
| RFC 9457 `ProblemDetail` error bodies | `ErrorResponse(status, message, errors)`; Spring's `ProblemDetail` is mapped to it | the existing client contract |
| `*Mapper` as a final utility class | MapStruct interfaces (constructor injection) | many record ↔ entity mappings; generated code stays in sync with the records |
| Spring context without a DB, port mocked in ITs | Oracle Free Testcontainer, real JPA + Liquibase | pantrio owns its schema, so the ITs prove the mappings and migrations |
| Multi-module (`*-api`, `*-batch`, shared logging module) | single module | one deployable; no second service shares the code |

### Dependency management

All dependency versions are declared in the root `pom.xml` (Dependabot opens weekly update PRs). Before adding a new dependency, check whether it is already present as a transitive dependency (`./mvnw dependency:tree`). Always verify Spring Boot compatibility via the official coordinates matrix for the current Boot version.

### Git

Never add AI attribution (`Co-Authored-By` lines for an AI agent, "Generated with …" footers) to commits or pull requests.
