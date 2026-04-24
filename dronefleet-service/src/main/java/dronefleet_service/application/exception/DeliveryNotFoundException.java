package dronefleet_service.application.exception;

public class DeliveryNotFoundException extends RuntimeException {
    public DeliveryNotFoundException(String message) { super(message); }
}
