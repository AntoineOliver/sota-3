package delivery_service.domain.event;

import java.time.Instant;

public record EtaUpdatedEvent(
        String deliveryId,
        String userId,
        String newEta,
        long remainingMinutes,
        Instant occurredAt
) implements DeliveryEvent {

    public static EtaUpdatedEvent of(
            String deliveryId,
            String userId,
            String newEta,
            long remainingMinutes
    ) {
        return new EtaUpdatedEvent(
                deliveryId,
                userId,
                newEta,
                remainingMinutes,
                Instant.now()
        );
    }

    @Override
    public String type() {
        return "EtaUpdatedEvent";
    }
}