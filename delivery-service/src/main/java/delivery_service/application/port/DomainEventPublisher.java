package delivery_service.application.port;

import common.hexagonal.OutBoundPort;
import delivery_service.domain.event.DeliveryEvent;


@OutBoundPort
public interface DomainEventPublisher {
    void publishEvent(DeliveryEvent event);
}
