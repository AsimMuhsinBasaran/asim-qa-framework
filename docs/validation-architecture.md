# Validation Architecture

## Purpose

This repository is evolving into a unified QA ecosystem for portfolio-grade automation across API, database, messaging, and UI layers. The goal is not to build separate mini projects, but to keep one consistent validation model with layer-specific adapters.

## Current Role of the Java API Framework

The current `java-automation` module is the backbone of the ecosystem.

It already provides:
- scenario-scoped state management
- centralized placeholder resolution
- auth handling
- request/response correlation
- retry classification
- parallel-safe logging
- masked Allure reporting

This module should remain the execution and validation core for backend-oriented checks.

## Intended Role of Cypress UI

The future `cypress-ui` workspace will own browser-level validation.

It should focus on:
- user flows
- visual and DOM assertions
- browser-specific waits
- screenshots and videos
- UI-to-backend verification when needed

It should not duplicate backend request logic, masking rules, or shared context semantics.

## Shared Contracts

Shared contracts live under `test-data/contracts/`.

They define:
- response shapes
- schema expectations
- cross-layer data contracts
- naming conventions
- runtime data expectations

The contract is the shared language between Java and Cypress. The code that reads it can differ, but the meaning should stay the same.

## Runtime Bridge

Runtime bridge data lives under `test-data/runtime/` and is generated during test execution.

This is the short-lived handoff layer between Java and Cypress when API-generated data must be reused by UI tests.

Examples:
- access token or session reference, only when necessary
- created entity identifiers
- correlation IDs
- scenario name
- environment name

Sensitive secrets must not be written to the bridge by default.

## Reporting Strategy

The first phase keeps reporting separate by layer:
- Java writes to `java-automation/target/allure-results`
- Cypress writes to its own Allure results directory later

Separate results keep the first integration simple and easier to debug.
Report merging can come later if the project needs a unified final HTML report.

## Do Not Do

Do not:
- create a universal validation DSL too early
- share Java classes directly with Cypress
- centralize every validation rule into one mega utility
- force DB, Kafka, and UI into the same execution model
- merge reports before the data flow is stable
- store secrets in runtime bridge files
- make the first phase dependent on complex orchestration

## Phased Roadmap

### Phase 1
- document the monorepo intent
- define shared folders
- define runtime bridge and contract ownership
- keep Java as the current execution core

### Phase 2
- add a minimal runtime bridge writer in Java
- let Cypress read generated runtime context
- keep layer boundaries strict

### Phase 3
- add DB, Kafka, and Cassandra adapters
- standardize extraction and assertion envelopes

### Phase 4
- unify reporting aggregation if it is still useful
- add CI matrix and artifact handling

### Phase 5
- refine shared validation vocabulary and docs
- keep layer-specific implementation isolated
