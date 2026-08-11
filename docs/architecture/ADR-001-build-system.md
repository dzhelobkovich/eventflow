# ADR-001: Build System and Java Version

## Status

Accepted

## Context

EventFlow requires a consistent and reproducible development environment
for local development and continuous integration.

The project should use a build configuration that can be executed without
requiring developers to install a specific Gradle version manually.

## Decision

EventFlow will use:

- Java 21 as the primary Java version.
- Gradle as the build automation tool.
- Gradle Kotlin DSL for build configuration.
- Gradle Wrapper to provide a consistent Gradle version across environments.

Build scripts will use the `.gradle.kts` format.

The Gradle Wrapper will be committed to the repository and used both locally
and in CI.

## Consequences

This decision provides:

- consistent Gradle versions across development and CI environments;
- reproducible project builds;
- no requirement for a system-wide Gradle installation;
- type-safe Gradle build configuration through Kotlin DSL.

Future services and modules will follow the same build conventions unless
an architectural decision explicitly changes this approach.