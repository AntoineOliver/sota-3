package delivery_service.domain.model;

public enum DeliveryStatus {
    CREATED,
    PAYMENT_PENDING,
    PAYMENT_CONFIRMED,
    DRONE_ASSIGNMENT_REQUESTED,
    WAIT_FOR_PICKUP,
    IN_PROGRESS,
    COMPLETED,
    CANCELED,
    FAILED
}
