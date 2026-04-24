package delivery_service.application.exception;
import delivery_service.domain.model.DeliveryId;

public class DroneAssignmentFailedException extends Exception {

    private final DeliveryId deliveryId;

    public DroneAssignmentFailedException(DeliveryId deliveryId) {
        super("No drone assigned to delivery: " + deliveryId);
        this.deliveryId = deliveryId;
    }

    public DeliveryId getDeliveryId() {
        return deliveryId;
    }
}
