package delivery_service.infrastructure.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import delivery_service.infrastructure.event.DroneEvent;
import org.springframework.stereotype.Component;

@Component
public class DroneEventMapper {

    private final ObjectMapper objectMapper;

    public DroneEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DroneEvent fromJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            String deliveryId = node.get("deliveryId").asText();
            String typeRaw = node.get("type").asText();

            DroneEvent.DroneEventType type =
                    switch (typeRaw) {
                        case "DroneAssignedEvent" -> DroneEvent.DroneEventType.DRONE_ASSIGNED;
                        case "DroneAtPickupEvent" -> DroneEvent.DroneEventType.DRONE_AT_PICKUP;
                        case "DroneDeliveredEvent" -> DroneEvent.DroneEventType.DRONE_DELIVERED;
                        case "DroneLocationUpdatedEvent" -> DroneEvent.DroneEventType.DRONE_LOCATION_UPDATED;
                        default -> throw new IllegalArgumentException("Unknown drone event type: " + typeRaw);
                    };

            String droneId = node.has("droneId") ? node.get("droneId").asText() : null;
            Double latitude = node.has("latitude") ? node.get("latitude").asDouble() : null;
            Double longitude = node.has("longitude") ? node.get("longitude").asDouble() : null;

            return new DroneEvent(
                    deliveryId,
                    droneId,
                    type,
                    latitude,
                    longitude
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to map DroneEvent", e);
        }
    }
}
