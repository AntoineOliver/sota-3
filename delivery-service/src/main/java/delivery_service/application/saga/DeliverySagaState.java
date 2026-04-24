package delivery_service.application.saga;

public enum DeliverySagaState {
    CREATED,
    PAYMENT_REQUESTED,
    PAYMENT_CONFIRMED,
    DRONE_ASSIGNMENT_REQUESTED,
    WAITING_FOR_PICKUP,
    IN_PROGRESS,
    COMPLETED,
    COMPENSATING,
    CANCELED,
    FAILED
}
