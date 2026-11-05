# TaskFlow

A small team task tracker. Register, create projects, and manage tasks on a board with **To Do / In Progress / Done** columns.

- **Backend:** Spring Boot 3.5 + Kotlin REST API with JWT authentication and PostgreSQL
- **Frontend:** React + TypeScript single-page app

## Features

- Register and log in; routes are protected and an expired session is handled cleanly
- Projects: each user only sees and changes their own
- Project board with a summary bar (counts per status and overdue tasks)
- Create, edit, delete and move tasks between columns (status dropdown)
- Filter by priority and assignee; sort by due date or priority; paginated API
- Overdue tasks are highlighted
- Activity log on each task (who changed the status, and when)
- Loading, empty and error states for every request; works on desktop and mobile widths

## Quick start

Requires Docker.

```bash
docker compose up --build
```

Open **http://localhost:3000** and log in with the seeded demo account:

| Email | Password |
|---|---|
| `demo@taskflow.com` | `Password123!` |

This starts PostgreSQL, the backend (port 8080) and the frontend (port 3000, served by nginx, which forwards `/api` to the backend). A second seeded user, `priya@taskflow.com` (same password), is available as an assignee.

If a port is already taken, override it: `FRONTEND_PORT=8081 BACKEND_PORT=8082 DB_PORT=5433 docker compose up --build`.

## Local development

Requires JDK 21, Node 20+ and Docker (for the database).

```bash
# 1. Database -> localhost:5432
docker compose up -d db

# 2. Backend -> http://localhost:8080
cd backend && ./mvnw spring-boot:run

# 3. Frontend -> http://localhost:5173 (Vite proxies /api to the backend)
cd frontend && npm install && npm run dev
```

### Configuration

All settings come from environment variables (or a git-ignored `backend/.env` file). Nothing secret is committed.

| Variable | Default (local only) | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/taskflow` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `taskflow` / `taskflow` | Database credentials |
| `JWT_SECRET` | a local-only placeholder | Token signing key, **at least 32 characters**. Always set it outside local use. |
| `JWT_EXPIRATION_MINUTES` | `1440` | Token lifetime (24 h) |
| `SEED_DEMO_DATA` | `true` | Create the demo user and sample projects on first start |
| `PORT` | `8080` | Backend port |

**Using a hosted database such as Supabase:** create `backend/.env` (or a root `.env` for Docker Compose) with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` and `JWT_SECRET`. Use the provider's session-pooler JDBC URL and add `?sslmode=require`. If you use Supabase, also disable its Data API or enable row-level security, because tables in the `public` schema are otherwise reachable through it.

## Running the tests

```bash
# Backend: 52 tests (29 unit with JUnit 5 + MockK, 23 integration with @SpringBootTest)
cd backend && ./mvnw test

# Frontend: 11 tests with Jest + React Testing Library
cd frontend && npm test
```

The backend integration tests start a throwaway PostgreSQL container with Testcontainers, so **Docker must be running**. They never use your local `.env` or database. With Colima, first run:
`export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock`

## Architecture

```
React SPA  --HTTP + JWT-->  Spring Boot API  --JPA-->  PostgreSQL
```

### Backend (`backend/src/main/kotlin/com/taskflow/`)

Layered: **controller -> service -> repository**.

| Package | Responsibility |
|---|---|
| `controller` | HTTP only: parse the request, call a service, return a DTO |
| `service` | Business rules (ownership checks, status-change logging, summary) |
| `repository` | Spring Data JPA repositories and query specifications |
| `domain` | JPA entities (`User`, `Project`, `Task`, `TaskActivity`) and enums |
| `dto` | Request/response classes and mappers; entities are never exposed |
| `sorting` | Task sorting strategies |
| `security`, `config` | JWT service and filter, Spring Security setup, demo data seeder |
| `exception` | `@RestControllerAdvice` handler and the single error response shape |

**Authentication.** `POST /api/auth/login` checks the BCrypt-hashed password and returns a signed JWT. A filter validates it on every other request; invalid or missing tokens get a JSON 401.

**Authorisation.** Project and task lookups are scoped to the current user in the query itself, so another user's project or task is reported as **404** (not 403), which avoids revealing that it exists.

**Errors.** Every failure uses one shape: `{ timestamp, status, error, message, path, fieldErrors[] }`. Validation failures are 400 with per-field messages.

### Frontend (`frontend/src/`)

