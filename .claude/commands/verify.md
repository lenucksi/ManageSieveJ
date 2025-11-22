---
description: Run full Maven verification including tests
---

# Verify

Run the full Maven verification lifecycle by executing `mvn clean verify`. This will:

- Clean previous builds
- Compile source code
- Run all tests
- Run integration tests (if any)
- Package the JAR
- Verify the package

Report any failures or issues encountered during verification.
