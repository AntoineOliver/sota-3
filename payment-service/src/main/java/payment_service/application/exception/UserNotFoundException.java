package payment_service.application.exception;

public class UserNotFoundException extends Exception {

    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }

    public UserNotFoundException(String userId, Throwable cause) {
        super("User not found: " + userId, cause);
    }
}
