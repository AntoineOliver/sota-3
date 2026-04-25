#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v kubectl >/dev/null 2>&1; then
  echo "kubectl is required"
  exit 1
fi

if ! KUBE_CONTEXT="$(kubectl config current-context 2>/dev/null)"; then
  echo "No Kubernetes context is configured."
  echo "Start or select a cluster first, then rerun this script."
  exit 1
fi

if ! kubectl --request-timeout=5s get namespace default >/dev/null 2>&1; then
  echo "Kubernetes context '$KUBE_CONTEXT' is configured, but access is not working."
  echo "You are probably not logged in, or the cluster is not reachable."
  echo "Fix kubectl access first, then rerun this script."
  exit 1
fi

echo "[1/4] Building project jars"
bash ./gradlew clean build

echo
echo "[2/4] Building local images expected by the manifest"
docker build -t api-gateway:latest ./api-gateway
docker build -t delivery-service:latest ./delivery-service
docker build -t payment-service:latest ./payment-service
docker build -t dronefleet-service:latest ./dronefleet-service
docker build -t user-service:latest ./user-service
docker build -t notification-service:latest ./notification-service

echo
echo "[3/4] Applying Kubernetes resources"
kubectl apply -f k8s/shipping-on-the-air.yaml

echo
echo "[4/4] Current cluster status"
kubectl get pods -n shipping-on-the-air -o wide
echo
kubectl get svc -n shipping-on-the-air
echo
echo "Suggested follow-up commands:"
echo "kubectl logs deployment/payment-service -n shipping-on-the-air"
echo "kubectl port-forward svc/api-gateway 8080:8080 -n shipping-on-the-air"
echo "kubectl port-forward svc/prometheus 9090:9090 -n shipping-on-the-air"
