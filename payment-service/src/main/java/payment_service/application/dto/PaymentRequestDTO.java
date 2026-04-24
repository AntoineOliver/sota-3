package payment_service.application.dto;

import payment_service.domain.model.Amount;

public record PaymentRequestDTO(
        String userId,
        String deliveryId,
        double amount,
        String currency
) {
    public Amount toDomainAmount() { return new Amount(amount, currency); }
}
