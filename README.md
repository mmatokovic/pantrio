# Pantrio

One-stop comestibles management solution.

Pantrio helps you manage pantry inventory with modern practices, using CQRS at the API level to separate commands (actions) from queries (read operations), combined with a responsive UI powered by HTMX.

## Running locally

Requires JDK 25 and Docker.

```bash
cd docker
docker-compose -f compose.yaml up
```

Start the app from the IDE or with `./mvnw spring-boot:run`, with the variables from `.env.example` set and the
`local` profile active (`SPRING_PROFILES_ACTIVE=local` logs to the console only).

```bash
./mvnw clean verify   # build, checkstyle, Error Prone, unit + ArchUnit tests, integration tests
```

## Endpoints

Endpoints are versioned in the path (`/api/v1/...`, `/api/v2/...`, Spring Framework 7 API versioning); an unsupported version, or a path that does not exist in that version, answers 400. v2 is v1 with two differences: `PUT /api/v2/items/{id}` replaces the whole item (fields left out are cleared, v1 keeps them), and `GET /pull/{barcode}` is gone (use `/products/{barcode}`). An unknown `sort` property answers 400. Swagger UI is at `/swagger-ui.html` and the OpenAPI document at `/v3/api-docs` (no auth). All endpoints need HTTP Basic auth as `user` / `PANTRIO_USER_PASSWORD` (role ADMIN): no credentials answer 401, the `prometheus` user (role METRICS, `/actuator/prometheus` only) answers 403. An update that races another one answers 409. Errors are `{"status", "message", "errors": [{"field", "detail"}]}` (`ErrorResponse`).

| Method | Path | Success | Errors | What it does |
|---|---|---|---|---|
| GET | `/api/v1/items` | 200 | | Page of items (`page`, `size`, `sort`) |
| POST | `/api/v1/items` | 201 | 400, 409 | Create an item |
| GET | `/api/v1/items/{id}` | 200 | 404 | One item |
| PUT | `/api/v1/items/{id}` | 200 | 400, 404, 409 | Update an item (v1: only given fields; v2: full replace) |
| DELETE | `/api/v1/items/{id}` | 204 | 404, 409 | Delete an item (409 while it is stocked) |
| GET | `/api/v1/storages` | 200 | | Page of storages |
| POST | `/api/v1/storages` | 201 | 400, 409 | Create a storage |
| GET | `/api/v1/storages/{id}` | 200 | 404 | One storage |
| PUT | `/api/v1/storages/{id}` | 200 | 400, 404 | Update a storage |
| DELETE | `/api/v1/storages/{id}` | 204 | 404, 409 | Delete a storage (409 while it holds stock) |
| POST | `/api/v1/storages/{storageId}/items` | 201 | 400, 404 | Put an item into a storage |
| GET | `/api/v1/storages/{storageId}/items` | 200 | 404 | Stock of a storage |
| GET | `/api/v1/storages/{storageId}/items/{itemId}` | 200 | 404 | One stocked item |
| PUT | `/api/v1/storages/{storageId}/items/{itemId}` | 200 | 400, 404 | Change quantity / expiry date |
| DELETE | `/api/v1/storages/{storageId}/items/{itemId}` | 204 | 404 | Remove an item from a storage |
| GET | `/api/v1/products/{barcode}` | 200 | 404, 422, 502, 503 | Product from OpenFoodFacts (503 also when too many lookups run at once) |
| GET | `/api/v1/pull/{barcode}` | 200 | 404, 422, 502, 503 | Fetch a product from OpenFoodFacts (v1 only) |
| POST | `/api/v1/pull/job` | 202 | 409 | Start the item enrichment batch job in the background (409 while a run is in progress) |
| DELETE | `/api/v1/pull/job` | 202 | 409 | Stop the running job (409 when nothing runs) |
| PUT | `/api/v1/pull/job?cron=` | 200 | 400 | Reschedule the job (Quartz cron), returns the next fire time |
| PUT | `/api/v1/pull/job/state?state=` | 200 | 400 | `resume` or `pause` the job (case-insensitive) |
| GET | `/api/v1/pull/job` | 200 | | Quartz trigger state |

## Author Information
[Mislav Matoković](https://github.com/mmatokovi)
## Helpful guides

- When commiting use style described here: https://cbea.ms/git-commit/

- This application is developed trying to follow following document: https://12factor.net/

- Interesting articles about pull requests: https://medium.com/@sam-cooper/i-merge-my-own-pull-requests-3001fe247be2
  and https://medium.com/better-programming/i-review-my-own-pull-requests-83f74937ccf8

- Guide how to name test classes and
  methods: https://www.codurance.com/publications/2014/12/13/naming-test-classes-and-methods <br>
  TDD methodology should be applied where possible, but not in strict mode.

- Why is having shared libraries (dependencies) hard to
  maintain: https://phauer.com/2016/dont-share-libraries-among-microservices/

- When updating dependency versions (Spring-related), it's useful to always check compatibility matrix.
  Example for version 4.1: https://docs.spring.io/spring-boot/4.1/appendix/dependency-versions/coordinates.html

***
