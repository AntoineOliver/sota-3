package payment_service.application.dto;

import payment_service.domain.model.PaymentId;

public record PaymentIdDTO(String paymentId) {
    public PaymentId toDomain() {
        return new PaymentId(paymentId);
    }
}
