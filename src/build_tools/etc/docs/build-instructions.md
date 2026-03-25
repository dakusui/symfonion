# Build Instructions

## Building locally

```
mvn package
```

Skipping tests:

```
mvn package -DskipTests
```

### Artifacts produced under `target/`

| File | Description |
|---|---|
| `symfonion-<version>.jar` | Thin jar (Maven default) |
| `symfonion-<version>-shaded.jar` | Fat/executable jar (all dependencies bundled) |
| `symfonion-<version>-sources.jar` | Sources jar |
| `symfonion-<version>-javadoc.jar` | Javadoc jar |
| `symfonion-<version>-bin.zip` | CLI distribution archive |

### CLI distribution archive layout

```
symfonion-<version>/
  bin/symfonion              ← Unix launcher (LF, mode 0755)
  bin/symfonion.bat          ← Windows launcher (CRLF, mode 0755)
  lib/symfonion-<version>.jar
```

The launcher scripts are filtered at build time: `${project.version}` in
the source files (`src/main/dist/bin/`) is replaced with the actual version,
so the packaged scripts reference the exact versioned jar.

---

## Releasing

The release process uses the Maven Release Plugin and consists of two steps.

### Step 1 — prepare

```
mvn release:prepare
```

This will:
1. Verify there are no uncommitted changes.
2. Prompt for the release version and the next development version.
3. Update `pom.xml` with the release version, commit it, and tag the commit as `v<version>`.
4. Update `pom.xml` again with the next `-SNAPSHOT` version and commit it.

### Step 2 — perform

```
mvn release:perform
```

This will:
1. Check out the tag created by `release:prepare` into `target/checkout/`.
2. Run a clean `mvn deploy` against that checkout.

During `deploy`, the following artifacts are built and uploaded:

| Artifact | Destination |
|---|---|
| `symfonion-<version>.jar` | Maven Central (Sonatype) |
| `symfonion-<version>-sources.jar` | Maven Central |
| `symfonion-<version>-javadoc.jar` | Maven Central |
| `symfonion-<version>-shaded.jar` | Maven Central |
| `symfonion-<version>-bin.zip` | Maven Central + GitHub Releases |

The `-bin.zip` ends up in two places because:
- `maven-assembly-plugin:single` runs at `package` with `attach=true` (the default),
  registering the zip as a secondary artifact; Maven's deploy mechanism then uploads
  all attached artifacts to Maven Central automatically.
- The `release` profile activates `github-release-plugin` at the `deploy` phase,
  which uploads the zip directly to the GitHub Release for the tag.

### GPG signing

Artifacts are signed automatically when the `performRelease` property is set
(the `release-sign-artifacts` profile activates on that property). The GPG
passphrase can be passed as:

```
mvn release:perform -Dgpg.passphrase=<passphrase>
```
