# JitPack Integration

This project is available on JitPack for easy consumption without authentication requirements.

## For Consumers (SieveEditor)

Add to your `pom.xml`:

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
        <version>v0.3.3</version> <!-- Use latest release tag -->
    </dependency>
</dependencies>
```

**No authentication required!** JitPack builds automatically from GitHub releases.

## How It Works

1. Create a GitHub release with a version tag (e.g., `v0.3.3`)
2. JitPack builds the project on-demand when first requested
3. Cached for future requests
4. Anyone can download without credentials

## Version Options

You can use:
- **Release tags**: `v0.3.3` (recommended)
- **Branch names**: `main-SNAPSHOT` (for latest from main)
- **Commit hashes**: `abc1234` (for specific commit)

## Build Status

Check build status at: https://jitpack.io/#lenucksi/ManageSieveJ

## Alternative: GitHub Packages

This project is also published to GitHub Packages, but requires authentication:

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

Requires `~/.m2/settings.xml` configuration with GitHub token.

## Recommendation

**Use JitPack** for public FOSS consumption - no authentication needed.
