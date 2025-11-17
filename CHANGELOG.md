# Changelog

## [0.3.5](https://github.com/lenucksi/ManageSieveJ/compare/managesievej-v0.3.4...managesievej-v0.3.5) (2025-11-17)


### Bug Fixes

* **ci:** add attestations permission to release workflow ([3a26141](https://github.com/lenucksi/ManageSieveJ/commit/3a261418dd4277b8c713b30649b6433651cdde19))

## [0.3.4](https://github.com/lenucksi/ManageSieveJ/compare/managesievej-v0.3.3...managesievej-v0.3.4) (2025-11-17)


### Features

* add OpenSSF Scorecard security assessment workflow ([03d2e65](https://github.com/lenucksi/ManageSieveJ/commit/03d2e65f545841f387da13221257a8544b39033a))
* enable SLSA build provenance attestation in CI workflow ([2e1e1de](https://github.com/lenucksi/ManageSieveJ/commit/2e1e1de4b16bcee7abad1e3eff33b25c4e61c20c))


### Bug Fixes

* **security:** apply critical security fixes for log sanitization and resource leaks ([677e07b](https://github.com/lenucksi/ManageSieveJ/commit/677e07bf5b0845775df2b37a0ac86fb5228bc61f))
* **security:** eliminate taint flow by never logging line content ([12ee5d0](https://github.com/lenucksi/ManageSieveJ/commit/12ee5d033755cd9d774db4071556e85fb70091bb))
* **security:** strengthen log redaction to address CodeQL alert ([9002cc7](https://github.com/lenucksi/ManageSieveJ/commit/9002cc7f830fff68f2b8ddb4a2c718db8f3f8b0f))


### Code Refactoring

* unify CodeQL workflows into single enhanced workflow ([f5ac1be](https://github.com/lenucksi/ManageSieveJ/commit/f5ac1be126a4e479e4e3e9d756d3900af4b0abf2))

## [0.3.3](https://github.com/lenucksi/ManageSieveJ/compare/managesievej-v0.3.2...managesievej-v0.3.3) (2025-11-16)


### Features

* add automated JitPack build trigger to release workflow ([a1140d3](https://github.com/lenucksi/ManageSieveJ/commit/a1140d3febdb572fd48a69f5db750fe413d8be01))
* add Claude Code harness for ManageSieveJ development ([2bf9f12](https://github.com/lenucksi/ManageSieveJ/commit/2bf9f120b941ae6f1b7ee9161e874cbeefc2d130))
* add JitPack repository integration for authentication-free distribution ([d87bf0f](https://github.com/lenucksi/ManageSieveJ/commit/d87bf0f4c6ca1c01ab9acce64a55d452fe4a215c))
* modernize repository with CI/CD, testing, and GitHub Packages support ([5d0cc44](https://github.com/lenucksi/ManageSieveJ/commit/5d0cc4438823f4dc6057ea85d95f392f3e77826d))
* upgrade to Java 21 LTS with backward compatibility testing ([b4b155e](https://github.com/lenucksi/ManageSieveJ/commit/b4b155e062b837829bc139f6db219b6d82cb5f49))


### Bug Fixes

* add protocol validation and case-insensitive notify methods ([2c9c2a4](https://github.com/lenucksi/ManageSieveJ/commit/2c9c2a40a39fa40f6bb9ad654c963a1b842a1c3a))
* add release-please configuration for Maven releases ([8ec55d4](https://github.com/lenucksi/ManageSieveJ/commit/8ec55d48fe1c1afaa6b584a00563aeb4ff600a4e))
* configure test compilation to work with Java modules ([83a650f](https://github.com/lenucksi/ManageSieveJ/commit/83a650f74aa225f8f6fa13ab4819b5871ff5be3d))
* convert SessionStart hook to LF line endings and prevent CRLF in shell scripts ([937f9d0](https://github.com/lenucksi/ManageSieveJ/commit/937f9d07a850354e4f4781495aca3016ff9559d5))
* correct actions/cache SHA to v4.3.0 ([c42a41a](https://github.com/lenucksi/ManageSieveJ/commit/c42a41a1a922c0825b924dc0e35365df433e1229))
* correct AssertJ import statements in all tests ([7afd698](https://github.com/lenucksi/ManageSieveJ/commit/7afd698a8e69307c47c0eac5a0f5ae5aa05826d7))
* correct branch names from 'main' to 'master' in all workflows ([ff8ca48](https://github.com/lenucksi/ManageSieveJ/commit/ff8ca484445f444a11bc78e7f7e0bda76aa2546c))
* correct publish-unit-test-result-action reference for GitHub-hosted runners ([6d97bbc](https://github.com/lenucksi/ManageSieveJ/commit/6d97bbc155871c61116a6f62785633ebcc3dd58e))
* correct release-please config file path to .github directory ([a7dd2bd](https://github.com/lenucksi/ManageSieveJ/commit/a7dd2bdf0002dcacfb68fcf39ade9772df2ba78f))
* correct release-please v4 configuration for GitHub Packages publishing ([35760f8](https://github.com/lenucksi/ManageSieveJ/commit/35760f8b15826d404445430615d8dc55b6d42db2))
* Correct release-please XML field from 'xmlpath' to 'xpath' ([d6a7ecc](https://github.com/lenucksi/ManageSieveJ/commit/d6a7ecc1a942ef0e9594fd968c7d6f628a3b1ca4))
* resolve CI workflow failures (Java compatibility, action path, OWASP caching) ([f799a8a](https://github.com/lenucksi/ManageSieveJ/commit/f799a8a7f547e7468b89a79b6cbbf580c43275d7))


### Dependencies

* **deps-dev:** bump the test-dependencies group with 2 updates ([f7dc0dc](https://github.com/lenucksi/ManageSieveJ/commit/f7dc0dc1a8e3af95627ad8ef95df2e637b541ff6))
* **deps:** bump actions/attest-build-provenance from 1.4.3 to 3.0.0 ([6690c1e](https://github.com/lenucksi/ManageSieveJ/commit/6690c1ef974a68e2d8b477f300e245a53f702eb5))
* **deps:** bump actions/checkout from 4.2.2 to 5.0.0 ([6aee5f4](https://github.com/lenucksi/ManageSieveJ/commit/6aee5f40e566c887cf65fcb4575cb98f55316c11))
* **deps:** bump codecov/codecov-action from 5.0.7 to 5.5.1 ([9026465](https://github.com/lenucksi/ManageSieveJ/commit/90264650ca9efe9ccea62c20079be578724edb0b))
* **deps:** bump s4u/maven-settings-action from 3.0.0 to 4.0.0 ([175f63f](https://github.com/lenucksi/ManageSieveJ/commit/175f63f0bfc753e93fd8d256cd374044476bbb9b))
* **deps:** bump step-security/harden-runner from 2.10.1 to 2.13.2 ([d59c9f6](https://github.com/lenucksi/ManageSieveJ/commit/d59c9f6affef8612369ece0b8eab0521c226b74a))
* **deps:** bump the maven-plugins group with 2 updates ([1b74efa](https://github.com/lenucksi/ManageSieveJ/commit/1b74efa0dbf4b4f1136b959acef46c356dbf9480))


### Code Refactoring

* remove JPMS module descriptor for classpath compatibility ([521bf74](https://github.com/lenucksi/ManageSieveJ/commit/521bf7407f9df7a9287eb0a6742a9580279ba1b7))


### Documentation

* recommend JitPack as primary distribution method ([c591a9a](https://github.com/lenucksi/ManageSieveJ/commit/c591a9a5f0bd3f385dee938df985c4131634c535))


### Build System

* add Maven wrapper for JitPack compatibility and fix build configuration ([47123e3](https://github.com/lenucksi/ManageSieveJ/commit/47123e362e9c5a319a56fecf9c3d5ec36e447ad0))
* upgrade CodeQL Action from v3 to v4 ([b221169](https://github.com/lenucksi/ManageSieveJ/commit/b2211691440cf754019c2f0074349feb663c5936))
