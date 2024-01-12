#!/usr/bin/env bash

[[ -e pom.xml ]] || {
  echo "Seems not to be in project top directory. Missing pom.xml"
  exit 1
}

top_dir="$(pwd)/src/site/asciidoc/example-project"
doc_base="${1:-${top_dir}}"

  # shellcheck disable=SC2156
find "${top_dir}" \
  -type d -not -path "${top_dir}" \
  -exec sh -c "echo 'include::../.attr.adoc[]' > {}/.attr.adoc" {} \;

echo "
:doc_base: ${doc_base}
" > src/site/asciidoc/example-project/.attr.adoc