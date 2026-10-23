# audit-service

Spring Boot 3.5 / Java 21 REST backend for the AuditApp front end (`../auditapp-v2`). It serves audits, findings and dashboard metrics at `http://localhost:4000/api`, the front end's default `NEXT_PUBLIC_API_BASE_URL`.

It is multi-tenant: users sign up into an organization, can belong to several, and only ever see the audits and findings of the organization their token is for.

JSON uses the same field names and values as the front end's `Audit` and `Finding` types in `src/lib/data.ts`. For example, `status` is `"In Progress"`, `severity` is `"High"`, and dates are `YYYY-MM-DD`. Responses also include `createdAt` and `updatedAt`.

## Run

Requires JDK 21, Maven 3.9+ and Docker (Docker is only needed to run the service, not for tests).

```bash
mvn spring-boot:run                       # dev profile: starts PostgreSQL in Docker and seeds sample data
mvn verify                                # build + integration tests (in-memory H2, no Docker)
docker compose down -v                    # wipe the local database; the next start re-seeds it
docker compose --profile app up --build   # service + PostgreSQL in containers, staging profile
```

In the dev profile, Spring Boot's Docker Compose support starts the `db` service from `docker-compose.yml` (PostgreSQL 16 at `jdbc:postgresql://localhost:5432/audit`, user `myuser`, password `mypassword`; set `DB_HOST_PORT` if 5432 is taken) and connects to it. The container keeps running, and its data persists, between restarts. Flyway applies the migrations and the sample data on the first start only. To use a database you run yourself, set `SPRING_DOCKER_COMPOSE_ENABLED=false` and `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`.

- Swagger UI: http://localhost:4000/api/docs (disabled in `prod`)
- Health: http://localhost:4000/api/actuator/health (`/liveness`, `/readiness`)

## Profiles and configuration

| Profile | Database | Sample data | Swagger |
| --- | --- | --- | --- |
| `dev` (default) | PostgreSQL in Docker (`docker-compose.yml`) | yes | yes |
| `staging` | PostgreSQL | no | yes |
| `prod` | PostgreSQL | no | no |

Set the profile with `SPRING_PROFILES_ACTIVE`. Environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `4000` | HTTP port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated front-end origins |
| `DB_URL` | dev: `jdbc:postgresql://localhost:5432/audit`; none in staging/prod | e.g. `jdbc:postgresql://host:5432/audit` |
| `DB_HOST_PORT` | `5432` | Host port docker-compose publishes PostgreSQL on |
| `DB_USERNAME` / `DB_PASSWORD` | none (staging/prod) | Database credentials |
| `DB_POOL_SIZE` | `10` | Hikari pool size |
| `JWT_SECRET` | dev only: built-in | HMAC key for access tokens, at least 32 bytes. Required in staging/prod. |
| `JWT_TTL` | `8h` | How long an access token is valid |

The schema is managed by Flyway (`src/main/resources/db/migration`). Hibernate only validates it. Add a new `V<n>__description.sql` for every schema change and never edit an applied one. Seed data lives in `db/seed` and is loaded only by the `dev` profile.

## Authentication and organizations

Sign up or log in to get a token, then send it as `Authorization: Bearer <token>` on every other request (Swagger UI has an Authorize button). A token works in one organization; `POST /auth/switch` returns a token for another one the user belongs to. Records of other organizations answer 404.

The dev profile seeds two users, both with password `password123`:

| Email | Organizations |
| --- | --- |
| `demo@auditapp.dev` | Acme Corp (the six sample audits), Globex Ltd |
| `owner@globex.dev` | Globex Ltd |

On an existing database, migration `V2` moves the audits and findings created before multi-tenancy into an organization called "Default organization", which has no members yet. To claim it, sign up, then insert a membership row for your user, for example:

```sql
INSERT INTO memberships (id, user_id, organization_id)
SELECT gen_random_uuid()::text, id, '00000000-0000-0000-0000-000000000001' FROM users WHERE email = 'you@example.com';
```

After that you can add colleagues from the app.

## API

All paths are relative to `/api`. Errors are returned as RFC 9457 `application/problem+json`, and validation errors include an `errors` map from field to message. Everything except sign-up, login and health needs a token (401 otherwise).

