# GymPro

Gym management system built with **Spring Boot microservices**.

## Architecture

Client → API Gateway (JWT check) → Services (each with its own MySQL database)

```
                    Client (Postman / Swagger / React later)
                                  |
                       API Gateway (8080)  validates JWT
                                  |
   ---------------------------------------------------------------------
   |            |               |               |              |
auth-service  member-service  attendance-    workout-      payment-
(8081)        (8082)          service (8083) service (8085) service (8084)
   |            |               |               |              |
auth_db       member_db      attendance_db   workout_db     payment_db

        Discovery Server (Eureka, 8761): all services register here
```

## Services

| Service | Port | What it does |
|---|---|---|
| discovery-server | 8761 | Eureka service registry |
| api-gateway | 8080 | Single entry point, routing, JWT validation |
| auth-service | 8081 | Register, login, JWT token, roles (ADMIN, TRAINER, MEMBER) |
| member-service | 8082 | Membership plans, member profiles, subscriptions, daily expiry job, admin dashboard |
| attendance-service | 8083 | Check-in / check-out, uses OpenFeign to verify an active membership |
| payment-service | 8084 | Creates payment orders, verifies payments (Razorpay or mock mode), activates the subscription |
| workout-service | 8085 | Trainers create workout and diet plans, members view their own |

## Tech Stack

Java 21, Spring Boot 4, Spring Cloud (Gateway, Eureka, OpenFeign), Spring Security, JWT, Spring Data JPA, MySQL, Razorpay, Swagger (springdoc OpenAPI), Maven

A parent `pom.xml` groups all services so they open together as one project in Eclipse.

## Key Features

- JWT authentication with role-based access (ADMIN, TRAINER, MEMBER)
- Each service has its own database
- Service-to-service calls with OpenFeign
- Scheduled job that expires old subscriptions
- Check-in is rejected if the membership is expired
- Trainers create workout and diet plans, and members see only their own
- Payment flow: create order, verify payment, then the subscription is activated automatically
- Razorpay integration with a **mock mode** for local development (no real keys needed)
- A member cannot subscribe without paying: the subscribe endpoint accepts only internal calls from payment-service (shared internal key)
- Global exception handling: every error returns the same clean JSON format
- Admin dashboard: total members, active/expired subscriptions, revenue, and memberships expiring within 7 days
- Swagger UI on every service for API documentation and testing

## Payment Flow

1. Member calls `POST /payments/create-order` with a plan id. payment-service reads the plan price from member-service and creates an order.
2. Member pays (the Razorpay checkout page in the real app).
3. Member calls `POST /payments/verify` with the order id, payment id and signature.
4. If the signature is valid, the payment is marked `SUCCESS` and payment-service tells member-service to activate the subscription.

**Mock mode** (default, `RAZORPAY_MOCK=true`): no Razorpay account is needed, and the signature `mock_success` counts as a valid payment. For real Razorpay test mode, set `RAZORPAY_MOCK=false`, `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET`.

## How to Run

1. Create the MySQL databases: `auth_db`, `member_db`, `attendance_db`, `workout_db`, `payment_db`
2. Set the environment variable `DB_PASSWORD` to your MySQL password (for auth, member, attendance, workout and payment services)
3. Start the services in this order: discovery-server, auth-service, member-service, attendance-service, workout-service, payment-service, api-gateway
4. Open http://localhost:8761 to see the registered services
5. Send all API requests to http://localhost:8080

## API Documentation (Swagger UI)

| Service | Swagger URL |
|---|---|
| auth-service | http://localhost:8081/swagger-ui.html |
| member-service | http://localhost:8082/swagger-ui.html |
| attendance-service | http://localhost:8083/swagger-ui.html |
| payment-service | http://localhost:8084/swagger-ui.html |
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
| POST | /members/subscribe/{planId} | Internal only (called by payment-service after payment) |
| GET | /members/{id}/subscription | Logged-in user |
| GET | /members/dashboard | ADMIN |
| POST | /payments/create-order | MEMBER |
| POST | /payments/verify | MEMBER |
| GET | /payments/me | MEMBER |
| GET | /payments | ADMIN |
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