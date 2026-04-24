package dronefleet_service.application.exception;

public class BaseAlreadyExistsException extends RuntimeException {
    public BaseAlreadyExistsException(String message) {
        super(message);
    }
}
