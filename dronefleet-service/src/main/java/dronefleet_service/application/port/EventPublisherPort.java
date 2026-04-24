package dronefleet_service.application.port;

import common.hexagonal.OutBoundPort;
import dronefleet_service.domain.event.DroneEvent;

@OutBoundPort
public interface EventPublisherPort {
    void publish(DroneEvent event);
}
