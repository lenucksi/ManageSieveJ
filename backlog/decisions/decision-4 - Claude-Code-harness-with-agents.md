---
id: decision-4
title: Claude Code harness with agents
date: '2026-05-20 14:55'
status: accepted
---

## Context

Need for AI-assisted development workflow with specialized agents for security auditing, test generation, and TDD enforcement.

## Decision

Created .claude/ directory with 3 agents (security-auditor, test-generator, test-guardian), build/test coverage/verify commands, SessionStart hook, and environment verification.

## Consequences

Standardized AI development workflow. Security reviewed on every change. Tests generated following project patterns. TDD enforced in automated sessions.
