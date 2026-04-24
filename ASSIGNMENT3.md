# Assignment 3 - Shipping on the Air

## 1. Selected microservice for event-driven reengineering

The selected candidate is `payment-service`.

Why this service is the best fit:

- In assignment 2, `delivery-service` was still calling `payment-service` synchronously over HTTP for payment and refund requests.
- `payment-service` was already publishing business events on Kafka (`payment-events`), so it was naturally close to an event-driven design but still exposed a synchronous command side.
- Reengineering this service removes a critical synchronous dependency from the delivery saga while keeping the rest of the system mostly stable.

### New interaction flow

The command side is now asynchronous:

1. `delivery-service` publishes a command on Kafka topic `payment-commands`.
2. `payment-service` consumes the command and starts the payment or refund workflow.
3. `payment-service` publishes the result on Kafka topic `payment-events`.
4. `delivery-service` reacts to these events to continue or compensate the saga.
5. `notification-service` continues to consume `payment-events` unchanged.

This means `payment-service` now interacts with the rest of the system through Kafka for its business commands and outcomes, which satisfies the assignment requirement of introducing an event-driven architecture for one microservice.

## 2. SLOs and measured SLIs

Two SLOs were defined around the redesigned payment flow.

### SLO 1 - Payment command completion

Goal:

- 99% of payment commands received by `payment-service` must reach a terminal outcome within the observation window.

Measured SLI:

- Ratio between completed payment commands and received payment commands.

Prometheus metrics:

- `shipping_payment_commands_received_total{command=...}`
- `shipping_payment_commands_completed_total{command=..., outcome=...}`
- `shipping_payment_commands_rejected_total{command=...}`

PromQL example:

```promql
sum(rate(shipping_payment_commands_completed_total[5m]))
/
sum(rate(shipping_payment_commands_received_total[5m]))
```

### SLO 2 - Payment command latency

Goal:

- 95% of payment and refund commands must complete in less than 5 seconds.

Measured SLI:

- End-to-end latency from Kafka command reception to the terminal business event emitted by `payment-service`.

Prometheus metric:

- `shipping_payment_command_duration_seconds`

PromQL example:

```promql
histogram_quantile(
  0.95,
  sum by (le, command) (
    rate(shipping_payment_command_duration_seconds_bucket[5m])
  )
)
```

Implementation notes:

- The timer starts when a command is consumed from `payment-commands`.
- The timer stops when `payment-service` emits the final outcome (`SUCCEEDED` or `FAILED`).
- Micrometer histogram support was enabled so the latency SLI is directly queryable from Prometheus.

## 3. Kubernetes deployment

A Kubernetes deployment is provided in:

- [k8s/shipping-on-the-air.yaml](/home/antoineoliver/projets/SAP/A3/sota-2/k8s/shipping-on-the-air.yaml)

The manifest contains:

- a dedicated namespace: `shipping-on-the-air`
- Kafka middleware using Redpanda
- Deployments and Services for all microservices
- `NodePort` exposure for `api-gateway` and Prometheus
- health probes using `/internal-health`
- Prometheus configuration for scraping `/metrics`

### Scaling choices

Because several services still use in-memory state, horizontal scaling was applied conservatively:

- `api-gateway`: `replicas: 2`
- `notification-service`: `replicas: 2`
- stateful or in-memory business services stay at `replicas: 1`

This keeps the deployment realistic and avoids incorrect state divergence for services that are not yet backed by persistent shared storage.

## 4. How to run

Build the jars and local images first:

```bash
./gradlew clean build
docker build -t api-gateway:latest ./api-gateway
docker build -t delivery-service:latest ./delivery-service
docker build -t payment-service:latest ./payment-service
docker build -t dronefleet-service:latest ./dronefleet-service
docker build -t user-service:latest ./user-service
docker build -t notification-service:latest ./notification-service
```

Deploy on Kubernetes:

```bash
kubectl apply -f k8s/shipping-on-the-air.yaml
```

Useful commands:

```bash
kubectl get all -n shipping-on-the-air
kubectl get svc -n shipping-on-the-air
kubectl logs deployment/payment-service -n shipping-on-the-air
kubectl port-forward svc/api-gateway 8080:8080 -n shipping-on-the-air
kubectl port-forward svc/prometheus 9090:9090 -n shipping-on-the-air
```
