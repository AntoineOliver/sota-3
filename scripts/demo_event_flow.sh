#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

BASE_URL="${BASE_URL:-http://localhost:8080}"
PAYMENT_URL="${PAYMENT_URL:-http://localhost:8083}"
PROMETHEUS_URL="${PROMETHEUS_URL:-http://localhost:9090}"
RUN_ID="$(date +%s)"
EMAIL="demo-${RUN_ID}@test.com"
PASSWORD="${PASSWORD:-DemoPass1234}"
AUTH_HEADER_FILE="$(mktemp)"

require_command() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1"
    exit 1
  }
}

cleanup() {
  rm -f "$AUTH_HEADER_FILE"
}

trap cleanup EXIT

api_call() {
  local method="$1"
  local url="$2"
  shift 2

  curl -fsS -X "$method" \
    -H @"$AUTH_HEADER_FILE" \
    "$url" \
    "$@"
}

require_command curl
require_command jq

echo "[0/7] Checking exposed services"
curl -fsS "$BASE_URL/health" >/dev/null
curl -fsS "$PAYMENT_URL/health" >/dev/null

echo "[1/7] Registering a user"
USER_RESPONSE="$(curl -fsS -X POST \
  "$BASE_URL/api/users/register?name=Demo&email=$EMAIL&password=$PASSWORD")"
echo "$USER_RESPONSE" | jq .
USER_ID="$(echo "$USER_RESPONSE" | jq -r '.id')"

echo
echo "[2/7] Requesting JWT token"
TOKEN_RESPONSE="$(curl -fsS -X POST \
  "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\"}")"
echo "$TOKEN_RESPONSE" | jq .
TOKEN="$(echo "$TOKEN_RESPONSE" | jq -r '.token')"

if [[ -z "$TOKEN" || "$TOKEN" == "null" ]]; then
  echo "Could not retrieve JWT token from api-gateway."
  exit 1
fi

printf 'Authorization: Bearer %s\n' "$TOKEN" >"$AUTH_HEADER_FILE"

echo
echo "[3/7] Creating a delivery"
DELIVERY_RESPONSE="$(api_call POST "$BASE_URL/api/deliveries" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"$USER_ID\",
    \"pickupLocationLat\": 48.8566,
    \"pickupLocationLon\": 2.3522,
    \"dropoffLocationLat\": 48.8647,
    \"dropoffLocationLon\": 2.3490,
    \"weight\": 2.1,
    \"requestedTimeStart\": \"2026-01-14T14:00:00Z\",
    \"requestedTimeEnd\": \"2026-01-14T16:00:00Z\"
  }")"
echo "$DELIVERY_RESPONSE"
DELIVERY_ID="$(echo "$DELIVERY_RESPONSE" | tr -d '"')"
PAYMENT_ID="${USER_ID}_${DELIVERY_ID}"

echo
echo "[4/7] Starting the delivery saga"
api_call POST "$BASE_URL/api/deliveries/$DELIVERY_ID/start" >/dev/null
echo "Delivery started: $DELIVERY_ID"

echo
echo "[5/7] Polling saga, delivery status and payment state"
for _ in {1..24}; do
  STATUS_JSON="$(api_call GET "$BASE_URL/api/deliveries/$DELIVERY_ID/status")"
  SAGA_JSON="$(api_call GET "$BASE_URL/api/deliveries/$DELIVERY_ID/saga")"
  PAYMENT_STATE="$(curl -fsS "$PAYMENT_URL/payments/$PAYMENT_ID" 2>/dev/null || true)"

  echo "delivery-status:"
  echo "$STATUS_JSON" | jq .
  echo "saga:"
  echo "$SAGA_JSON" | jq .
  if [[ -n "$PAYMENT_STATE" ]]; then
    echo "payment-state: $PAYMENT_STATE"
  fi

  SAGA_STATE="$(echo "$SAGA_JSON" | jq -r '.state')"
  DELIVERY_STATE="$(echo "$STATUS_JSON" | jq -r '.status')"

  if [[ "$SAGA_STATE" == "COMPLETED" || "$DELIVERY_STATE" == "COMPLETED" ]]; then
    break
  fi
  sleep 5
done

echo
echo "[6/7] Showing payment event sourcing"
echo "Payment event sourcing stream:"
PAYMENT_EVENTS_JSON="$(api_call GET "$BASE_URL/api/payments/$PAYMENT_ID/events")"
echo "$PAYMENT_EVENTS_JSON" | jq .
echo
echo "[7/7] Showing payment metrics and Prometheus data"
echo "Payment metrics:"
curl -fsS "$PAYMENT_URL/metrics" | grep '^shipping_payment_' || true
echo
echo "Prometheus quick check (may be empty if scrape has not happened yet):"
curl -fsS "$PROMETHEUS_URL/api/v1/query?query=shipping_payment_commands_received_total" | jq .
echo
echo "Useful logs:"
echo "docker logs payment-service --tail 50"
echo "docker logs notification-service --tail 50"
