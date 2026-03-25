#!/usr/bin/env bash
# test_pipeline.sh — Integration tests for the symfonion launcher pipeline.
#
# Tests cover:
#   - JSON input passes through without preprocessing  (backward compat)
#   - YAML input triggers the yq stage
#   - YAML++ input triggers the yq and jq++ stages
#   - --skip-yq  disables the yq stage for a YAML file
#   - --skip-jqpp disables the jq++ stage for a YAML++ file
#   - A missing yq dependency produces a friendly error message
#
# All tests use --dry-run to avoid requiring a compiled SyMFONION JAR or a
# real Java installation.  The yq and jq++ tools are mocked with simple
# identity-transform scripts (they copy their input to stdout unchanged),
# which is sufficient to verify that the correct stages are invoked.
#
# Usage:
#   bash src/test/scripts/test_pipeline.sh

set -eu -o pipefail -o errtrace

# --- definitions ---

_script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
_resources_dir="${_script_dir}/resources"
_launcher_src="${_script_dir}/../../main/dist/bin/symfonion"

_pass_count=0
_fail_count=0
_skip_count=0

# Per-run state set by _run_launcher_capture.
_last_exit=0
_last_stdout=""
_last_stderr=""

# Temp directory for the current test case, set by _setup / cleared by _teardown.
_test_home=""

function _pass() {
  local _name="${1}"
  printf 'PASS  %s\n' "${_name}"
  (( _pass_count += 1 )) || true
}

function _fail() {
  local _name="${1}"
  local _msg="${2:-}"
  printf 'FAIL  %s%s\n' "${_name}" "${_msg:+  [${_msg}]}"
  (( _fail_count += 1 )) || true
}

function _skip() {
  local _name="${1}"
  local _reason="${2:-}"
  printf 'SKIP  %s%s\n' "${_name}" "${_reason:+  (${_reason})}"
  (( _skip_count += 1 )) || true
}

# Set up an isolated test environment under a fresh temp directory.
# After this call the following globals are set:
#   _test_home         — root of the isolated environment
#   _mock_dir          — directory prepended to PATH; contains mock yq and jq++
#   _patched_launcher  — the launcher script with ${project.version} replaced
function _setup() {
  _test_home="$(mktemp -d)"
  _mock_dir="${_test_home}/mock_bin"
  local _home_dir="${_test_home}/home"

  mkdir -p \
    "${_home_dir}/bin" \
    "${_home_dir}/lib" \
    "${_home_dir}/share/symfonion/prelude" \
    "${_mock_dir}"

  # Patch the Maven token so bash can parse the launcher's function body.
  # (${project.version} contains '.', which is not a valid bash identifier
  # character; Maven substitutes it at assembly time.)
  # --dry-run never reaches the exec line, but bash parses the whole function
  # at definition time, so we must substitute before sourcing.
  _patched_launcher="${_home_dir}/bin/symfonion"
  sed 's/\${project\.version}/TEST/g' "${_launcher_src}" > "${_patched_launcher}"
  chmod +x "${_patched_launcher}"

  # Mock yq: identity transform — outputs the last argument (the input file)
  # unchanged.  Real yq converts YAML/JSON to JSON; our test fixtures are
  # already valid for pipeline testing without actual YAML parsing.
  cat > "${_mock_dir}/yq" << 'MOCK_YQ'
#!/usr/bin/env bash
# Mock yq for pipeline tests.
cat "${@: -1}"
MOCK_YQ
  chmod +x "${_mock_dir}/yq"

  # Mock jq++: identity transform.
  cat > "${_mock_dir}/jq++" << 'MOCK_JQPP'
#!/usr/bin/env bash
# Mock jq++ for pipeline tests.
cat "${@: -1}"
MOCK_JQPP
  chmod +x "${_mock_dir}/jq++"
}

# Remove the temp directory created by _setup.
function _teardown() {
  [[ -z "${_test_home}" ]] && return 0
  rm -rf "${_test_home}"
  _test_home=""
}

# Run the patched launcher with mock tools on PATH; capture exit code,
# stdout, and stderr into the globals _last_exit, _last_stdout, _last_stderr.
function _run_launcher_capture() {
  _last_exit=0
  local _out="${_test_home}/stdout"
  local _err="${_test_home}/stderr"
  PATH="${_mock_dir}:${PATH}" bash "${_patched_launcher}" "${@}" \
    > "${_out}" 2> "${_err}" || _last_exit="${?}"
  _last_stdout="$(cat "${_out}")"
  _last_stderr="$(cat "${_err}")"
}

# -----------------------------------------------------------------------
# Test cases
# -----------------------------------------------------------------------

function test_json_bypasses_preprocessing() {
  local _name="JSON input bypasses preprocessing"
  _setup

  # For a .json file the launcher should not call yq; verify by checking that
  # --print-intermediate produces no stage output headers in stderr.
  _run_launcher_capture \
    --dry-run --print-intermediate \
    -q "${_resources_dir}/minimal.json"

  if [[ "${_last_exit}" -ne 0 ]]; then
    _fail "${_name}" "exit ${_last_exit}; stderr: ${_last_stderr}"
  elif printf '%s' "${_last_stderr}" | grep -q '\[yq stage output\]'; then
    _fail "${_name}" "yq stage ran for a .json file (should be bypassed)"
  else
    _pass "${_name}"
  fi
  _teardown
}

