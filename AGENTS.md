# AGENTS.md

This file provides guidance for AI agents working on the **symfonion** codebase — a JSON/YAML-based music macro language processor written in Java.

## Project Overview

- **Language**: Java 21
- **Build tool**: Maven 3.x
- **Artifact**: `com.github.dakusui:symfonion` (JAR + CLI distribution archive)
- **Distribution**: `symfonion-VERSION-bin.zip` unpacking to `bin/symfonion` launcher + `lib/symfonion-VERSION.jar`

## Build & Test

```bash
# Compile and run all tests
mvn -B package

# Run tests only (skip recompile)
mvn -B test

# Build without tests
mvn -B package -DskipTests
```

CI runs `mvn -B package` on every push/PR (see `.github/workflows/maven.yml`).

## Source Layout

```
src/
  main/java/com/github/dakusui/
    symfonion/
      cli/          # Command-line parsing (Cli, Subcommand, CliUtils)
      core/         # Compiler + runtime (MidiCompiler, Symfonion)
      song/         # Domain model (Song, Part, Pattern, Bar, Note, …)
      utils/midi/   # MIDI device scanning and utilities
      exception/    # Error handling
    valid8j_cliche/ # Assertion helpers (vendored)
  test/java/com/github/dakusui/
    symfonion/tests/  # JUnit 5 integration tests (CLI, error, malformed input)
    testutils/        # Shared test helpers (JSON builders, MIDI stubs)
```

## Key Conventions

- **No Windows support** — only Linux/macOS launchers; do not introduce `.bat` scripts.
- **No dollar-sign prefixes** on JSON/YAML keywords (removed in v3.x).
- **Java 21** — use modern Java features freely; the CI baseline is JDK 21.
- Tests use **JUnit 5 (Jupiter)**; keep new tests in `src/test/java/…/symfonion/tests/`.
- For bash scripts, follow the project's shell conventions (see `.claude/skills/symfonion-bash`).

## Release

Releases are published to Maven Central via Sonatype. Follow the release skill (`.claude/skills/symfonion-release`) rather than running `mvn release:*` manually.

## Useful Entry Points

| Goal | Starting point |
|---|---|
| CLI parsing | `Cli.java`, `Subcommand.java` |
| Song compilation to MIDI | `MidiCompiler.java`, `MidiCompilerContext.java` |
| Song/score domain model | `Song.java`, `Part.java`, `Pattern.java` |
| Error messages | `SymfonionExceptionThrower.java` |
| Smoke tests | `CliSmokeTest.java` |
