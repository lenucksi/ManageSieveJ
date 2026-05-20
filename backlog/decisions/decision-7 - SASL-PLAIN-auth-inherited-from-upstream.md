---
id: decision-7
title: SASL PLAIN auth inherited from upstream
date: '2026-05-20 14:55'
status: accepted
---

## Context

ManageSieve protocol uses SASL for authentication. The upstream implementation (Osric Wilkinson) used SASL PLAIN as primary mechanism. This project inherits that architecture.

## Decision

Retain SASL PLAIN as inherited auth mechanism via javax.security.sasl. Security auditor agent monitors for credential handling improvements. No migration to alternative SASL mechanisms.

## Consequences

Working auth with PLAIN. CRAM-MD5 and other mechanisms theoretically supported via SASL framework. Credential exposure in logs is a known issue (TASK-4).
