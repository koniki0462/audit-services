# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Spring Boot 3.5 / Java 21 REST backend (`audit-service`) for the AuditApp Next.js front end, which lives in the sibling folder `../auditapp-v2` (it has its own CLAUDE.md). Note that the front end's docs refer to this folder as `../audit-service`, but on disk it is `audit-services`. The API is served at `http://localhost:4000/api` (context path `/api`), which is the front end's default `NEXT_PUBLIC_API_BASE_URL`.

## Commands

Requires JDK 21 and Maven 3.9+, and Docker for running locally (not for tests).

```bash
mvn spring-boot:run                                   # dev profile: starts PostgreSQL in Docker, seeds sample data
mvn verify                                            # build + tests (in-memory H2, no Docker needed)
mvn test -Dtest=AuditApiTest                          # one test class
mvn test -Dtest=AuditApiTest#returns404ForUnknownAudit   # one test method
docker compose down -v                                # wipe the local database; the next start re-seeds it
docker compose --profile app up --build               # service + PostgreSQL in containers, staging profile
```

- Swagger UI: `/api/docs` (disabled in `prod`). Health: `/api/actuator/health` (`/liveness`, `/readiness`).
- Local database: `docker-compose.yml` service `db` (PostgreSQL 16), published on `localhost:5432` (`DB_HOST_PORT` to change it if another PostgreSQL holds 5432), database `audit`, user `myuser`, password `mypassword` (override with `DB_USERNAME` / `DB_PASSWORD`; they only apply to a new volume). Data persists in the `pgdata` volume across restarts.

## Profiles

All profile config is in the single multi-document `src/main/resources/application.yml`.

- `dev` (default): PostgreSQL from Docker. `spring-boot-docker-compose` (an optional dependency, left out of the packaged jar) runs `docker compose up` for `db` on startup and points the datasource at it; `lifecycle-management: start-only` leaves the container running afterwards. `SPRING_DOCKER_COMPOSE_ENABLED=false` uses the dev datasource (`DB_URL`, default `localhost:5432`) against a database you manage. Flyway loads `db/migration` **and** `db/seed`; Swagger on. Compose support is disabled in every other profile, and Spring Boot skips it in tests.
- `staging` / `prod`: PostgreSQL from `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (`DB_POOL_SIZE` optional), and `JWT_SECRET` (32+ bytes, startup fails without it; dev has a built-in default). No seed data. `prod` also disables Swagger. The Dockerfile defaults to `prod`; docker-compose overrides it to `staging`.
- `PORT` (default 4000), `JWT_TTL` (token lifetime, default `8h`) and `CORS_ALLOWED_ORIGINS` (comma-separated, default `http://localhost:3000`) apply everywhere.

## Architecture

Package `com.auditapp.audit`, layered `web` (controllers + `dto` records) → `service` → `repository` → `domain` (JPA entities `Audit`, `Finding`, `Organization`, `UserAccount`, `Membership`), plus `security`. Controllers only take and return DTO records (`*Request` / `*Response` with a static `from(entity)`); services map requests onto entities and throw `NotFoundException`.

