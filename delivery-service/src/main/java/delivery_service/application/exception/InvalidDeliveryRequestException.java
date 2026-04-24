package delivery_service.application.exception;

public class InvalidDeliveryRequestException extends Exception {

    public InvalidDeliveryRequestException(String message) {
        super(message);
    }
}
