# JitPack Integration (Deprecated)

> **⚠️ Deprecated:** This project now publishes to **Maven Central** under `io.github.lenucksi:managesievej`.
> No extra repository configuration is needed. Please migrate your dependency declarations.

## Legacy: JitPack

Old releases are still available via JitPack, but no new releases will be published here.

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
        <version>v0.3.3</version>
    </dependency>
</dependencies>
```

## Legacy: GitHub Packages

Old releases are also available via GitHub Packages (requires authentication).

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/lenucksi/ManageSieveJ</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.lenucksi</groupId>
        <artifactId>managesievej</artifactId>
        <version>0.3.3</version>
    </dependency>
</dependencies>
```

## Recommendation

**Use Maven Central** — no extra repository, no authentication, automatic dependency resolution.