- **Organizations.** `organizations` (primary key `organization_id`, mapped to the entity's `id`) holds core details: unique `legal_name` (checked case-insensitively in `service/OrganizationWriter`), optional trade name, entity type, industry code, purpose, incorporation date, contact email/number, address and fiscal-year-end month, plus `status` (`OrganizationStatus`) and `is_active`. Members edit details with `PUT /organization`; `status` and `is_active` are not editable through the API and are not enforced yet (an inactive or suspended organization still works). `OrganizationWriter` is shared by sign-up and the organization endpoints.
- **Multi-tenancy.** Shared tables with an `organization_id` column on `audits` and `findings`. A user belongs to organizations through `memberships`, and there are no roles: every member can do everything in their organization. Scoping is explicit, not automatic: services get the organization from `security/CurrentUser.organizationId()` and pass it to org-aware repository methods (`findByIdAndOrganizationId`, `Specs.inOrganization`, ...). Never call the inherited id-only `findById`/`existsById`/`count` on `AuditRepository` or `FindingRepository` from a service, and set the organization in the entity constructor on create. A record in another organization returns 404, not 403. A composite FK `findings(audit_id, organization_id) → audits(id, organization_id)` stops a finding from pointing at another organization's audit. `TenancyApiTest` covers cross-organization reads and writes; extend it when adding an endpoint.
- **Auth.** `POST /auth/signup` and `/auth/login` return an HS256 JWT signed by `security/TokenService` (subject = user id, `org` claim = organization id); everything except those, health and the dev tools needs `Authorization: Bearer`. Switching organization (`POST /auth/switch`) issues a new token. `SecurityConfig.membershipCheck` re-checks the membership on every request, so removing a member revokes their tokens at once. Passwords are BCrypt and emails are stored lower-case. 401s are problem+json too (custom entry point).
- **Front-end contract.** JSON field names and values must match the front end's `Audit` / `Finding` types in `../auditapp-v2/src/lib/data.ts`. Enums (`AuditStatus`, `Severity`) are stored by constant name (`IN_PROGRESS`) but serialised with labels (`"In Progress"`) via `@JsonValue`. `@JsonCreator from()` (through `domain/Labels.parse`) accepts either form, case-insensitively, and `config/WebConfig` registers the same converters for query params. A new enum should follow this pattern and get a converter in `WebConfig`.
- **Filtering.** List filters are JPA `Specification`s in `repository/Specs`, where a null argument means "no filter". The `q` text search deliberately mirrors the front end's `filterAudits` (case-insensitive substring of id, title, department, auditor), so keep the two in sync.
- **IDs.** Audit and finding IDs are strings assigned by the server from global DB sequences (shared by all organizations, so an organization sees gaps): `"AUD-" + audit_seq` (starts at 1001) and `"F-" + finding_seq` (starts at 201), using native `nextval` queries on the repositories. Seed data occupies the low numbers. Organization, user and membership IDs are random UUIDs.
- **Concurrency.** Entities have a `@Version` column. `OptimisticLockingFailureException`, `DataIntegrityViolationException` and `service/ConflictException` map to 409; `ForbiddenException` to 403; `BadCredentialsException` to 401.
- **Errors.** `web/ApiExceptionHandler` returns everything as RFC 9457 `application/problem+json`. Bean-validation failures add an `errors` map from field to message. Cross-field rules are `@AssertTrue` methods on the request record (for example `AuditRequest.isDateRangeValid`), marked `@JsonIgnore`.
- **Deletes.** Deleting an audit deletes its findings explicitly in `AuditService.delete`. There is also `ON DELETE CASCADE` in the schema.
- **Dashboard.** `service/DashboardService` reproduces the front end's dashboard metrics: "active" means `IN_PROGRESS` + `IN_REVIEW`, "high risk" means unresolved `HIGH` + `CRITICAL`, and the severity breakdown counts only unresolved findings. If you change a definition here, change it in the front end too, and the reverse.
- **API reference.** `README.md` has the endpoint table and the env var table. Keep it current when you add or change an endpoint.

## Database

- Flyway is the only thing that creates or changes the schema, in every profile: `ddl-auto=validate`, `generate-ddl=false` and `spring.sql.init.mode=never` (no `schema.sql` / `data.sql`). Every new table, column, index or sequence needs a migration: add `src/main/resources/db/migration/V<n>__description.sql` and never edit an applied one. `SchemaManagedByFlywayTest` lists the expected tables and sequences and fails on anything else, so add new ones there too.
- Migrations must run on both PostgreSQL (dev, staging, prod) and H2 in PostgreSQL mode (tests). H2 is a test-scope dependency only.
- The dev and `--profile app` (staging) runs share the `pgdata` volume, but staging has no `db/seed` location, so Flyway's validation fails on a volume dev has seeded. Run `docker compose down -v` when switching.
- `V2__organizations_and_users.sql` moves rows that existed before multi-tenancy into a "Default organization" (fixed id `...0001`, created only if there are audits). It has no members until someone is added through the API.
- Seed data is `db/seed/V1_1__seed_sample_data.sql` (Acme's audits) and `V2_1__seed_organizations_and_users.sql`, which share Flyway's version space with `db/migration`. Pick new migration versions that don't collide with them. V2_1 renames the default organization to Acme Corp, adds Globex Ltd with AUD-1007/1008 and F-208/209, and two users with password `password123`: `demo@auditapp.dev` (Acme and Globex) and `owner@globex.dev` (Globex only). `V3_1__seed_organization_details.sql` fills in both organizations' details. If you change the seed, update the front end's `src/test/fixtures.ts` and the counts asserted in `AuditApiTest`.

## Tests

`AuditApiTest` (audit/finding endpoints, signed in as the demo user in Acme), `TenancyApiTest` (auth, membership, isolation), `OrganizationDetailsApiTest` (organization details) and `SchemaManagedByFlywayTest` (expected tables, sequences and migration versions) are `@SpringBootTest` + MockMvc integration tests against the seeded dev schema. They get real tokens through `TestLogins.token(mvc, email)` and send them with `TestLogins.as(token, request)`. Each test method gets a fresh context with its own uniquely named in-memory H2 database (`@DirtiesContext` AFTER_EACH_TEST_METHOD), so tests can mutate data freely but are slow to add in bulk. That unique DB comes from a `spring.datasource.url` override in each class's `@SpringBootTest(properties = ...)`, which repeats the dev URL's H2 mode flags. If you change those flags in `application.yml`, change them there too. There is no `src/test/resources`. Assertions rely on the seed contents (for example 6 audits in Acme, `AUD-1001` first, new IDs starting at `AUD-1009` / `F-210`).

## Not implemented

No pagination (lists return everything), no roles or invitations (members are added by the email of an existing account), no refresh tokens or password reset, and no rate limiting on login.
