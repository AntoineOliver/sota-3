package delivery_service.infrastructure.event;

import delivery_service.application.exception.DeliveryNotFoundException;
import delivery_service.application.port.DeliveryService;
import delivery_service.domain.model.DeliveryId;
import delivery_service.infrastructure.mapper.DroneEventMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
public class DroneEventListener {

    private final DroneEventMapper mapper;
    private final DeliveryService deliveryService;

    public DroneEventListener(
            DroneEventMapper mapper,
            DeliveryService deliveryService
    ) {
        this.mapper = mapper;
        this.deliveryService = deliveryService;
    }

    @KafkaListener(topics = "drone-events")
    public void onDroneEvent(String json) throws DeliveryNotFoundException {
        DroneEvent event = mapper.fromJson(json);

        switch (event.type()) {
            case DRONE_AT_PICKUP ->
                    deliveryService.onDroneAtPickup(
                            new DeliveryId(event.deliveryId())
                    );

            case DRONE_DELIVERED ->
                    deliveryService.onDroneDelivered(
                            new DeliveryId(event.deliveryId())
                    );

            case DRONE_LOCATION_UPDATED ->
                    deliveryService.onDroneLocationUpdated(
                            new DeliveryId(event.deliveryId()),
                            event.droneId(),
                            event.latitude(),
                            event.longitude()
                    );

            case DRONE_ASSIGNED -> {
                deliveryService.onDroneAssigned(
                        new DeliveryId(event.deliveryId()),
                        event.droneId()
                );
            }
        }
    }
}