| Folder | Responsibility |
|---|---|
| `api` | The only code that talks to the backend: an Axios client that attaches the JWT and reports 401s, plus typed functions per resource |
| `hooks` | React Query hooks (`useProjects`, `useProjectTasks`, ...) |
| `context` | Auth session (token and user) |
| `components` | Reusable UI: `Button`, `Modal`, `TaskCard`, `SummaryBar`, `FilterBar`, `TaskForm`, ... |
| `pages` | Login, Register, Projects, Project board |
| `lib` | Validation, formatting, filter helpers, session storage |

**State management: React Query + a small Context.** Most state in this app is server data (projects, tasks, summary). React Query gives loading and error states, caching and refetching for free, so each screen handles "loading / empty / error" without hand-written reducers. After a change (for example moving a task), one invalidation refreshes both the board and the summary so they cannot disagree. Context holds only the login session. Redux would add boilerplate without benefit here.

**Expired tokens.** The stored token's expiry is checked at startup, and any 401 from the API ends the session and shows "Your session expired" on the login page.

## API

All endpoints except register and login require `Authorization: Bearer <token>`.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Create a user (also returns a token) |
| POST | `/api/auth/login` | Return a JWT |
| GET | `/api/projects` | List the current user's projects |
| POST | `/api/projects` | Create a project |
| GET | `/api/projects/{id}/tasks` | List tasks. Query: `status`, `priority`, `assigneeId`, `sortBy` (`dueDate`\|`priority`), `direction` (`asc`\|`desc`), `page`, `size` (max 100) |
| POST | `/api/projects/{id}/tasks` | Create a task |
| PUT | `/api/tasks/{id}` | Update a task |
| PATCH | `/api/tasks/{id}/status` | Change only the status |
| DELETE | `/api/tasks/{id}` | Delete a task |
| GET | `/api/projects/{id}/summary` | Tasks per status and number overdue |
| GET | `/api/users` | *Extra:* people who can be assigned (needed by the assignee dropdowns) |
| GET | `/api/tasks/{id}/activity` | *Extra (bonus):* status-change history |

## Design patterns

- **Repository:** Spring Data JPA repositories isolate all database access.
- **Strategy:** `TaskSortStrategy` with `DueDateSortStrategy` and `PrioritySortStrategy`; a resolver picks one from the `sortBy` parameter. A new sort option is one new class. Priority needs its own strategy because it sorts by rank, then by due date.
- **Specification:** `TaskSpecifications` builds the task query from only the filters that were supplied.
- **Mapper (factory):** `TaskMapper` builds responses from entities and is the single place that defines "overdue".
- **DTOs:** separate request and response classes keep entities and password hashes out of the API.

## Bonus items

Done: **Docker Compose** (database, backend and frontend with one command) and the **task activity log**.
Not done: OpenAPI/Swagger, Flyway migrations, GitHub Actions.

## Assumptions

- Any registered user can be chosen as an assignee; projects themselves are private to their owner.
- A past due date is rejected when **creating** a task but allowed when editing, because a task can legitimately become overdue. Overdue means due before today and not Done.
- A task with no due date sorts last when sorting by due date ascending (PostgreSQL's default).
- The status dropdown replaces drag-and-drop, which the assignment allows.
- The seeded demo password is intentional, documented demo data.

## Trade-offs and limitations

- **Priority is stored by ordinal** (LOW < MEDIUM < HIGH) so the database sorts it correctly. Reordering the enum would corrupt data, so it carries a warning comment.
- **The JWT is kept in `localStorage`.** It is simple, but an XSS bug could read it; an httpOnly cookie would be safer in production. There are no refresh tokens, so a session lasts 24 hours.
- **The board loads at most 100 tasks** (the API's page-size cap) and shows a banner if there are more. The API itself is fully paginated.
- **Schema is created by Hibernate** (`ddl-auto: update`) rather than migrations, which is fine for this scope but not for production.
- **`GET /api/users` lists every user** to any logged-in user, which suits a small team but not a public product.
- The compose file ships local-only default credentials; set real values for anything shared.

## What I would do next

- Flyway migrations and a GitHub Actions pipeline that builds and runs both test suites
- Drag-and-drop between columns, with optimistic updates
- Project sharing and membership (so assignees are limited to project members)
- Refresh tokens with an httpOnly cookie
- Pagination controls and search on the board; OpenAPI documentation
