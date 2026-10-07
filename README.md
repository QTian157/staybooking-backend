# StayBooking Backend

A Spring Boot backend for a full-stack accommodation booking platform supporting
property management, location-based search, reservations, authentication, and
image management.

**Frontend Repository:** [StayBooking Frontend](https://github.com/QTian157/staybooking-frontend)  
**Backend Repository:** [StayBooking Backend](https://github.com/QTian157/staybooking-backend)  
**Live Demo:** Temporarily unavailable while the original cloud deployment is being updated

---

## Key Features

- Stateless JWT authentication and role-based authorization for HOST and GUEST users
- REST APIs for authentication, property management, search, and reservations
- Geospatial property search using Elasticsearch geo-distance queries
- MySQL/JPA filtering for guest capacity and reservation availability
- Transactional reservation workflow with collision detection and date-level availability tracking
- BCrypt password hashing with Spring Security
- Google Geocoding integration for property locations
- Google Cloud Storage integration for property images
- Explicit CORS configuration for frontend-backend integration

---

## Tech Stack

- Java
- Spring Boot
- Spring Data JPA / Hibernate
- Spring Security
- MySQL
- Elasticsearch
- JWT
- Google Cloud Storage
- Google Geocoding API
- Maven

---

## Architecture Overview

The backend follows a layered architecture:

Controller → Service → Repository  
　　　　　　　　　↓  
　　　　 MySQL / Elasticsearch

Security is handled centrally through Spring Security and JWT filters.

- Controllers handle HTTP requests and request validation
- Services contain business logic and application workflows
- Repositories provide persistent data access through Spring Data JPA
- DTOs define request and response contracts
- Centralized exception handling provides consistent API error responses
- Spring Security filters handle authentication and authorization

The application uses stateless authentication with no server-side user sessions.

---

## Authentication & Authorization

Authentication is handled using JWT tokens and Spring Security.

Two roles are supported:

- `ROLE_GUEST`
- `ROLE_HOST`

### Public Endpoints

- `POST /register/*`
- `POST /authenticate/*`

### Role-Based Access Control

Authorization rules are enforced through Spring Security.

- Hosts can manage stays
- Guests can search stays and manage reservations
- Protected endpoints require authentication

Authenticated requests include the JWT token in the authorization header:

`Authorization: Bearer <JWT_TOKEN>`

---

## Search

Property search combines Elasticsearch with relational data stored in MySQL.

### Search Flow

1. A location is converted to geographic coordinates using the Google Geocoding API.
2. Elasticsearch geo-distance queries identify nearby stays.
3. Candidate stay IDs are filtered through MySQL/JPA based on guest capacity and reservation availability.
4. Matching stays are returned to the frontend.

This allows Elasticsearch to handle geospatial search while relational booking
and availability data remain in MySQL.

---

## Reservation Logic

The reservation workflow validates booking requests and prevents conflicting reservations.

- Check-in must occur before check-out
- Reservation dates cannot be in the past
- Availability is tracked at the date level
- Collision checks prevent overlapping reservations
- Reservation operations are executed transactionally
- Reservations are associated with the authenticated guest
- Authorization checks prevent users from accessing or modifying another user's reservations

---

## External Services

The backend integrates with:

- MySQL for relational application data
- Elasticsearch for geospatial property search
- Google Geocoding API for address-to-coordinate conversion
- Google Cloud Storage for property image storage

Configuration and credentials are supplied through application configuration
and environment variables.

Sensitive information such as database credentials, JWT secrets, and API keys
is not committed to version control.

---

## CORS & Security

- JWT-based stateless authentication
- BCrypt password hashing
- Role-based authorization
- CSRF disabled for the stateless REST API
- `SessionCreationPolicy.STATELESS`
- Explicit CORS configuration for the separately hosted React frontend

---

## Frontend

The user interface is implemented as a separate React application.

**Frontend Repository:**  
[StayBooking Frontend](FRONTEND_GITHUB_URL)

The frontend communicates with this Spring Boot backend through REST APIs.

The original frontend deployment is hosted on Render:

https://staybooking-frontend.onrender.com/

The original backend/cloud infrastructure is currently inactive, so the hosted
frontend may not provide full application functionality until the deployment is restored.

---

## Running Locally

### Prerequisites

- Java
- Maven
- MySQL
- Elasticsearch
- Required Google API / Cloud Storage configuration

### Configuration

Configure the required database and external service settings using
`application.properties` or environment variables.

Required configuration includes:

- MySQL connection
- JWT signing configuration
- Elasticsearch connection
- Google Geocoding API configuration
- Google Cloud Storage configuration

### Start the Backend

Run:

`mvn spring-boot:run`

The backend will be available at:

`http://localhost:8080`

The React frontend runs separately and communicates with the backend through
REST APIs.

---

## Project Structure

The backend is organized into separate layers for API handling, business logic,
data access, security, and external integrations.

Typical structure:

src/main/java
├── controller
├── service
├── repository
├── model
├── security
├── config
├── exception
└── dto

This separation keeps HTTP handling, business logic, persistence, and security
concerns isolated and easier to maintain.

---

## Project Status

Core application functionality includes:

- User registration and authentication
- HOST / GUEST role-based authorization
- Property management
- Geospatial property search
- Guest-capacity and date-availability filtering
- Reservation management
- Collision prevention for overlapping reservations
- Image storage
- External geocoding integration

The original cloud deployment is currently inactive and is being updated.
The application can be configured to run locally with the required services.

---

## Related Repositories

- **Backend:** [StayBooking Backend](BACKEND_GITHUB_URL)
- **Frontend:** [StayBooking Frontend](FRONTEND_GITHUB_URL)

---

## License

For educational and personal use.
