# Deployment Guide

## Local Docker

```bash
cd docker
docker compose up --build
```

Services:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/api/swagger-ui.html`
- MySQL: `localhost:3306`

Demo credentials:

- `admin@crop.ai` / `Password@123`
- `doctor@crop.ai` / `Password@123`
- `farmer@crop.ai` / `Password@123`

Set `APP_AI_PROVIDER=tensorflow` only after mounting a trained model and replacing the TensorFlow provider placeholder with model inference code.
