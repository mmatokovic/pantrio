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

Endpoints are versioned in the path (`/api/v1/...`, Spring Framework 7 API versioning); an unsupported version answers 400. All endpoints need HTTP Basic auth (`user` / `PANTRIO_USER_PASSWORD`). Errors are `{"status", "message", "errors": [{"field", "detail"}]}` (`ErrorResponse`).

| Method | Path | Success | Errors | What it does |
|---|---|---|---|---|
| GET | `/api/v1/items` | 200 | | Page of items (`page`, `size`, `sort`) |
| POST | `/api/v1/items` | 201 | 400, 409 | Create an item |
| GET | `/api/v1/items/{id}` | 200 | 404 | One item |
| PUT | `/api/v1/items/{id}` | 200 | 400, 404 | Update an item |
| DELETE | `/api/v1/items/{id}` | 204 | 404 | Delete an item |
| GET | `/api/v1/storages` | 200 | | Page of storages |
| POST | `/api/v1/storages` | 201 | 400, 409 | Create a storage |
| GET | `/api/v1/storages/{id}` | 200 | 404 | One storage |
| PUT | `/api/v1/storages/{id}` | 200 | 400, 404 | Update a storage |
| DELETE | `/api/v1/storages/{id}` | 204 | 404 | Delete a storage |
| POST | `/api/v1/storages/{storageId}/items` | 201 | 400, 404 | Put an item into a storage |
| GET | `/api/v1/storages/{storageId}/items` | 200 | 404 | Stock of a storage |
| GET | `/api/v1/storages/{storageId}/items/{itemId}` | 200 | 404 | One stocked item |
| PUT | `/api/v1/storages/{storageId}/items/{itemId}` | 200 | 400, 404 | Change quantity / expiry date |
| DELETE | `/api/v1/storages/{storageId}/items/{itemId}` | 204 | 404 | Remove an item from a storage |
| GET | `/api/v1/products/{barcode}` | 200 | 404, 422, 502, 503 | Product from OpenFoodFacts |
| GET | `/api/v1/pull/{barcode}` | 200 | 404, 422, 502, 503 | Fetch a product from OpenFoodFacts |
| POST | `/api/v1/pull/job` | 202 | | Start the item enrichment batch job now |
| DELETE | `/api/v1/pull/job` | 202 | | Interrupt the running job |
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