function test_yaml_triggers_yq_stage() {
  local _name="YAML input triggers yq stage"
  _setup

  _run_launcher_capture \
    --dry-run --print-intermediate \
    -q "${_resources_dir}/minimal.yaml"

  if [[ "${_last_exit}" -ne 0 ]]; then
    _fail "${_name}" "exit ${_last_exit}; stderr: ${_last_stderr}"
  elif ! printf '%s' "${_last_stderr}" | grep -q '\[yq stage output\]'; then
    _fail "${_name}" "yq stage output header not found in stderr"
  elif printf '%s' "${_last_stderr}" | grep -q '\[jq++ stage output\]'; then
    _fail "${_name}" "jq++ stage ran for a .yaml file (should be disabled)"
  else
    _pass "${_name}"
  fi
  _teardown
}

function test_jfpp_triggers_both_stages() {
  local _name="YAML++ (.jfpp) input triggers yq and jq++ stages"
  _setup

  _run_launcher_capture \
    --dry-run --print-intermediate \
    -q "${_resources_dir}/minimal.jfpp"

  if [[ "${_last_exit}" -ne 0 ]]; then
    _fail "${_name}" "exit ${_last_exit}; stderr: ${_last_stderr}"
  elif ! printf '%s' "${_last_stderr}" | grep -q '\[yq stage output\]'; then
    _fail "${_name}" "yq stage output header missing from stderr"
  elif ! printf '%s' "${_last_stderr}" | grep -q '\[jq++ stage output\]'; then
    _fail "${_name}" "jq++ stage output header missing from stderr"
  else
    _pass "${_name}"
  fi
  _teardown
}

function test_skip_yq_flag() {
  local _name="--skip-yq disables yq stage for YAML input"
  _setup

  _run_launcher_capture \
    --skip-yq --dry-run --print-intermediate \
    -q "${_resources_dir}/minimal.yaml"

  if [[ "${_last_exit}" -ne 0 ]]; then
    _fail "${_name}" "exit ${_last_exit}; stderr: ${_last_stderr}"
  elif printf '%s' "${_last_stderr}" | grep -q '\[yq stage output\]'; then
    _fail "${_name}" "yq stage ran despite --skip-yq"
  else
    _pass "${_name}"
  fi
  _teardown
}

function test_skip_jqpp_flag() {
  local _name="--skip-jqpp disables jq++ stage for YAML++ input"
  _setup

  _run_launcher_capture \
    --skip-jqpp --dry-run --print-intermediate \
    -q "${_resources_dir}/minimal.jfpp"

  if [[ "${_last_exit}" -ne 0 ]]; then
    _fail "${_name}" "exit ${_last_exit}; stderr: ${_last_stderr}"
  elif ! printf '%s' "${_last_stderr}" | grep -q '\[yq stage output\]'; then
    _fail "${_name}" "yq stage should still run when only --skip-jqpp is passed"
  elif printf '%s' "${_last_stderr}" | grep -q '\[jq++ stage output\]'; then
    _fail "${_name}" "jq++ stage ran despite --skip-jqpp"
  else
    _pass "${_name}"
  fi
  _teardown
}

function test_missing_yq_gives_friendly_error() {
  local _name="Missing yq gives friendly error message"
  _setup

  # Remove the mock yq so it is absent from PATH.
  rm -f "${_mock_dir}/yq"

  _run_launcher_capture \
    --dry-run \
    -q "${_resources_dir}/minimal.yaml"

  if [[ "${_last_exit}" -eq 0 ]]; then
    _fail "${_name}" "expected non-zero exit when yq is missing"
  elif ! printf '%s' "${_last_stderr}" | grep -q "'yq'"; then
    _fail "${_name}" "error message did not mention 'yq'; stderr: ${_last_stderr}"
  elif ! printf '%s' "${_last_stderr}" | grep -q -- '--skip-yq'; then
    _fail "${_name}" "error message did not mention '--skip-yq'; stderr: ${_last_stderr}"
  else
    _pass "${_name}"
  fi
  _teardown
}

function test_stage_control_flags_not_forwarded_to_java() {
  local _name="Launcher flags are not forwarded to Java arg list"
  _setup

  # Use a mock Java that records its arguments so we can inspect them.
  cat > "${_mock_dir}/java" << 'MOCK_JAVA'
#!/usr/bin/env bash
# Mock Java: print all arguments to stdout.
printf '%s\n' "${@}"
MOCK_JAVA
  chmod +x "${_mock_dir}/java"

  # Patch the launcher further: make it call the mock Java (by removing 'exec'
  # and letting our mock java be first on PATH).
  # We re-enable the main stage for this test (no --dry-run).
  # The mock Java will exit 0, so the launcher will exit 0 too.
  _run_launcher_capture \
    --skip-yq --skip-jqpp \
    -q "${_resources_dir}/minimal.json"

  # The mock Java prints its args to stdout.  None of our flags should appear.
  if printf '%s' "${_last_stdout}" | grep -qE -- '--skip-yq|--skip-jqpp|--dry-run|--print-intermediate'; then
    _fail "${_name}" "launcher flag leaked into Java args; stdout: ${_last_stdout}"
  else
    _pass "${_name}"
  fi
  _teardown
}

# -----------------------------------------------------------------------
# Summary
# -----------------------------------------------------------------------

function main() {
  printf 'Running symfonion launcher pipeline tests...\n\n'

  test_json_bypasses_preprocessing
  test_yaml_triggers_yq_stage
  test_jfpp_triggers_both_stages
  test_skip_yq_flag
  test_skip_jqpp_flag
  test_missing_yq_gives_friendly_error
  test_stage_control_flags_not_forwarded_to_java

  printf '\nResults: %d passed, %d failed, %d skipped\n' \
    "${_pass_count}" "${_fail_count}" "${_skip_count}"

  [[ "${_fail_count}" -eq 0 ]]
}

# --- entry point ---

main "${@}"
