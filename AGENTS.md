# AGENTS.md

This file provides guidance for AI agents working on the **symfonion** codebase — a JSON/YAML-based music macro language processor written in Java.

## Project Overview

- **Language**: Java 21
- **Build tool**: Maven 3.x
- **Artifact**: `com.github.dakusui:symfonion` (JAR + CLI distribution archive)
- **Distribution**: `symfonion-VERSION-bin.zip` unpacking to `bin/symfonion` launcher + `lib/symfonion-VERSION.jar`

## Build & Test

### Prerequisites (Ubuntu/Debian)

```bash
sudo apt install maven ruby-rubygems openjdk-21-jdk
# Then set OpenJDK 21 as default:
sudo update-alternatives --config java   # choose openjdk-21 from the list
```

### Commands

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

- **YAML is the primary user-facing input format.** The `bin/symfonion` launcher transparently converts `.yaml`/`.yml` files through `yq` → `jq++` before passing them to the JAR. Plain `.json` files are passed directly and remain fully supported.
- **No Windows support** — only Linux/macOS launchers; do not introduce `.bat` scripts.
- **No dollar-sign prefixes** on JSON/YAML keywords (removed in v3.x).
- **Java 21** — use modern Java features freely; the CI baseline is JDK 21.
- Tests use **JUnit 5 (Jupiter)**; keep new tests in `src/test/java/…/symfonion/tests/`.
- For bash scripts, follow the project's shell conventions (see `.agents/skills/symfonion-bash`).

## Directory Layout

See [`src/site/asciidoc/INSTALLATION.adoc`](src/site/asciidoc/INSTALLATION.adoc) for the full layout, first-run bootstrap details, and `JF_PATH` precedence.

## Launcher & Preprocessing Pipeline

`bin/symfonion` (and its dev copy `src/main/resources/utils/symfonion`) handles three concerns:

1. **Dependency bootstrap** — on first use, downloads `yq` and installs `jqplusplus` (via `go install`) into `$SYMFONION_HOME/lib/`. Presence of `lib/jq++` (the symlink created by jqplusplus) signals that deps are ready.
2. **YAML preprocessing** — `.yaml`/`.yml` args are piped through `yq -o=json | jq++` and written to a temp file; the temp path replaces the original arg.
3. **JF_PATH setup** — builds the search path for `jq++` library files (prelude), respecting `SYMFONION_PATH` → `.symfonion/prelude` → built-in prelude.

Version pins (`yq_version`, `jqplusplus_version`) live at the top of both launcher files and should be bumped together at release time.

## Directory Layout

See [`src/site/asciidoc/INSTALLATION.adoc`](src/site/asciidoc/INSTALLATION.adoc) for the full layout, first-run bootstrap details, and `JF_PATH` precedence.

## Launcher & Preprocessing Pipeline

`bin/symfonion` (and its dev copy `src/main/resources/utils/symfonion`) handles three concerns:

1. **Dependency bootstrap** — on first use, downloads `yq` and installs `jqplusplus` (via `go install`) into `$SYMFONION_HOME/lib/`. Presence of `lib/jq++` (the symlink created by jqplusplus) signals that deps are ready.
2. **YAML preprocessing** — `.yaml`/`.yml` args are piped through `yq -o=json | jq++` and written to a temp file; the temp path replaces the original arg.
3. **JF_PATH setup** — builds the search path for `jq++` library files (prelude), respecting `SYMFONION_PATH` → `.symfonion/prelude` → built-in prelude.

Version pins (`yq_version`, `jqplusplus_version`) live at the top of both launcher files and should be bumped together at release time.

## Release

Releases are published to Maven Central via Sonatype. Follow the release skill (`.agents/skills/symfonion-release`) rather than running `mvn release:*` manually.

## Agent Workflow Files

- Shared reusable workflows live under `.agents/skills/`.
- `.claude/skills` is a symlink to `.agents/skills` for Claude.
- `.codex/skills` is a symlink to `.agents/skills` for Codex.
- Keep workflow guidance in `.agents/skills`; do not duplicate it under tool-specific directories.

## Useful Entry Points

| Goal | Starting point |
|---|---|
| CLI parsing | `Cli.java`, `Subcommand.java` |
| Song compilation to MIDI | `MidiCompiler.java`, `MidiCompilerContext.java` |
| Song/score domain model | `Song.java`, `Part.java`, `Pattern.java` |
| Error messages | `SymfonionExceptionThrower.java` |
| Smoke tests | `CliSmokeTest.java` |
