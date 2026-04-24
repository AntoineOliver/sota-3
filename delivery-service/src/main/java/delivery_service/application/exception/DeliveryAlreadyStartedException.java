package delivery_service.application.exception;
import delivery_service.domain.model.DeliveryId;

public class DeliveryAlreadyStartedException extends Exception {

  private final DeliveryId deliveryId;

  public DeliveryAlreadyStartedException(DeliveryId deliveryId) {
    super("Delivery already started: " + deliveryId);
    this.deliveryId = deliveryId;
  }

  public DeliveryId getDeliveryId() {
    return deliveryId;
  }
}