---
id: decision-1
title: TestNG to JUnit 5 migration
date: '2026-05-20 14:55'
status: accepted
---

## Context

Original upstream code used TestNG for testing. Modern Java ecosystem standard is JUnit 5 with Mockito and AssertJ. Migration was needed for better tooling support (IDE, build plugins, CI).

## Decision

Full migration from TestNG to JUnit Jupiter 6.0.3 + Mockito 5.23.0 + AssertJ 3.27.7. All existing tests rewritten in JUnit 5 style.

## Consequences

Better IDE integration, easier contribution (JUnit 5 is standard), improved assertion readability with AssertJ. Jazzer fuzz testing also uses JUnit 5 platform.
