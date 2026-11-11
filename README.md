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
| member-service | 8082 | Membership plans, member profiles, subscriptions, daily expiry job |
| attendance-service | 8083 | Check-in / check-out, uses OpenFeign to verify an active membership |

## Tech Stack

Java 21, Spring Boot 4, Spring Cloud (Gateway, Eureka, OpenFeign), Spring Security, JWT, Spring Data JPA, MySQL, Maven

## Key Features

- JWT authentication with role-based access
- Each service has its own database
- Service-to-service calls with OpenFeign
- Scheduled job that expires old subscriptions
- Check-in is rejected if the membership is expired

## How to Run

1. Create the MySQL databases: `auth_db`, `member_db`, `attendance_db`
2. Set the environment variable `DB_PASSWORD` to your MySQL password
3. Start the services in this order: discovery-server, auth-service, member-service, attendance-service, api-gateway
4. Open http://localhost:8761 to see the registered services
5. Send all API requests to http://localhost:8080

## Author

Teja - https://github.com/teja0093