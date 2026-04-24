package delivery_service.infrastructure.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import delivery_service.infrastructure.event.PaymentEvent;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventMapper {

    private final ObjectMapper objectMapper;

    public PaymentEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PaymentEvent fromJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            String deliveryId = node.get("deliveryId").asText();
            String typeRaw = node.get("type").asText();

            PaymentEvent.PaymentEventType type =
                    switch (typeRaw) {
                        case "PaymentSucceededEvent" -> PaymentEvent.PaymentEventType.PAYMENT_SUCCEEDED;
                        case "PaymentFailedEvent" -> PaymentEvent.PaymentEventType.PAYMENT_FAILED;
                        case "RefundSucceededEvent" -> PaymentEvent.PaymentEventType.REFUND_SUCCEEDED;
                        case "RefundFailedEvent" -> PaymentEvent.PaymentEventType.REFUND_FAILED;
                        case "PaymentInitiatedEvent" -> null;
                        default -> throw new IllegalArgumentException("Unknown payment event type: " + typeRaw);
                    };

            return new PaymentEvent(deliveryId, type);

        } catch (Exception e) {
            throw new RuntimeException("Failed to map PaymentEvent", e);
        }
    }
}

