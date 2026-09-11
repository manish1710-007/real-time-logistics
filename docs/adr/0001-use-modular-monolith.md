# 1. Modular Monolith Architecture

**Date:** 2026-09-11
**Status:** Accepted

## Context

I am building a real-time logistics platform as a solo developer. I need to deliver features quickly while maintaining a clear architectural path for scaling the system in the future.

A microservices architecture would introduce additional operational complexity, including service deployment, networking, distributed transactions, observability, service discovery, and message infrastructure. This complexity is not justified at the current stage of the project.

However, I want the system to be structured so that individual modules can be extracted into independent services if the system eventually requires independent scaling or deployment.

## Decision

I will use a **Modular Monolith Architecture**.

The application will initially run as a single deployable application and a single JVM process. The codebase will be divided into domain-oriented modules such as:

* `driver`
* `order`
* `dispatch`
* `payment`
* `notification`

Where practical, these modules will also be represented as separate Maven modules to provide additional compile-time dependency isolation.

Each module will have a clearly defined public API and internal implementation.

For example:

```text
driver/
├── DriverFacade.java
├── DriverAssigned.java
└── internal/
    ├── DriverEntity.java
    ├── DriverRepository.java
    └── DriverService.java
```

Other modules may depend on `DriverFacade` or consume published domain events, but must not access the driver's repositories, entities, mappers, or other internal implementation details directly.

### Database ownership

The application will initially use a single database instance.

However, database ownership will remain modular. Each module owns its own tables/schema, and other modules must not directly access another module's persistence layer.

For example:

```text
Order ──X──> DriverRepository
Order ──X──> driver tables

Order ──> Driver API
   or
Order ──> Driver-related domain event
```

This provides the simplicity of a shared database deployment while preserving a boundary that can support future extraction.

### Module communication

Modules may communicate through:

1. Explicit public APIs/facades when an immediate response is required.
2. Domain/application events when a module needs to notify other modules that something has happened.

Circular module dependencies are prohibited.

Architecture tests using **ArchUnit and/or Spring Modulith** will enforce these rules as part of the build.

## Consequences

### Positive

* Fast development and iteration.
* Simple deployment and infrastructure.
* Easy local development and debugging.
* No distributed-system complexity during the early stages.
* Transactions can remain local when appropriate.
* Modules can be tested independently.
* Explicit module boundaries reduce accidental coupling.
* Module ownership of persistence reduces future extraction difficulty.
* Individual modules can potentially be extracted into microservices when independent scaling or deployment becomes necessary.

### Negative

* All modules initially share the same JVM/process.
* A failure in the application can affect all modules.
* Modules share the same database instance.
* Database-level separation is weaker than fully independent microservices.
* Strong discipline is required to prevent modules from bypassing public APIs.
* Some cross-module operations may require events or carefully designed APIs.
* Extracting a module into a microservice later will still require additional work around networking, distributed transactions, messaging, deployment, and observability.

## Architectural Rules

The following rules are mandatory:

1. Modules must not access another module's internal implementation.
2. Modules must not access another module's repositories or database tables directly.
3. Entities belonging to one module must not become shared domain models across modules.
4. Module dependencies must be explicitly defined where practical.
5. Circular module dependencies are prohibited.
6. Cross-module communication must use a public API or domain/application event.
7. Architecture tests must run as part of CI/build validation.
8. Database ownership must remain aligned with module ownership.
9. Shared infrastructure code must not become a backdoor for bypassing module boundaries.

## Future Extraction Strategy

If a module eventually requires independent scaling, deployment, or operational isolation, it may be extracted into a separate service.

The intended evolution is:

```text
Modular Monolith
       │
       │ scaling/organizational requirement
       ▼
Extract selected module
       │
       ▼
Independent Microservice
```

Microservices will therefore be introduced based on an actual scaling or operational requirement rather than as a default architectural choice.

## Alternatives Considered

### Microservices

Rejected for the initial system because the operational and distributed-system complexity is not justified for a solo developer at the current stage.

### Traditional Monolith

Rejected because unrestricted access between domains would make boundaries difficult to enforce and would make future extraction more expensive.

### Modular Monolith without Maven module separation

Possible and potentially sufficient, using package-level boundaries enforced by Spring Modulith/ArchUnit.

However, Maven module separation may provide an additional compile-time boundary and will be evaluated based on project complexity. Spring Modulith can independently model and verify logical application modules within a Spring Boot application.

## Decision Outcome

Use a **modular monolith with explicit domain boundaries, module-owned persistence, controlled synchronous APIs, domain/application events, and automated architecture verification**.

Microservices will only be introduced when a specific module demonstrates a concrete need for independent scaling, deployment, or operational isolation.
