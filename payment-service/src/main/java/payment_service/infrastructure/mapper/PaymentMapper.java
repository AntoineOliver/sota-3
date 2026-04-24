package payment_service.infrastructure.mapper;

import payment_service.application.dto.PaymentResponseDTO;
import payment_service.domain.model.Payment;

public class PaymentMapper {

    public static PaymentResponseDTO toResponseDto(Payment p) {
        return new PaymentResponseDTO(
                p.getPaymentId().toString(),
                p.getUserId(),
                p.getDeliveryId(),
                p.getAmount().amount().doubleValue(),
                p.getAmount().currency().toString(),
                p.getWhenCreated(),
                p.getState().name()
        );
    }
}
