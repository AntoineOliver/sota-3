package payment_service.application.exception;

public class PaymentNotFoundException extends Exception {

    public PaymentNotFoundException(String paymentId) {
        super("Payment not found: " + paymentId);
    }

    public PaymentNotFoundException(String paymentId, Throwable cause) {
        super("Payment not found: " + paymentId, cause);
    }
}
