---
id: decision-5
title: JitPack over GitHub Packages as primary distribution
date: '2026-05-20 14:55'
status: accepted
---

## Context

Two distribution options: JitPack builds on-demand from GitHub tags, GitHub Packages requires authentication. Target users (SieveEditor developers) need simple dependency declaration.

## Decision

JitPack is the primary recommended distribution. GitHub Packages remains available but requires GitHub token authentication. Both are published during CI release.

## Consequences

Users add one JitPack repository to pom.xml and can depend on any tag. Simpler onboarding. No authentication required for read access.
