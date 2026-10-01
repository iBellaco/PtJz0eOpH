#!/usr/bin/env bash
set -euo pipefail

# Run from any directory; no Android SDK is needed for the source audit.
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
audit_dir="${COACH_AUDIT_DIR:-${TMPDIR:-/tmp}/coach-portuguese-audit}"
mkdir -p "$audit_dir/deps"
maven_root="https://repo.maven.apache.org/maven2"
dependencies=(
  org/jetbrains/kotlin/kotlin-compiler-embeddable/2.2.10/kotlin-compiler-embeddable-2.2.10.jar
  org/jetbrains/kotlin/kotlin-stdlib/2.2.10/kotlin-stdlib-2.2.10.jar
  org/jetbrains/kotlin/kotlin-script-runtime/2.2.10/kotlin-script-runtime-2.2.10.jar
  org/jetbrains/kotlin/kotlin-reflect/1.6.10/kotlin-reflect-1.6.10.jar
  org/jetbrains/kotlin/kotlin-daemon-embeddable/2.2.10/kotlin-daemon-embeddable-2.2.10.jar
  org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.8.0/kotlinx-coroutines-core-jvm-1.8.0.jar
  org/jetbrains/annotations/13.0/annotations-13.0.jar
  org/json/json/20240303/json-20240303.jar
)
for dependency in "${dependencies[@]}"; do
  destination="$audit_dir/deps/${dependency##*/}"
  if [ ! -s "$destination" ]; then
    curl --fail --silent --show-error --location --retry 3 "$maven_root/$dependency" -o "$destination.part"
    mv "$destination.part" "$destination"
  fi
done
compile_classpath="$audit_dir/deps/kotlin-stdlib-2.2.10.jar:$audit_dir/deps/json-20240303.jar:$audit_dir/deps/kotlin-compiler-embeddable-2.2.10.jar"
cd "$project_root"
for audit in UserVisiblePortugueseAudit PortugueseAudit; do
  java -cp "$audit_dir/deps/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
    -no-stdlib -no-reflect -classpath "$compile_classpath" \
    "tools/$audit.kt" app/src/main/java/com/example/util/TranslationCatalog.kt \
    app/src/main/java/com/example/util/TranslationAssets.kt -d "$audit_dir/$audit.jar"
done
java -Xmx1g -cp "$audit_dir/UserVisiblePortugueseAudit.jar:$audit_dir/deps/*" \
  UserVisiblePortugueseAuditKt "$project_root" "$audit_dir/visible.json" --check
java -Xmx1g -cp "$audit_dir/PortugueseAudit.jar:$audit_dir/deps/*" \
  PortugueseAuditKt "$project_root" "$audit_dir/all-unchanged.json" --all
echo "Audit reports: $audit_dir"
