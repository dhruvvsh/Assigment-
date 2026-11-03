# TaskFlow

A small team task tracker: a Spring Boot + Kotlin REST API and a React + TypeScript frontend.

> Work in progress. The full README (setup, tests, architecture, patterns, trade-offs) is added in the final part.

## Repository layout

| Folder      | Contents                                    |
|-------------|---------------------------------------------|
| `backend/`  | Spring Boot 3.5, Kotlin, Maven, JPA, PostgreSQL |
| `frontend/` | React 19, TypeScript, Vite                  |

## Prerequisites

- JDK 21
- Node 20+
- Docker (runs PostgreSQL locally, and is required by the backend integration tests via Testcontainers)

> **Colima users:** point Testcontainers at your socket before running backend tests:
> `export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock`

## Quick start (development)

### Database

The backend reads its database settings from environment variables, or from a git-ignored
`backend/.env` file. Pick one:

**Option A: Supabase (PostgreSQL).** Create `backend/.env` with:

```properties
DB_URL=jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.<project-ref>
DB_PASSWORD=<database password>
JWT_SECRET=<random string, 32+ characters>
```

Use the **Session pooler** values from Supabase (Project Settings > Database > Connection string).

**Option B: local PostgreSQL in Docker** (no `.env` needed, defaults match):

```bash
docker compose up -d db
```

### Run

```bash
# Backend  -> http://localhost:8080
cd backend && ./mvnw spring-boot:run

# Frontend -> http://localhost:5173 (proxies /api to the backend)
cd frontend && npm install && npm run dev
```
