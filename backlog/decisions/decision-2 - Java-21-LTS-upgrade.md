---
id: decision-2
title: Java 21 LTS upgrade
date: '2026-05-20 14:55'
status: accepted
---

## Context

Project was originally on Java 11. Java 17 and 21 LTS releases brought significant improvements (records, sealed classes, pattern matching, virtual threads) and ecosystem support.

## Decision

Upgraded to Java 21 LTS as target, retaining Java 17 source compatibility. Source and target set to 21 in Maven compiler plugin.

## Consequences

Access to modern language features. Users need JDK 17+ to compile. CI matrix tests across all 3 OSes with JDK 21 Temurin.
