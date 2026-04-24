package dronefleet_service.application.exception;

public class DroneNotFoundException extends RuntimeException {
    public DroneNotFoundException(String message) { super(message); }
}
