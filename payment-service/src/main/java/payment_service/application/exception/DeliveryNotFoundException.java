package payment_service.application.exception;

public class DeliveryNotFoundException extends Exception {

    public DeliveryNotFoundException(String deliveryId) {
        super("Delivery not found: " + deliveryId);
    }

    public DeliveryNotFoundException(String deliveryId, Throwable cause) {
        super("Delivery not found: " + deliveryId, cause);
    }
}
