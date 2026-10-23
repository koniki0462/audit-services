# audit-service

Spring Boot 3.5 / Java 21 REST backend for the AuditApp front end (`../auditapp-v2`). It serves audits, findings and dashboard metrics at `http://localhost:4000/api`, the front end's default `NEXT_PUBLIC_API_BASE_URL`.

JSON uses the same field names and values as the front end's `Audit` and `Finding` types in `src/lib/data.ts`. For example, `status` is `"In Progress"`, `severity` is `"High"`, and dates are `YYYY-MM-DD`. Responses also include `createdAt` and `updatedAt`.

## Run

Requires JDK 21 and Maven 3.9+.

```bash
mvn spring-boot:run          # dev profile: in-memory H2 seeded with the front end's sample data
mvn verify                   # build + integration tests
docker compose up --build    # service + PostgreSQL, staging profile
```

- Swagger UI: http://localhost:4000/api/docs (disabled in `prod`)
- Health: http://localhost:4000/api/actuator/health (`/liveness`, `/readiness`)
- H2 console (dev only): http://localhost:4000/api/h2-console, JDBC URL `jdbc:h2:mem:auditdb`, user `sa`

## Profiles and configuration

| Profile | Database | Sample data | Swagger |
| --- | --- | --- | --- |
| `dev` (default) | H2 in memory, PostgreSQL mode | yes | yes |
| `staging` | PostgreSQL | no | yes |
| `prod` | PostgreSQL | no | no |

Set the profile with `SPRING_PROFILES_ACTIVE`. Environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `4000` | HTTP port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated front-end origins |
| `DB_URL` | none (staging/prod) | e.g. `jdbc:postgresql://host:5432/audit` |
| `DB_USERNAME` / `DB_PASSWORD` | none (staging/prod) | Database credentials |
| `DB_POOL_SIZE` | `10` | Hikari pool size |

The schema is managed by Flyway (`src/main/resources/db/migration`). Hibernate only validates it. Add a new `V<n>__description.sql` for every schema change and never edit an applied one. Seed data lives in `db/seed` and is loaded only by the `dev` profile.

## API

All paths are relative to `/api`. Errors are returned as RFC 9457 `application/problem+json`, and validation errors include an `errors` map from field to message.

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/audits?q=&status=` | `q` matches id, title, department or auditor, like `filterAudits`. |
| GET | `/audits/{id}` | 404 if unknown |
| GET | `/audits/{id}/findings` | Findings for one audit |
| POST | `/audits` | 201 + `Location`; id assigned (`AUD-1007`, ...) |
| PUT | `/audits/{id}` | Full replace |
| DELETE | `/audits/{id}` | Also deletes its findings |
| GET | `/findings?q=&auditId=&severity=&resolved=` | All filters optional |
| GET | `/findings/{id}` | |
| POST | `/findings` | 201; id assigned (`F-208`, ...); `resolved` defaults to false |
| PUT | `/findings/{id}` | Full replace |
| PATCH | `/findings/{id}` | `{"resolved": true}` to resolve, `false` to reopen |
| DELETE | `/findings/{id}` | |
| GET | `/dashboard/summary` | Totals, active/completed audits, open and high-risk findings, breakdowns by status and severity |

Enum values in query parameters and bodies accept either the label (`In Progress`) or the constant (`IN_PROGRESS`).

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
├── service      Business logic, id generation, NotFoundException
├── web          Controllers, ApiExceptionHandler (problem+json)
│   └── dto      Request/response records
└── config       CORS and query-param enum conversion
```

## Not included yet

- **Authentication.** Every endpoint is open. Add Spring Security (for example an OAuth2 resource server with JWT) before exposing this beyond a trusted network.
- **Pagination.** Lists return everything, which is fine at the front end's current scale.
