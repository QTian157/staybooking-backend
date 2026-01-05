# StayBooking Backend

A Spring Boot backend for an Airbnb-style booking platform, focusing on **stateless authentication**, **role-based authorization**, and **clean backend architecture**.

This project is built as a **production-oriented learning backend**, emphasizing real-world concerns such as security, external integrations, and frontend–backend separation.

---

## Key Features

- Stateless authentication using JWT
- Role-based access control (HOST / GUEST)
- RESTful APIs for authentication, stays, search, and reservations
- MySQL as primary datastore (AWS RDS)
- Google Cloud Storage for image uploads
- Elasticsearch integration for search
- Explicit CORS handling for frontend integration

---

## Tech Stack

- Java + Spring Boot
- Spring Security + JWT
- MySQL (AWS RDS)
- Google Cloud Storage
- Elasticsearch
- Maven

---

## Architecture Overview

The backend follows a layered architecture:

```
Controller → Service → DAO / Repository
↓
Security (JWT, Roles)
```

- Controllers handle request validation and authentication context
- Services encapsulate business logic
- Persistence is handled via JPA repositories
- Security is centralized and enforced through Spring Security filters

The application is fully **stateless**, with no server-side session storage.

---

## Authentication & Authorization

Authentication is handled via JWT tokens.

Two roles are supported:

- `ROLE_GUEST`
- `ROLE_HOST`

### Public Endpoints

- `POST /register/*`
- `POST /authenticate/*`

### Role-Based Access Control

Authorization rules are enforced centrally via Spring Security:

- Hosts can manage stays (`/stays/**`)
- Guests can search stays and manage reservations (`/search`, `/reservations/**`)
- All other endpoints require authentication

```md
JWT tokens must be included in requests as:
Authorization: Bearer <JWT_TOKEN>

```

---

## Reservation Logic

- Reservations are always associated with the currently authenticated user
- Server-side validation ensures:
    - Check-in date is before check-out date
    - Dates are not in the past
- Authorization checks ensure users can only access or modify their own reservations

This logic is enforced to maintain data integrity and security.

---

## Configuration & External Services

The backend integrates with multiple external services, configured via `application.properties` and environment variables:

- MySQL database (AWS RDS)
- JWT signing configuration
- Google Cloud Storage bucket
- Elasticsearch
- Geocoding API

Sensitive information (credentials, secrets, API keys) is not committed to version control.

---

## CORS & Security Settings

- CSRF is disabled due to stateless JWT authentication
- Sessions are disabled (`SessionCreationPolicy.STATELESS`)
- CORS is handled explicitly to support frontend development on a separate origin

This enables smooth integration with a React frontend running on a different port.

---

## Running Locally

1. Configure database credentials and external service settings in `application.properties`
2. Start the backend:

```bash
mvn spring-boot:run
```

3. Backend will be available at:

```arduino
http://localhost:8080

```
The frontend runs separately (typically on `http://localhost:3000`).

## Project Status

- This backend is an actively evolving project, focusing on:

- Backend architecture clarity

- Secure authentication and authorization

- Real-world service integrations

- Clean separation between frontend and backend

## License

For educational and personal use.
