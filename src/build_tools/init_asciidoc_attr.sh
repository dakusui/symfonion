#!/usr/bin/env bash
set -E -o nounset -o errexit +o posix -o pipefail
shopt -s inherit_errexit

[[ -e pom.xml ]] || {
  echo "Seems not to be in project top directory. Missing pom.xml" >&2
  exit 1
}
myself="$(realpath "${0}")"
top_dir="$(pwd)/src/site/mmml"
doc_base="${1:-${top_dir}}"

# shellcheck disable=SC2156
find "${top_dir}" \
  -type d -not -path "${top_dir}" \
  -exec sh -c "printf '%s\n%s' '// CAUTION: This file is auto-generated. Add .attr.adoc to .gitignore and DO NOT EDIT //' 'include::../.attr.adoc[]' > {}/.attr.adoc" {} \;

_target_file="${top_dir}/.attr.adoc"
echo "
// CAUTION: This file is auto-generated. Add .attr.adoc to .gitignore and DON'T EDIT //
:doc_base: ${doc_base}
:directory-layout: {doc_base}/directory-layout
:project_root: {directory-layout}/home/USER/WORKSPACE/example-song
:project_src: {project_root}/src
:main_mmml_src: {project_root}/src/mmml
:target_dir: {project_root}/target
:target_mmml_dir: {project_root}/target/mmml
:xdg_config_home: {directory-layout}/home/USER/.config
:xdg_data_home: {directory-layout}/home/USER/.local/share
:xdg_local_bin: {directory-layout}/home/USER/.local/bin
:xdg_local_lib: {directory-layout}/home/USER/.local/lib
" > "${_target_file}"

if [[ "${doc_base}" == "${top_dir}" ]]; then
  echo "
[.text-right]
**<link:{directory-layout}/INDEX.adoc[top]>**
**<link:{project_root}/INDEX.adoc[project_root]>**
**<link:${myself}[update .attr base]>**
[.text-right]
- **<link:{xdg_config_home}/mmml/INDEX.adoc[XDG_CONFIG_HOME/mmml: +{xdg_config_home}+/mmml/INDEX.adoc]>**
- **<link:{xdg_data_home}/mmml/INDEX.adoc[XDG_DATA_HOME/mmml: +{xdg_data_home}+/mmml/INDEX.adoc]>**
- **<link:{xdg_local_bin}/INDEX.adoc[XDG_LOCAL_BIN: +{xdg_local_bin}+/INDEX.adoc]>**
- **<link:{xdg_local_lib}/mmml/INDEX.adoc[XDG_LOCAL_LIB/mmml: +{xdg_local_lib}+/mmml/INDEX.adoc]>**
- ** link:{doc_base}/features/mmml-pipeline.adoc[+{doc_base}+/features/mmml-pipeline.adoc]**
- ** link:{xdg_local_lib}/mmml/1.0.1/lib/json{plus}{plus}[+{xdg_local_lib}+/mmml/1.0.1/lib/json++]**
[.text-left]
" >> "${_target_file}"
fi