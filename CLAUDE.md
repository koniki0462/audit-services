# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Spring Boot 3.5 / Java 21 REST backend (`audit-service`) for the AuditApp Next.js front end, which lives in the sibling folder `../auditapp-v2` (it has its own CLAUDE.md). Note that the front end's docs refer to this folder as `../audit-service`, but on disk it is `audit-services`. The API is served at `http://localhost:4000/api` (context path `/api`), which is the front end's default `NEXT_PUBLIC_API_BASE_URL`.

## Commands

Requires JDK 21 and Maven 3.9+.

```bash
mvn spring-boot:run                                   # dev profile: in-memory H2 seeded with sample data
mvn verify                                            # build + tests
mvn test -Dtest=AuditApiTest                          # one test class
mvn test -Dtest=AuditApiTest#returns404ForUnknownAudit   # one test method
docker compose up --build                             # service + PostgreSQL 16, staging profile
```

- Swagger UI: `/api/docs` (disabled in `prod`). Health: `/api/actuator/health` (`/liveness`, `/readiness`).
- H2 console (dev only): `/api/h2-console`, JDBC URL `jdbc:h2:mem:auditdb`, user `sa`.

## Profiles

All profile config is in the single multi-document `src/main/resources/application.yml`.

- `dev` (default): H2 in PostgreSQL mode, Flyway loads `db/migration` **and** `db/seed`, Swagger and H2 console on.
- `staging` / `prod`: PostgreSQL from `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (`DB_POOL_SIZE` optional). No seed data. `prod` also disables Swagger. The Dockerfile defaults to `prod`; docker-compose overrides it to `staging`.
- `PORT` (default 4000) and `CORS_ALLOWED_ORIGINS` (comma-separated, default `http://localhost:3000`) apply everywhere.

## Architecture

Package `com.auditapp.audit`, layered `web` (controllers + `dto` records) → `service` → `repository` → `domain` (JPA entities `Audit`, `Finding`). Controllers only take and return DTO records (`*Request` / `*Response` with a static `from(entity)`); services map requests onto entities and throw `NotFoundException`.

- **Front-end contract.** JSON field names and values must match the front end's `Audit` / `Finding` types in `../auditapp-v2/src/lib/data.ts`. Enums (`AuditStatus`, `Severity`) are stored by constant name (`IN_PROGRESS`) but serialised with labels (`"In Progress"`) via `@JsonValue`. `@JsonCreator from()` (through `domain/Labels.parse`) accepts either form, case-insensitively, and `config/WebConfig` registers the same converters for query params. A new enum should follow this pattern and get a converter in `WebConfig`.
- **Filtering.** List filters are JPA `Specification`s in `repository/Specs`, where a null argument means "no filter". The `q` text search deliberately mirrors the front end's `filterAudits` (case-insensitive substring of id, title, department, auditor), so keep the two in sync.
- **IDs.** IDs are strings assigned by the server from DB sequences: `"AUD-" + audit_seq` (starts at 1001) and `"F-" + finding_seq` (starts at 201), using native `nextval` queries on the repositories. Seed data occupies the low numbers.
- **Concurrency.** Entities have a `@Version` column. `OptimisticLockingFailureException` and `DataIntegrityViolationException` map to 409.
- **Errors.** `web/ApiExceptionHandler` returns everything as RFC 9457 `application/problem+json`. Bean-validation failures add an `errors` map from field to message. Cross-field rules are `@AssertTrue` methods on the request record (for example `AuditRequest.isDateRangeValid`), marked `@JsonIgnore`.
- **Deletes.** Deleting an audit deletes its findings explicitly in `AuditService.delete`. There is also `ON DELETE CASCADE` in the schema.

## Database

- Flyway owns the schema. `spring.jpa.hibernate.ddl-auto=validate`, so every entity change needs a matching migration. Add `src/main/resources/db/migration/V<n>__description.sql` and never edit an applied one.
- Migrations must run on both PostgreSQL and H2 in PostgreSQL mode (dev and tests use H2).
- Seed data is `db/seed/V1_1__seed_sample_data.sql`, which shares Flyway's version space with `db/migration`. Pick new migration versions that don't collide with it. If you change the seed, update the front end's `src/test/fixtures.ts` and the counts asserted in `AuditApiTest`.

## Tests

`src/test/java/com/auditapp/audit/AuditApiTest.java` holds `@SpringBootTest` + MockMvc integration tests against the seeded dev schema. Each test method gets a fresh context with its own uniquely named in-memory H2 database (`@DirtiesContext` AFTER_EACH_TEST_METHOD), so tests can mutate data freely but are slow to add in bulk. Assertions rely on the seed contents (for example 6 audits, `AUD-1001` first).

## Not implemented

There is no authentication (every endpoint is open) and no pagination (lists return everything).
