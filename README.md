# Creator Directory API

A **multi-tenant Creator Directory REST API** built using **Spring Boot** as part of the DigiTace Backend Challenge.

The project demonstrates how to securely build a SaaS-style backend where multiple agencies share the same platform while ensuring **strict tenant isolation**. Every request is scoped to the authenticated user's agency, preventing accidental data leakage between tenants.

---

## Repository

GitHub Repository:

https://github.com/rishitsharma07/creator-directory-api

---

# Features

## Multi-Tenant Architecture

- Agency-based tenant isolation
- Every request authenticated using `X-User-Id`
- Tenant context propagated through the request lifecycle
- Cross-agency data access prevented by design

---

## Creator Management

- View creators visible to your agency
- View creator by ID
- Add creators to your agency
- Update creator information
- Update agency-specific notes
- Remove only your agency's link to a creator
- Search creators
- Pagination support
- Filtering by:
    - Niche
    - Minimum Followers

---

## User Management

- View users within your agency
- Invite new users
- Role-based authorization

Roles:

- OWNER
- ADMIN
- MEMBER

---

## Plan Enforcement

FREE agencies can link a maximum of **5 creators**.

The 6th creator returns:

```
HTTP 403 Forbidden
```

PRO agencies have no creator limit.

---

## Rate Limiting

Every agency is limited to:

```
50 requests / minute
```

Requests exceeding the limit receive

```
HTTP 429 Too Many Requests
```

---

## Automated Integration Tests

The project contains automated integration tests proving:

- Tenant isolation
- Shared creator note isolation
- Unauthorized access
- Role permissions
- Free plan limits
- Cross-agency protection

---

# Tech Stack

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- PostgreSQL
- Maven
- Lombok
- Hibernate
- JUnit 5
- MockMvc
- Docker

---

# Project Structure

```
src
├── main
│   ├── config
│   ├── context
│   ├── controller
│   ├── domain
│   ├── dto
│   ├── exception
│   ├── interceptor
│   ├── repository
│   ├── service
│   └── CreatorDirectoryApiApplication.java
│
└── test
    └── CreatorDirectoryIsolationTests.java
```

---

# Running the Project

## 1 Clone Repository

```bash
git clone https://github.com/rishitsharma07/creator-directory-api.git

cd creator-directory-api
```

---

## 2 Start PostgreSQL

```bash
docker compose up -d
```

Verify container

```bash
docker ps
```

---

## 3 Run Application

```bash
mvn spring-boot:run
```

Application starts on

```
http://localhost:8080
```

---

# Seed Data

The project automatically seeds the database during startup using a `CommandLineRunner`.

The following data is inserted automatically:

### Agencies

- Nova Talent (FREE)
- Bright Star Agency (PRO)
- Solo Creators Co (FREE)

### Users

- owner@nova.com
- admin@nova.com
- owner@brightstar.com
- owner@solo.com

### Creators

- Priya Sharma
- Rahul Verma
- Ananya Iyer

During startup the application prints the generated UUIDs for testing.

Example:

```
TEST NOVA TALENT OWNER
TEST NOVA TALENT ADMIN
TEST BRIGHT STAR
TEST SOLO CREATORS
```

Use those UUIDs as the value of the

```
X-User-Id
```

header while testing.

---

# Authentication

This project intentionally does **not** implement password authentication.

Every request must include

```
X-User-Id: <uuid>
```

The API uses this header to

- authenticate the user
- determine their agency
- determine their role

---

# REST Endpoints

## Creators

| Method | Endpoint | Description |
|---------|----------|-------------|
| GET | `/creators` | List creators visible to caller |
| GET | `/creators/{id}` | View single creator |
| POST | `/creators` | Create creator |
| PATCH | `/creators/{id}` | Update creator / agency notes |
| DELETE | `/creators/{id}` | Remove agency link |
| GET | `/creators/search` | Search creators |

---

## Users

| Method | Endpoint | Description |
|---------|----------|-------------|
| GET | `/users` | List users in caller's agency |
| POST | `/users` | Invite user |

---

# Search

Current implementation supports

- Pagination

```
?page=0&limit=10
```

- Filter by niche

```
?niche=beauty
```

- Minimum followers

```
?minFollowers=10000
```

---

# Tenant Isolation

Tenant isolation is enforced through multiple layers.

1. Every request passes through `TenantInterceptor`.

2. The interceptor validates the `X-User-Id`.

3. The user's agency is stored inside `TenantContext`.

4. Repository methods always query using the current tenant.

5. Controllers never receive agency IDs from clients.

This prevents one agency from accidentally reading another agency's data.

---

# Shared Creator Model

Creators are shared globally.

Agency-specific information is stored separately through the
`AgencyCreatorLink` entity.

This allows

- multiple agencies to work with the same creator
- each agency to maintain private notes
- agencies to never view another agency's notes

---

# Validation

Incoming request payloads are validated using

```
@Valid
```

DTO validation ensures invalid requests are rejected before reaching the service layer.

---

# Testing

Run all automated tests

```bash
mvn clean test
```

The project contains integration tests verifying

- Agency A cannot read Agency B's notes
- Unlinked agencies receive 404
- Member users cannot invite users
- Admins can invite users
- Free plan creator limit
- Missing authentication header
- Invalid user ID
- Creator unlink behaviour

---

# Assumptions

The following implementation decisions were made:

- PATCH updates shared creator fields and the caller's own notes only.
- DELETE removes only the caller's agency link.
- Creators are deleted only when no agencies remain linked.
- Authentication is simplified using `X-User-Id` as specified in the challenge.
- Pagination and filtering are implemented.
- Sorting is left as a future enhancement.

---

# Future Improvements

With more development time, the following enhancements would be added:

- Sorting support (`sortBy`, `order`)
- JWT authentication
- Redis-backed distributed rate limiting
- PostgreSQL Row Level Security
- Swagger / OpenAPI documentation
- Testcontainers for integration tests
- GitHub Actions CI/CD pipeline
- Audit logging
- Soft deletes
- API versioning

---

# Deliverables

This repository contains

- Runnable Spring Boot API
- Automatic database seeding
- Postman Collection
- Automated JUnit Integration Tests
- ARCHITECTURE.md
- README.md

---

# License

This project was developed solely for the DigiTace Backend Challenge and is intended for evaluation purposes.

# Team

| Name          | GitHub                    |
|---------------|---------------------------|
| Rishit Sharma | https://github.com/rishitsharma07 |
| Karan Kumar   | https://github.com/KKumarPro |
| Kishu Raj     | https://github.com/kishuraj25       |