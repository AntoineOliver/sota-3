#!/bin/bash
set -e

echo "🚀 Building all services..."

docker compose build

echo "🚀 Starting all services..."

docker compose up -d

echo "✅ All services are running"

echo ""
echo "📡 Services:"
echo " - Delivery      : http://localhost:8081"
echo " - User          : http://localhost:8082"
echo " - Payment       : http://localhost:8083"
echo " - Dronefleet    : http://localhost:8084"
echo " - Notification  : http://localhost:8085"
echo " - Kafka         : localhost:9092"