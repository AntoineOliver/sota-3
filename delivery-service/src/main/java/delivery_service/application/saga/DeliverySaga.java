package delivery_service.application.saga;

import java.time.Instant;

public record DeliverySaga(
        String deliveryId,
        DeliverySagaState state,
        String lastMessage,
        Instant updatedAt
) {
    public DeliverySaga transitionTo(DeliverySagaState nextState, String message) {
        return new DeliverySaga(deliveryId, nextState, message, Instant.now());
    }

    public static DeliverySaga created(String deliveryId, String message) {
        return new DeliverySaga(deliveryId, DeliverySagaState.CREATED, message, Instant.now());
    }
}
