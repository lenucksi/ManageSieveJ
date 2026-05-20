---
id: decision-6
title: SLSA Level 3 build attestations
date: '2026-05-20 14:55'
status: accepted
---

## Context

SLSA (Supply-chain Levels for Software Artifacts) provides security guarantees about build integrity. Needed for supply chain security and OpenSSF Scorecard compliance.

## Decision

Implemented SLSA Level 3 build attestations via actions/attest-build-provenance. Attests JAR artifacts during CI release workflow. Also enabled OpenSSF Scorecard weekly scanning.

## Consequences

Build artifacts have verifiable provenance. Scorecard shows SLSA compliance. Better supply chain security posture.
