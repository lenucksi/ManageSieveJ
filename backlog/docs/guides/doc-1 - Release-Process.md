---
id: doc-1
title: Release Process
type: guide
created_date: '2026-05-20 14:56'
updated_date: '2026-05-20 14:56'
---

# Release Process

## Automated via release-please

Triggers on push to master. Creates release PR, then publishes GitHub release with tag.

## Build & Publish

1. release-please creates GitHub release
2. build-and-publish workflow: `mvn deploy` to GitHub Packages
3. SLSA Level 3 attestation
4. JAR + checksums attached to release
5. JitPack build triggered (builds on-demand from tags)

## Manual

- Bump version in pom.xml + .release-please-manifest.json
- Commit with conventional commit message
- Push to master

**Source:** .github/RELEASE.md, .github/workflows/release.yml
