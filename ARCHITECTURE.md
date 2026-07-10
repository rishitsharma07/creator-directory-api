# Architecture

## Overview

The Creator Directory API is a multi-tenant REST application built using Spring Boot following a layered architecture.

Each request is authenticated using the `X-User-Id` header. The authenticated user's agency is determined before any business logic executes, ensuring every operation is automatically scoped to a single tenant.

The application is organized into the following layers:

```
                Client
                   │
                   ▼
          TenantInterceptor
                   │
                   ▼
            TenantContext
                   │
                   ▼
             REST Controller
                   │
                   ▼
             Service Layer
                   │
                   ▼
           Repository Layer
                   │
                   ▼
              PostgreSQL
```

---

# Domain Model

The system consists of four primary entities.

```
Agency
 │
 ├──────────────┐
 │              │
 ▼              ▼
User      AgencyCreatorLink
                │
                ▼
             Creator
```

## Agency

Represents a tenant of the platform.

Each agency has

- unique identifier
- name
- subscription plan (FREE / PRO)

---

## User

Each user belongs to exactly one agency.

Users are assigned one of three roles.

- OWNER
- ADMIN
- MEMBER

Role-based authorization determines which operations each user may perform.

---

## Creator

A creator is a globally shared resource.

Instead of belonging to a single agency, creators may be linked to multiple agencies.

---

## AgencyCreatorLink

Rather than storing an `agencyId` directly inside the Creator entity, a separate junction entity (`AgencyCreatorLink`) models the many-to-many relationship.

Each link stores

- Agency
- Creator
- Private Notes
- Added Timestamp

This design allows multiple agencies to work with the same creator while keeping their notes completely isolated.

---

# Tenant Isolation Strategy

Tenant isolation is the primary design goal of this application.

## Step 1 — Request Authentication

Every request must include

```
X-User-Id
```

The `TenantInterceptor` validates this header.

It then

- verifies the user exists
- determines the user's agency
- determines the user's role

---

## Step 2 — Tenant Context

After authentication, the agency ID (and user role where required) is stored inside a ThreadLocal `TenantContext`.

```
Request
    │
    ▼
TenantInterceptor
    │
    ▼
TenantContext
```

This information remains available throughout the request lifecycle.

At request completion, the context is cleared to prevent cross-request contamination.

---

## Step 3 — Repository Isolation

Repositories never receive agency IDs from client requests.

Instead, repository methods retrieve the current tenant directly from `TenantContext`.

Examples include

- finding visible creators
- searching creators
- retrieving creator details

Because repository methods automatically apply tenant filtering, developers cannot accidentally expose another tenant's data by forgetting to pass an agency ID from the controller.

This creates structural isolation instead of relying solely on developer discipline.

---

# Shared Creator Model

One creator may be associated with multiple agencies.

Example

```
                 Priya Sharma
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             ▼
 Nova Talent                 Bright Star Agency

 Notes:                      Notes:

 Great for skincare          Booked for Q1 shoot
 campaigns
```

Each agency only receives its own `AgencyCreatorLink`.

Therefore

- Nova cannot view Bright Star's notes.
- Bright Star cannot view Nova's notes.
- Agencies not linked to the creator receive HTTP 404.

---

# Role-Based Authorization

Authorization is enforced in the service layer.

| Role | Permissions |
|------|-------------|
| OWNER | Full access |
| ADMIN | Manage creators and invite users |
| MEMBER | Manage creators only |

Members attempting to invite new users receive

```
HTTP 403 Forbidden
```

---

# Plan Enforcement

Subscription limits are enforced entirely on the server.

Before linking or creating a creator, the service

1. determines the agency plan
2. counts linked creators
3. validates the limit

```
FREE

↓

Creator Count

↓

>= 5 ?

↓

Reject Request
```

If a FREE agency attempts to link a sixth creator, the API returns

```
HTTP 403 Forbidden
```

PRO agencies have no creator limit.

---

# Rate Limiting

To protect the API from abuse, requests are limited per agency.

The `RateLimitInterceptor` maintains an in-memory request counter using a `ConcurrentHashMap`.

Configuration

- 50 requests
- 1 minute window

When the limit is exceeded, the request is rejected with

```
HTTP 429 Too Many Requests
```

---

# Validation

Incoming payloads are validated using Bean Validation (`@Valid`).

This ensures invalid requests are rejected before reaching business logic.

Validation is applied to

- Creator creation
- Creator update
- User invitation

---

# Automated Testing

The project contains integration tests verifying the most critical security guarantees.

The test suite proves

- Agency A cannot read Agency B's notes.
- Agencies without a creator link receive HTTP 404.
- Members cannot invite users.
- Admins can invite users.
- Free plan agencies cannot exceed five creators.
- Missing authentication headers return HTTP 401.
- Invalid users return HTTP 401.
- Removing a creator only removes the caller's agency link.

---

# Assumptions

The following implementation decisions were made where the specification allowed flexibility.

- `PATCH /creators/{id}` updates shared creator fields and the caller's own notes.
- `DELETE /creators/{id}` removes only the caller's agency link.
- A creator is deleted only if no agencies remain linked.
- Authentication is intentionally simplified using the `X-User-Id` header.
- Pagination and filtering are implemented.
- Sorting is identified as a future enhancement.

---

# Future Improvements

Given additional development time, the following improvements would be implemented.

- PostgreSQL Row-Level Security (RLS)
- Redis-backed distributed rate limiting
- JWT authentication
- API versioning
- Swagger/OpenAPI documentation
- Testcontainers for integration testing
- Audit logging
- Soft deletes
- Distributed caching
- CI/CD pipeline using GitHub Actions

---

## Design Summary

The architecture prioritizes **structural tenant isolation** over relying on individual queries being written correctly.

By resolving the authenticated tenant once, storing it in `TenantContext`, and enforcing repository-level isolation, the risk of accidental cross-tenant data leakage is significantly reduced while keeping the codebase modular and maintainable.