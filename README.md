# GymPro

Gym management system built with **Spring Boot microservices**.

## Architecture

Client → API Gateway (JWT check) → Services (each with its own MySQL database)

## Services

| Service | Port | What it does |
|---|---|---|
| discovery-server | 8761 | Eureka service registry |
| api-gateway | 8080 | Single entry point, routing, JWT validation |
| auth-service | 8081 | Register, login, JWT token, roles (ADMIN, TRAINER, MEMBER) |
| member-service | 8082 | Membership plans, member profiles, subscriptions, daily expiry job, admin dashboard |
| attendance-service | 8083 | Check-in / check-out, uses OpenFeign to verify an active membership |
| workout-service | 8085 | Trainers create workout and diet plans, members view their own |

## Tech Stack

Java 21, Spring Boot 4, Spring Cloud (Gateway, Eureka, OpenFeign), Spring Security, JWT, Spring Data JPA, MySQL, Swagger (springdoc OpenAPI), Maven

A parent `pom.xml` groups all services so they open together as one project in Eclipse.

## Key Features

- JWT authentication with role-based access (ADMIN, TRAINER, MEMBER)
- Each service has its own database
- Service-to-service calls with OpenFeign
- Scheduled job that expires old subscriptions
- Check-in is rejected if the membership is expired
- Trainers create workout and diet plans, and members see only their own
- Global exception handling: every error returns the same clean JSON format
- Admin dashboard: total members, active/expired subscriptions, revenue, and memberships expiring within 7 days
- Swagger UI on every service for API documentation and testing

## How to Run

1. Create the MySQL databases: `auth_db`, `member_db`, `attendance_db`, `workout_db`
2. Set the environment variable `DB_PASSWORD` to your MySQL password
3. Start the services in this order: discovery-server, auth-service, member-service, attendance-service, workout-service, api-gateway
4. Open http://localhost:8761 to see the registered services
5. Send all API requests to http://localhost:8080

## API Documentation (Swagger UI)

| Service | Swagger URL |
|---|---|
| auth-service | http://localhost:8081/swagger-ui.html |
| member-service | http://localhost:8082/swagger-ui.html |
| attendance-service | http://localhost:8083/swagger-ui.html |
| workout-service | http://localhost:8085/swagger-ui.html |

Services behind the gateway read the headers `X-User-Email` and `X-User-Role`. In Swagger, fill these in by hand (for example `X-User-Role: ADMIN`). Through the gateway on port 8080 they are added automatically from the JWT.

## Main API Endpoints

| Method | Endpoint | Who can use it |
|---|---|---|
| POST | /auth/register | Everyone |
| POST | /auth/login | Everyone |
| POST | /plans | ADMIN |
| GET | /plans | Any logged-in user |
| GET | /plans/{id} | Any logged-in user |
| PUT | /plans/{id} | ADMIN |
| DELETE | /plans/{id} | ADMIN |
| POST | /members/profile | Logged-in user |
| GET | /members/me | Logged-in user |
| GET | /members | ADMIN, TRAINER |
| POST | /members/subscribe/{planId} | MEMBER |
| GET | /members/{id}/subscription | Logged-in user |
| GET | /members/dashboard | ADMIN |
| POST | /attendance/check-in | MEMBER (active subscription needed) |
| POST | /attendance/check-out | MEMBER |
| GET | /attendance/me | MEMBER |
| GET | /attendance/member/{id} | ADMIN, TRAINER |
| POST | /workouts/plans | TRAINER, ADMIN |
| POST | /workouts/diets | TRAINER, ADMIN |
| GET | /workouts/member?email= | TRAINER, ADMIN |
| DELETE | /workouts/plans/{id} | TRAINER, ADMIN |
| GET | /workouts/my-plans | MEMBER |
| GET | /workouts/my-diets | MEMBER |

## Author

Teja - https://github.com/teja0093