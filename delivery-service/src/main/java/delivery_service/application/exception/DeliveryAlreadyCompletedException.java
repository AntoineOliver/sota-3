package delivery_service.application.exception;
import delivery_service.domain.model.DeliveryId;

public class DeliveryAlreadyCompletedException extends Exception {

  private final DeliveryId deliveryId;

  public DeliveryAlreadyCompletedException(DeliveryId deliveryId) {
    super("Delivery already completed: " + deliveryId);
    this.deliveryId = deliveryId;
  }

  public DeliveryId getDeliveryId() {
    return deliveryId;
  }
}