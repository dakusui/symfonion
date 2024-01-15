#!/usr/bin/env bash

[[ -e pom.xml ]] || {
  echo "Seems not to be in project top directory. Missing pom.xml" >&2
  exit 1
}
myself="$(realpath "${0}")"
top_dir="$(pwd)/src/site/asciidoc/example-project"
doc_base="${1:-${top_dir}}"

# shellcheck disable=SC2156
find "${top_dir}" \
  -type d -not -path "${top_dir}" \
  -exec sh -c "printf '%s\n%s' '// CAUTION: This file is auto-generated. Add .attr.adoc to .gitignore and DO NOT EDIT //' 'include::../.attr.adoc[]' > {}/.attr.adoc" {} \;

_target_file="src/site/asciidoc/example-project/.attr.adoc"
echo "
// CAUTION: This file is auto-generated. Add .attr.adoc to .gitignore and DON'T EDIT //
:doc_base: ${doc_base}
:project_root: ${doc_base}/home/USER/WORKSPACE
:project_src: ${doc_base}/home/USER/WORKSPACE/src
:main_mmml_src: ${doc_base}/home/USER/WORKSPACE/src/mmml
:target_dir: ${doc_base}/home/USER/WORKSPACE/target
:target_mmml_dir: ${doc_base}/home/USER/WORKSPACE/target/mmml
" > "${_target_file}"

if [[ "${doc_base}" == "${top_dir}" ]]; then
  echo "
[.text-right]
**<link:{doc_base}/INDEX.adoc[top]>**
**<link:{doc_base}/home/USER/WORKSPACE/example-song/INDEX.adoc[top]>**
**<link:${myself}[update .attr base]>**

[.text-left]
" >> "${_target_file}"
fi