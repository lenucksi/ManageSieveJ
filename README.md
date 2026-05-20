# Introduction

[Sieve][1] is a simple mail filtering language. Users scripts are stored and run on
the mail server, and are managed through the [Manage Sieve][2] protocol. This library
provides a Java API to the Manage Sieve protocol.

## Dependencies

No runtime dependencies are required.

The Unit tests use testng.

## Example

There is an example use of the library in the
com.fluffypeople.managesieve.examples package.

## Maven

Available on Maven Central — no extra repository needed:

    <dependency>
      <groupId>io.github.lenucksi</groupId>
      <artifactId>managesievej</artifactId>
      <version>0.3.13-SNAPSHOT</version>
    </dependency>

> **Note:** The old groupId `com.fluffypeople` is no longer published by this fork.
> Previous versions are still available on Maven Central under `com.fluffypeople`.

## Licence

This library is covered by the MIT licence.

## References

[1]: <http://tools.ietf.org/html/rfc3028> "Sieve RFC"
[2]: <http://tools.ietf.org/html/rfc5804> "Manage Sieve RFC"