| Method | Path | Notes |
| --- | --- | --- |
| POST | `/auth/signup` | `{name, email, password, organizationLegalName}`. 201 + token; creates the user and their first organization. 409 if the email is taken. |
| POST | `/auth/login` | `{email, password, organizationId?}`. Token for that organization, or the first by name. 401 on bad credentials. |
| GET | `/auth/me` | Current user, organization, and all of the user's organizations |
| POST | `/auth/switch` | `{organizationId}`. Token for another organization the user belongs to |
| POST | `/organizations` | Organization details (below). 201 + token for the new organization; the caller is its first member |
| GET | `/organization` | Details of the current organization |
| PUT | `/organization` | Replace the current organization's details. `status` and `isActive` can't be changed here |
| GET | `/organization/members` | Members of the current organization |
| POST | `/organization/members` | `{email}`. Adds an existing account. 404 if no account, 409 if already a member |
| DELETE | `/organization/members/{userId}` | Removes a member; their tokens stop working at once. 409 for the last member |
| GET | `/audits?q=&status=` | `q` matches id, title, department or auditor, like `filterAudits`. |
| GET | `/audits/{id}` | 404 if unknown |
| GET | `/audits/{id}/findings` | Findings for one audit |
| POST | `/audits` | 201 + `Location`; id assigned (`AUD-1009`, ...) from a sequence shared by all organizations |
| PUT | `/audits/{id}` | Full replace |
| DELETE | `/audits/{id}` | Also deletes its findings |
| GET | `/findings?q=&auditId=&severity=&resolved=` | All filters optional |
| GET | `/findings/{id}` | |
| POST | `/findings` | 201; id assigned (`F-210`, ...); `resolved` defaults to false. The audit must be in the caller's organization |
| PUT | `/findings/{id}` | Full replace |
| PATCH | `/findings/{id}` | `{"resolved": true}` to resolve, `false` to reopen |
| DELETE | `/findings/{id}` | |
| GET | `/dashboard/summary` | Totals, active/completed audits, open and high-risk findings, breakdowns by status and severity |

Enum values in query parameters and bodies accept either the label (`In Progress`) or the constant (`IN_PROGRESS`).

Organization details (`POST /organizations`, `PUT /organization`). Only `legalName` is required, and it must be unique across all organizations (ignoring case, 409 otherwise). Blank or missing optional fields are stored as null. Responses add `id`, `status` (`Pending`, `Active`, `Suspended`, `Dissolved`; new organizations are `Active`), `isActive` (default true), `createdAt` and `updatedAt`.

```json
{
  "legalName": "Acme Corporation Pvt Ltd",
  "tradeName": "Acme",
  "entityType": "Corporation",
  "industryCode": "332510",
  "businessPurpose": "Tools and hardware.",
  "dateOfIncorporation": "2009-04-01",
  "contactEmail": "compliance@acme.test",
  "contactNumber": "+91 80 4000 1000",
  "address": "12 MG Road, Bengaluru",
  "fiscalYearEndMonth": 3
}
```

`entityType` is one of `Sole Proprietorship`, `Partnership`, `LLC`, `Corporation`, `Non-Profit`, `Government`, `Other`. `dateOfIncorporation` can't be in the future, and `fiscalYearEndMonth` is 1 to 12.

Example request body for an audit:

```json
{
  "title": "Cash Handling Review",
  "department": "Finance",
  "auditor": "Priya Sharma",
  "status": "Planned",
  "startDate": "2026-12-01",
  "endDate": "2026-12-15",
  "progress": 0,
  "description": "Petty cash counts"
}
```

## Layout

```
com.auditapp.audit
├── domain       JPA entities (Audit, Finding) and enums with JSON labels
├── repository   Spring Data repositories + Specs (list filters)
├── security     JWT issuing, Spring Security config, CurrentUser (the caller's organization)
├── service      Business logic scoped to the caller's organization, id generation
├── web          Controllers, ApiExceptionHandler (problem+json)
│   └── dto      Request/response records
└── config       CORS and query-param enum conversion
```

## Not included yet

- **Roles and invitations.** Every member can do everything in their organization, and members are added by the email of an existing account (no invitation emails).
- **Refresh tokens, password reset, login rate limiting.** Tokens simply expire after `JWT_TTL`.
- **Pagination.** Lists return everything, which is fine at the front end's current scale.
