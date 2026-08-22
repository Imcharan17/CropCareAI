# Project Report

## Title

AI Powered Crop Disease Detection and Farmer Support Portal

## Objective

The system helps farmers identify crop diseases from uploaded images and connects them with agriculture experts through ticketing and real-time chat. Administrators manage users, assignments, reports, crop categories, analytics, and account status.

## Architecture

The frontend is a React/Vite SPA with Redux Toolkit state management, Tailwind and Material UI styling, Axios API access, Chart.js analytics, and responsive layouts. The backend is a layered Spring Boot 3 service with controllers, DTOs, services, repositories, JPA entities, security filters, global exception handling, and WebSocket/STOMP messaging. MySQL 8 stores normalized operational data.

## AI Module

The current AI implementation is a deterministic service stub behind `DiseaseDetectionService`. It validates images, stores uploads, returns disease intelligence, and persists predictions. A TensorFlow Serving, Python FastAPI, or gRPC model can be integrated by replacing `DiseaseDetectionServiceImpl.runPrediction`.

## Security

Spring Security issues JWTs after login. Endpoints are protected with role checks for `ROLE_ADMIN`, `ROLE_DOCTOR`, and `ROLE_FARMER`. Passwords are BCrypt encoded. Blocked users cannot authenticate.

## Quality

The code follows a layered structure, typed DTOs, Bean Validation, global exception handling, repository boundaries, and environment-based configuration.
