package delivery_service.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import delivery_service.domain.event.DeliveryEvent;
import delivery_service.infrastructure.event.PaymentEvent;
import org.springframework.stereotype.Component;

/**
 * Mapper générique pour sérialiser et désérialiser les events
 * sans dépendre directement d'autres microservices.
 */

@Component
public class DomainEventMapper {

    private final ObjectMapper objectMapper;

    public DomainEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Object fromJson(String json) {
        try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(json);
            String type = node.has("type") ? node.get("type").asText() : "";

            return switch (type) {
                case "DeliveryCreatedEvent",
                     "DeliveryCompletedEvent",
                     "DeliveryFailedEvent",
                     "DeliveryStartedEvent",
                     "EtaUpdatedEvent",
                     "DeliveryCanceledEvent"
                        -> objectMapper.treeToValue(node, DeliveryEvent.class);

                default -> throw new IllegalArgumentException("Unknown delivery event type: " + type);
            };

        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize delivery event", e);
        }
    }
}
