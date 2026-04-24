#!/bin/bash
set -e

BASE_URL="http://localhost:8080"

echo "🚀 START SCENARIO (API GATEWAY ONLY)"

# ----------------------------
# 1. CREATE BASE
# ----------------------------
echo "👉 Creating base..."

BASE_RESPONSE=$(curl -s -X POST \
"$BASE_URL/api/drones/base?name=DEFENSE&lat=48.8924&lon=2.2369&capacity=10")

echo "RAW BASE RESPONSE:"
echo "$BASE_RESPONSE" | jq
sleep 2

BASE_ID=$(echo "$BASE_RESPONSE" | jq -r '.baseId')

echo "✅ Base created: $BASE_ID"

# ----------------------------
# 2. CREATE DRONE
# ----------------------------
echo "👉 Creating drone..."

DRONE_RESPONSE=$(curl -s -X POST \
"$BASE_URL/api/drones/drone?baseId=$BASE_ID")

echo "$DRONE_RESPONSE" | jq
sleep 2

echo "✅ Drone created"

# ----------------------------
# 3. REGISTER USER
# ----------------------------
echo "👉 Register user..."

USER_RESPONSE=$(curl -s -X POST \
"$BASE_URL/api/users/register?name=Antoine&email=antoine@test.com&password=AnTOIne1234")

echo "$USER_RESPONSE" | jq
sleep 2

USER_ID=$(echo "$USER_RESPONSE" | jq -r '.id')

echo "✅ User ID: $USER_ID"

# ----------------------------
# 4. CREATE DELIVERY
# ----------------------------
echo "👉 Creating delivery..."

DELIVERY_RESPONSE=$(curl -s -X POST "$BASE_URL/api/deliveries" \
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
  }")

echo "$DELIVERY_RESPONSE"

DELIVERY_ID=$(echo "$DELIVERY_RESPONSE" | tr -d '"')

echo "✅ DELIVERY ID: $DELIVERY_ID"

# ----------------------------
# 5. START DELIVERY
# ----------------------------
echo "🚚 Starting delivery..."

curl -s -X POST \
"$BASE_URL/api/deliveries/$DELIVERY_ID/start"

echo "✅ Delivery started"

sleep 5

# ----------------------------
# 6. PAYMENT
# ----------------------------
echo "💳 Payment start..."

curl -s -X POST \
"$BASE_URL/api/payments/${USER_ID}_${DELIVERY_ID}/start"

sleep 2

PAYMENT_STATUS=$(curl -s \
"$BASE_URL/api/payments/${USER_ID}_${DELIVERY_ID}" | tr -d '"')

echo "📊 Payment status: $PAYMENT_STATUS"

if [ "$PAYMENT_STATUS" = "PAYMENT_FAILED" ]; then
  echo "❌ STOP"
  exit 1
fi

sleep 3

# ----------------------------
# 7. MONITORING
# ----------------------------
echo "📡 MONITORING"

END=$((SECONDS+120))

while [ $SECONDS -lt $END ]; do

  echo "----"

  curl -s "$BASE_URL/api/deliveries/$DELIVERY_ID/status" | jq
  curl -s "$BASE_URL/api/deliveries/$DELIVERY_ID/remaining-time" | jq
  curl -s "$BASE_URL/api/drones/drone?droneId=D-00001" | jq

  STATUS=$(curl -s "$BASE_URL/api/deliveries/$DELIVERY_ID/status" | jq -r '.status')

  if [ "$STATUS" = "COMPLETED" ]; then
    echo "🎉 DONE"
    exit 0
  fi

  sleep 5
done