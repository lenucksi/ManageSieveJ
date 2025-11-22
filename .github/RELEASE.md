# Release Process Documentation

## Overview

This repository uses [release-please](https://github.com/googleapis/release-please) v4 for automated semantic versioning and releases.

## How It Works

### 1. Conventional Commits

All commits should follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

- `feat:` - New features (triggers minor version bump)
- `fix:` - Bug fixes (triggers patch version bump)
- `perf:` - Performance improvements (patch bump)
- `security:` - Security fixes (patch bump)
- `deps:` - Dependency updates (patch bump)
- `docs:` - Documentation changes (no version bump)
- `test:` - Test changes (no version bump)
- `refactor:` - Code refactoring (no version bump)
- `build:` - Build system changes (no version bump)
- `ci:` - CI configuration changes (no version bump)
- `chore:` - Other changes (no version bump)

**Breaking changes:** Add `!` after type or include `BREAKING CHANGE:` in commit body for major version bump.

### 2. Release Please PR

When commits are pushed to `main`, release-please will:

1. Analyze commits since last release
2. Determine next version based on conventional commits
3. Create/update a "release PR" with:
   - Updated version in `pom.xml`
   - Generated `CHANGELOG.md`
   - Updated `.release-please-manifest.json`

### 3. Merging the Release PR

When you merge the release PR:

1. Release-please creates a GitHub release with:
   - Git tag (e.g., `v0.3.3`)
   - Release notes from changelog
2. The `build-and-publish` job runs automatically:
   - Builds the JAR files
   - Publishes to GitHub Packages
   - Generates SLSA attestation
   - Uploads artifacts to the release
   - Adds installation instructions

## Configuration Files

### `.release-please-manifest.json`

Tracks the current version:

```json
{
  ".": "0.3.2"
}
```

### `.github/release-please-config.json`

Configures release behavior:

```json
{
  "release-type": "maven",
  "packages": {
    ".": {
      "package-name": "managesievej",
      "changelog-path": "CHANGELOG.md",
      "extra-files": [
        {
          "type": "xml",
          "path": "pom.xml",
          "xmlpath": "/project/version"
        }
      ]
    }
  },
  "changelog-sections": [...]
}
```

## Manual Release

To trigger a manual release:

1. Go to Actions → Release workflow
2. Click "Run workflow"
3. Select branch `main`
4. Click "Run workflow"

This will check for unreleased commits and create a release PR if needed.

## Version Strategy

- **0.x.y** (pre-1.0): Breaking changes bump minor version
- **1.x.y** (post-1.0): Breaking changes bump major version
- Feature commits bump minor version
- Fix/patch commits bump patch version

## Consuming Releases in SieveEditor

### Option 1: JitPack (Recommended - No Authentication) 🌟

After a release is published, update SieveEditor's `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.lenucksi</groupId>
        <artifactId>ManageSieveJ</artifactId>
        <version>v0.3.3</version> <!-- Use release tag -->
    </dependency>
</dependencies>
```

**No authentication required!** JitPack builds from GitHub releases automatically.

### Option 2: GitHub Packages (Requires Authentication)

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/lenucksi/ManageSieveJ</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.fluffypeople</groupId>
        <artifactId>managesievej</artifactId>
        <version>0.3.3</version>
    </dependency>
</dependencies>
```

Requires GitHub Packages authentication in `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_PERSONAL_ACCESS_TOKEN</password>
    </server>
</servers>
```

## Troubleshooting

### Release PR not created

- Ensure commits use conventional commit format
- Check that commits exist since last release
- Verify workflow has necessary permissions

### Publishing fails

- Ensure GitHub token has `packages: write` permission
- Check Maven configuration in `pom.xml`
- Verify `distributionManagement` points to correct repository

### Version not updated in pom.xml

- Check `extra-files` configuration in `release-please-config.json`
- Ensure `xmlpath` is correct: `/project/version`

## References

- [Release Please Documentation](https://github.com/googleapis/release-please)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [GitHub Packages Maven](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
