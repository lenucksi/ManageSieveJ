---
id: decision-3
title: JPMS removal
date: '2026-05-20 14:55'
status: accepted
---

## Context

Project initially included module-info.java for JPMS (Java Platform Module System). Test compilation kept failing due to module path issues with test dependencies.

## Decision

Removed module-info.java entirely. The library is used as a classpath dependency in most projects (including SieveEditor), so JPMS provides limited benefit.

## Consequences

Simpler build, no module resolution errors. MIT license header removed from module-info. Library consumers don't need to deal with module resolution.
