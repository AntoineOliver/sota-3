
---

# Shipping on the Air – Assignment 3

## Event-Driven Reengineering, Observability, and Deployment Report

---

## 1. Context and Objectives

This document presents the evolution of the *Shipping on the Air* system from Assignment 2 to Assignment 3.

The main objectives of this assignment are:

* introducing an **event-driven architecture** for one microservice
* integrating **Kafka** as a messaging middleware
* defining **Service Level Objectives (SLOs)** and measuring **Service Level Indicators (SLIs)**
* preparing a **Kubernetes deployment**
* providing a clear explanation of the system suitable for demonstration and evaluation

---

## 2. Executive Summary

The microservice selected for event-driven reengineering is the **`payment-service`**.

### Before the changes

* `delivery-service` invoked `payment-service` synchronously using HTTP
* `payment-service` already published business events on Kafka (`payment-events`)
* however, command handling remained synchronous

### After the changes

* `delivery-service` publishes **commands** to Kafka (`payment-commands`)
* `payment-service` **consumes these commands asynchronously**
* `payment-service` continues publishing business events (`payment-events`)
* `delivery-service` and `notification-service` react to these events
* observability was improved with **metrics for SLIs/SLOs**
* a complete **Kubernetes deployment** was introduced

### Result

The system is now:

* more **loosely coupled**
* **asynchronous** on the payment workflow
* more **observable**
* aligned with **event-driven architecture principles**

---

## 3. Architecture Overview

### 3.1 Microservices

The system includes:

* api-gateway
* user-service
* delivery-service
* payment-service
* dronefleet-service
* notification-service
* Prometheus
* Kafka (via Redpanda)

---

### 3.2 Event-Driven Payment Workflow

The payment workflow is now fully event-driven:

1. a client request goes through the API gateway
2. the gateway forwards it to the delivery-service
3. the delivery-service publishes a Kafka command (`payment-commands`)
4. the payment-service consumes the command
5. it executes the business logic
6. it publishes the result as an event (`payment-events`)
7. the delivery-service updates its saga
8. the notification-service reacts and produces notifications

---

## 4. Event-Driven Reengineering of Payment-Service

### 4.1 Why Payment-Service Was Selected

The choice is justified by both technical and domain considerations:

* it already follows an **event-based lifecycle**
* it already publishes **domain events**
* it had **partial Kafka integration**
* only the command side was still synchronous
* removing HTTP calls eliminates a **critical coupling**

### Key insight

The service was already *internally event-driven*, making it the most natural candidate for full event-driven reengineering.

---

### 4.2 New Interaction Flow

The command side is now asynchronous:

1. `delivery-service` publishes a command on `payment-commands`
2. `payment-service` consumes the command
3. it executes payment or refund logic
4. it publishes results on `payment-events`
5. `delivery-service` reacts to continue or compensate the saga
6. `notification-service` consumes events unchanged

---

### 4.3 Implementation Changes

#### Delivery Side

* `PaymentAdapter` no longer performs HTTP calls
* it publishes Kafka commands:

    * `REQUEST_PAYMENT`
    * `REQUEST_REFUND`

#### Payment Side

* introduction of `PaymentCommandListener`
* consumes Kafka commands
* triggers business logic
* publishes existing domain events

---

## 5. Observability: SLOs and SLIs

Two SLOs were defined around the redesigned payment workflow.

---

### 5.1 SLO 1 – Payment Command Completion

**Objective:**
99% of payment commands must reach a terminal state

**SLI:**
ratio of completed commands over received commands

**Metrics used:**

* `shipping_payment_commands_received_total`
* `shipping_payment_commands_completed_total`
* `shipping_payment_commands_rejected_total`

**PromQL example:**

```promql
sum(rate(shipping_payment_commands_completed_total[5m]))
/
sum(rate(shipping_payment_commands_received_total[5m]))
```

---

### 5.2 SLO 2 – Payment Command Latency

**Objective:**
95% of commands must complete in less than 5 seconds

**SLI:**
end-to-end latency from command reception to final event emission

**Metric used:**

* `shipping_payment_command_duration_seconds`

**PromQL example:**

```promql
histogram_quantile(
  0.95,
  sum by (le, command) (
    rate(shipping_payment_command_duration_seconds_bucket[5m])
  )
)
```

---

### 5.3 Implementation Notes

* timing starts when a Kafka command is consumed
* timing stops when the final event is emitted
* Micrometer histograms are used for accurate latency measurement

---

### 5.4 Interpretation

* SLO 1 measures **reliability** (do commands complete?)
* SLO 2 measures **performance** (do they complete fast enough?)

---

## 6. Validation of Event-Driven Behavior

The transition to asynchronous communication can be verified through:

### Logs

* delivery-service logs show Kafka command publication
* payment-service logs show command consumption

### Business Events

* payment event history shows lifecycle transitions

### Metrics

* Prometheus metrics confirm command processing

### Key Point

The absence of direct HTTP calls between services demonstrates true **asynchronous decoupling**.

---

## 7. Notifications

The `notification-service` consumes events and produces side effects:

* simulated notifications are written to logs

This confirms:

* events are correctly propagated
* downstream consumers react properly

---

## 8. Kubernetes Deployment

The deployment is defined in:

* `k8s/shipping-on-the-air.yaml`

### Contents

* dedicated namespace: `shipping-on-the-air`
* Kafka (Redpanda)
* all microservice deployments and services
* Prometheus monitoring
* health probes (`/internal-health`)
* NodePort exposure for gateway and Prometheus

---

### 8.1 Scaling Strategy

Due to in-memory state limitations:

* `api-gateway`: replicated (2 instances)
* `notification-service`: replicated (2 instances)
* stateful services remain single-instance

This avoids state inconsistency while still demonstrating scalability concepts.

---

## 9. Execution and Deployment

### Build

```bash
./gradlew clean build
```

### Docker Images

```bash
docker build -t api-gateway:latest ./api-gateway
docker build -t delivery-service:latest ./delivery-service
docker build -t payment-service:latest ./payment-service
docker build -t dronefleet-service:latest ./dronefleet-service
docker build -t user-service:latest ./user-service
docker build -t notification-service:latest ./notification-service
```

### Kubernetes Deployment

```bash
kubectl apply -f k8s/shipping-on-the-air.yaml
```

### Useful Commands

```bash
kubectl get all -n shipping-on-the-air
kubectl logs deployment/payment-service -n shipping-on-the-air
kubectl port-forward svc/api-gateway 8080:8080 -n shipping-on-the-air
kubectl port-forward svc/prometheus 9090:9090 -n shipping-on-the-air
```

---

## 10. Limitations

Some limitations remain:

* several services rely on **in-memory storage**
* this limits horizontal scalability
* payment simulation is **probabilistic**
* production deployment would require:

    * persistent storage
    * better infrastructure management

### Interpretation

The system meets the **educational objectives**, but would require additional improvements for production use.

---

## 11. Conclusion

This project successfully fulfills the requirements of Assignment 3:

* a relevant microservice was transformed into an **event-driven architecture**
* Kafka is used for **asynchronous communication**
* SLOs and SLIs are defined and measurable
* a **Kubernetes deployment** is provided
* the system is **observable and demonstrable**

### Key Strength

The payment workflow is now:

* loosely coupled
* asynchronous
* observable
* technically consistent and easy to justify

---