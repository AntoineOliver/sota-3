package payment_service.application.exception;

public class RefundFailedException extends Exception {

    public RefundFailedException(String paymentId) {
        super("Refund failed for payment: " + paymentId);
    }

    public RefundFailedException(String paymentId, Throwable cause) {
        super("Refund failed for payment: " + paymentId, cause);
    }
}
