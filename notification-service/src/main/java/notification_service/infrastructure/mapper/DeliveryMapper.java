package notification_service.infrastructure.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import notification_service.infrastructure.event.DeliveryEvent;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMapper {

    private final ObjectMapper objectMapper;

    public DeliveryMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DeliveryEvent fromJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            String deliveryId = node.get("deliveryId").asText();
            String userId = node.get("userId").asText();
            String typeRaw = node.get("type").asText();

            DeliveryEvent.DeliveryEventType type = switch (typeRaw) {
                case "DeliveryCreatedEvent" -> DeliveryEvent.DeliveryEventType.DELIVERY_CREATED;
                case "DeliveryStartedEvent" -> DeliveryEvent.DeliveryEventType.DELIVERY_STARTED;
                case "DeliveryCompletedEvent" -> DeliveryEvent.DeliveryEventType.DELIVERY_COMPLETED;
                case "DeliveryFailedEvent" -> DeliveryEvent.DeliveryEventType.DELIVERY_FAILED;
                case "DeliveryCanceledEvent" -> DeliveryEvent.DeliveryEventType.DELIVERY_CANCELED;
                case "EtaUpdatedEvent" -> DeliveryEvent.DeliveryEventType.ETA_UPDATED;
                case "WaitForPickUpEvent" -> DeliveryEvent.DeliveryEventType.WAIT_FOR_PICKUP;
                default -> throw new IllegalArgumentException("Unknown delivery event: " + typeRaw);
            };

            String reason = node.has("reason") ? node.get("reason").asText() : null;
            String eta = node.has("newEta") ? node.get("newEta").asText() : null;
            Integer remainingMinutes = node.has("remainingMinutes")
                    ? node.get("remainingMinutes").asInt()
                    : null;

            return new DeliveryEvent(
                    deliveryId,
                    userId,
                    type,
                    reason,
                    eta,
                    remainingMinutes
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to map DeliveryEvent", e);
        }
    }
}