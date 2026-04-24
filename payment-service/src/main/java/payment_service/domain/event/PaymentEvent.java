package payment_service.domain.event;

import common.ddd.DomainEvent;

import java.time.Instant;

public interface PaymentEvent { String type(); Instant occurredAt(); }
