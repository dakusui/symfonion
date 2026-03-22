---
name: symfonion-bash
description: >
  Conventions and structure for writing bash scripts in the SyMFONION project.
  Use this skill whenever you are asked to write, create, or significantly modify
  a bash script (.sh file) in this project — including bootstrap scripts, helper
  scripts, CI scripts, or any other shell scripts. Apply it even if the user just
  says "write a script to do X" without explicitly mentioning conventions.
---

# SyMFONION Bash Script Conventions

Follow these conventions for every bash script in the project. They exist to keep
scripts readable, portable, and safe to source or call from any working directory.

## Header

Every script starts with a strict-mode header:

```bash
#!/usr/bin/env bash

set -eu -o pipefail -o errtrace
```

`-e` aborts on error, `-u` catches unset variables, `pipefail` propagates errors
through pipes, and `errtrace` makes ERR traps inherit into functions and subshells.

## Naming conventions

| Kind | Form | Example | Notes |
|---|---|---|---|
| Local variable | `_name` | `_projectdir` | Always declare with `local` inside functions |
| Global variable | `name` | `projectdir` | Set outside functions; readable from anywhere in the file |
| Environment variable | `NAME` | `JAVA_HOME` | Exported to child processes; uppercase throughout |
| Private function | `_name` | `_parse_args` | Only called from within the same file |
| Public function | `name` | `main`, `progress` | Safe to source and call from outside the file |

The leading underscore signals "internal — don't call me from outside". Public
functions have no prefix so callers can invoke them naturally.

When a private function belongs to a logical module and name collision is a concern
(e.g., in a file that may be sourced alongside others), use a double-underscore
namespace prefix: `__module__function_name`. See `__bootstrap__checkenv` in
`bootstrap.sh` for an example.

## Structure

A script has exactly three regions, in this order:

1. **Header** — shebang + `set` flags
2. **Definitions** — global/environment variable declarations and function definitions
   (both private and public). No side effects here.
3. **Entry point** — a single line (or subshell block) that calls `main`. Nothing
   else lives here.

```bash
#!/usr/bin/env bash

set -eu -o pipefail -o errtrace

# --- definitions ---

some_global="value"

function _helper() {
  local _x="${1}"
  echo "${_x}"
}

function main() {
  local _arg="${1}"
  _helper "${_arg}"
}

# --- entry point ---

main "${@}"
```

`main` receives all command-line arguments (`"${@}"`). All logic lives inside
`main` or functions called by it. Keep the entry-point region free of assignments
and function definitions so the script's startup behaviour is easy to audit at a
glance.

## Script location

All project scripts live under `src/build_tools/bin/`. When creating a new script,
place it there unless there is a specific reason to put it elsewhere (e.g.,
`bootstrap.sh` lives at the project root because it must be found before the build
tooling exists).

## Working-directory portability

Scripts must not assume they are run from the project root or any other specific
directory. If a script genuinely needs to operate from a particular directory,
change into it inside a subshell so the caller's working directory is unaffected:

```bash
# --- entry point ---

(cd "$(dirname "${BASH_SOURCE[0]}")" && main "${@}")
```

Use `dirname "${BASH_SOURCE[0]}"` (the script's own directory) or an absolute path
derived from it. Never hardcode paths relative to an assumed working directory.

If the script computes a project root from its own location, do it inside `main`:

```bash
function main() {
  local _scriptdir
  _scriptdir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  local _projectdir
  _projectdir="$(realpath "${_scriptdir}/..")"
  # ...
}
```

## Local variables

Declare every variable inside a function with `local` on the same line as the
first assignment where possible, or as a bare `local` declaration before use:

```bash
function greet() {
  local _name="${1}"
  local _greeting
  _greeting="Hello, ${_name}!"
  echo "${_greeting}"
}
```

Never rely on a variable being unset just because you didn't set it — use `local`
to scope it explicitly.

## A complete minimal example

```bash
#!/usr/bin/env bash

set -eu -o pipefail -o errtrace

script_name="my-script"

function _check_args() {
  local _count="${1}"
  [[ "${_count}" -ge 1 ]] || {
    echo "Usage: ${script_name} <target>" >&2
    return 1
  }
}

function run() {
  local _target="${1}"
  echo "Running against: ${_target}"
}

function main() {
  _check_args "${#}"
  local _target="${1}"
  run "${_target}"
}

(cd "$(dirname "${BASH_SOURCE[0]}")" && main "${@}")
```
