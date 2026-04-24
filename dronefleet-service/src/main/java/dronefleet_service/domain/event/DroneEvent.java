package dronefleet_service.domain.event;

import common.ddd.DomainEvent;

import java.time.Instant;

public interface DroneEvent {String type(); Instant occurredAt();  }
