# Deployment Guide

## Production Checklist

1. Replace `JWT_SECRET` with a strong secret from a vault.
2. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
3. Configure SMTP for email notifications.
4. Mount persistent storage for `UPLOAD_DIR` or replace the storage service with S3.
5. Set `CORS_ORIGINS` to the production frontend domain.
6. Put the backend behind HTTPS using a reverse proxy or cloud load balancer.
7. Run database migrations using `database/schema.sql` or Flyway in a production pipeline.

## Docker

```bash
cd docker
docker compose up --build -d
```

## Manual Backend

```bash
cd backend
mvn clean package
java -jar target/crop-disease-portal-1.0.0.jar
```

## Manual Frontend

```bash
cd frontend
npm install
npm run build
```
