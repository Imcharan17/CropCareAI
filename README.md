# AI Powered Crop Disease Detection and Farmer Support Portal

Full-stack enterprise web application for crop disease detection, farmer support tickets, doctor recommendations, analytics, notifications, audit-ready data modeling, and JWT-secured role-based access.

## Structure

- `frontend/` React, Vite, Tailwind CSS, Material UI, Redux Toolkit, React Router, Axios, Framer Motion, Chart.js
- `backend/` Spring Boot 3, Spring Security JWT, JPA/Hibernate, WebSocket/STOMP, Swagger
- `database/` MySQL schema and sample data
- `docs/` architecture, API, deployment, and Mermaid diagrams
- `documentation/` extended API docs, project report, and Postman collection
- `docker/` Full stack Docker Compose

## Quick Start

Start Docker Desktop, then run:

```bash
cd docker
docker compose up --build
```

On installations using legacy Compose, run `docker-compose up --build` instead.

Frontend: `http://localhost:5173`
Backend API: `http://localhost:8080/api`
Swagger: `http://localhost:8080/api/swagger-ui.html`

Demo users use password `Password@123`:

- `admin@crop.ai`
- `doctor@crop.ai`
- `farmer@crop.ai`

## Local Development

Backend:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile uses an embedded H2 database for zero-setup development. Docker Compose and the default profile use MySQL 8.

Frontend:

```bash
cd frontend
npm install
npm run dev
```

The Vite development server proxies `/api` to `http://localhost:8080`. The Nginx container proxies the same path to the backend service, including WebSocket upgrades.

## Verification

```bash
cd backend && mvn test
cd frontend && npm test -- --run && npm run build
cd docker && docker-compose config
```

The default AI provider is deterministic mock inference. Set `APP_AI_PROVIDER=tensorflow` only after mounting a trained model and implementing model loading in `TensorFlowProvider`.

## Render Deployment

This repository now includes a root `render.yaml` Blueprint for deploying on Render without changing the application frameworks:

- `crop-care-frontend`: Docker web service serving the Vite build through Nginx
- `crop-care-backend`: Docker web service running Spring Boot
- `crop-care-db`: Render Postgres database

The Render deployment uses:

- Render Postgres for persistent user and report data
- generated `JWT_SECRET`
- frontend Nginx proxying to the backend public Render URL

To deploy:

1. Push this repository to GitHub.
2. In Render, create a new Blueprint and point it at the repo.
3. Sync the Blueprint.
4. Set `GEMINI_API_KEY` on the backend service in the Render dashboard.
5. Open the `crop-care-frontend` service URL after the deploy completes.
