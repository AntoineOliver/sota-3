#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLEAN_CONFLICTS=0

if [[ "${1:-}" == "--clean-conflicts" ]]; then
  CLEAN_CONFLICTS=1
fi

cd "$ROOT_DIR"

required_containers=(
  kafka
  delivery-service
  user-service
  payment-service
  dronefleet-service
  notification-service
  api-gateway
  prometheus
)

conflicts=()
for name in "${required_containers[@]}"; do
  if docker ps -a --format '{{.Names}}' | grep -Fxq "$name"; then
    conflicts+=("$name")
  fi
done

if (( ${#conflicts[@]} > 0 )) && [[ "$CLEAN_CONFLICTS" -eq 0 ]]; then
  echo "Conflicting container names already exist:"
  printf " - %s\n" "${conflicts[@]}"
  echo
  echo "If they are from an old run, remove them first with:"
  echo "docker rm -f ${conflicts[*]}"
  echo
  echo "Or rerun this script with:"
  echo "./scripts/demo_compose_stack.sh --clean-conflicts"
  exit 1
fi

if (( ${#conflicts[@]} > 0 )) && [[ "$CLEAN_CONFLICTS" -eq 1 ]]; then
  echo "Removing old conflicting containers"
  docker rm -f "${conflicts[@]}"
fi

echo "[1/3] Building and starting the stack"
docker compose up -d --build

echo
echo "[2/3] Waiting for HTTP services"
services=(
  "api-gateway|http://localhost:8080/health"
  "delivery-service|http://localhost:8081/health"
  "user-service|http://localhost:8082/health"
  "payment-service|http://localhost:8083/health"
  "dronefleet-service|http://localhost:8084/health"
  "notification-service|http://localhost:8085/health"
)

for service in "${services[@]}"; do
  name="${service%%|*}"
  url="${service##*|}"
  echo " - waiting for $name"
  ready=0
  for _ in {1..60}; do
    if curl -fsS "$url" >/dev/null 2>&1; then
      ready=1
      break
    fi
    sleep 2
  done
  if [[ "$ready" -ne 1 ]]; then
    echo "$name did not become healthy in time"
    docker compose ps
    exit 1
  fi
done

echo
echo "[3/3] Final status"
docker compose ps
echo
echo "Gateway:     http://localhost:8080"
echo "Prometheus:  http://localhost:9090"
echo "Kafka:       localhost:9092"
echo
echo "Use ./scripts/demo_event_flow.sh to run the end-to-end scenario."
