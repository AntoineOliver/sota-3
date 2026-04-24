package notification_service.infrastructure.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import notification_service.infrastructure.event.PaymentEvent;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    private final ObjectMapper objectMapper;

    public PaymentMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PaymentEvent fromJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            String deliveryId = node.get("deliveryId").asText();
            String userId = node.get("userId").asText();
            String typeRaw = node.get("type").asText();

            PaymentEvent.PaymentEventType type = switch (typeRaw) {
                case "PaymentSucceededEvent" -> PaymentEvent.PaymentEventType.PAYMENT_SUCCEEDED;
                case "PaymentFailedEvent" -> PaymentEvent.PaymentEventType.PAYMENT_FAILED;
                case "RefundSucceededEvent" -> PaymentEvent.PaymentEventType.REFUND_SUCCEEDED;
                case "RefundFailedEvent" -> PaymentEvent.PaymentEventType.REFUND_FAILED;
                default -> throw new IllegalArgumentException("Unknown payment event: " + typeRaw);
            };

            String amount = node.has("amount") ? node.get("amount").asText() : null;
            String currency = node.has("currency") ? node.get("currency").asText() : null;
            String reason = node.has("reason") ? node.get("reason").asText() : null;

            return new PaymentEvent(
                    deliveryId,
                    userId,
                    type,
                    amount,
                    currency,
                    reason
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to map PaymentEvent", e);
        }
    }
}