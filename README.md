# TaskFlow

A small team task tracker: a Spring Boot + Kotlin REST API and a React + TypeScript frontend.

> Work in progress. The full README (setup, tests, architecture, patterns, trade-offs) is added in the final part.

## Repository layout

| Folder      | Contents                                    |
|-------------|---------------------------------------------|
| `backend/`  | Spring Boot 3.5, Kotlin, Maven, JPA         |
| `frontend/` | React 19, TypeScript, Vite                  |

## Prerequisites

- JDK 21
- Node 20+
- Docker (optional, used in the final part)

## Quick start (development)

```bash
# Backend  -> http://localhost:8080
cd backend && ./mvnw spring-boot:run

# Frontend -> http://localhost:5173 (proxies /api to the backend)
cd frontend && npm install && npm run dev
```
