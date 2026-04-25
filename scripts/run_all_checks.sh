#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_IMAGES=0

if [[ "${1:-}" == "--build-images" ]]; then
  BUILD_IMAGES=1
fi

cd "$ROOT_DIR"

echo "[1/4] Running Gradle tests"
bash ./gradlew clean test

echo
echo "[2/4] Summarizing test reports"
total_tests=0
total_skipped=0
total_failures=0
total_errors=0

while IFS= read -r report; do
  line="$(grep -m1 "<testsuite " "$report")"
  if [[ "$line" =~ tests=\"([0-9]+)\" ]]; then
    tests="${BASH_REMATCH[1]}"
  else
    tests=0
  fi
  if [[ "$line" =~ skipped=\"([0-9]+)\" ]]; then
    skipped="${BASH_REMATCH[1]}"
  else
    skipped=0
  fi
  if [[ "$line" =~ failures=\"([0-9]+)\" ]]; then
    failures="${BASH_REMATCH[1]}"
  else
    failures=0
  fi
  if [[ "$line" =~ errors=\"([0-9]+)\" ]]; then
    errors="${BASH_REMATCH[1]}"
  else
    errors=0
  fi

  total_tests=$((total_tests + tests))
  total_skipped=$((total_skipped + skipped))
  total_failures=$((total_failures + failures))
  total_errors=$((total_errors + errors))

  printf " - %s: tests=%s skipped=%s failures=%s errors=%s\n" \
    "${report#./}" "$tests" "$skipped" "$failures" "$errors"
done < <(find . -path "*/build/test-results/test/TEST-*.xml" | sort)

echo "Totals: tests=$total_tests skipped=$total_skipped failures=$total_failures errors=$total_errors"

echo
echo "[3/4] Validating Docker Compose file"
docker compose config -q
echo "docker-compose.yml is valid"

echo
echo "[4/4] Checking Kubernetes manifest presence"
test -f k8s/shipping-on-the-air.yaml
echo "k8s/shipping-on-the-air.yaml is present"

if [[ "$BUILD_IMAGES" -eq 1 ]]; then
  echo
  echo "[extra] Building Docker images"
  docker compose build
fi

echo
echo "All checks completed successfully."
