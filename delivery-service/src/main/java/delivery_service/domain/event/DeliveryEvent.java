package delivery_service.domain.event;

import common.ddd.DomainEvent;

import java.time.Instant;

/**
 *
 * Base interface for SOTA Delivery domain events
 *
 */
public interface DeliveryEvent {String type(); Instant occurredAt();}