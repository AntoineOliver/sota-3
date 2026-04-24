package dronefleet_service.application.exception;

public class DroneAlreadyExistsException  extends RuntimeException {
    public DroneAlreadyExistsException(String message) {
        super(message);
    }
}