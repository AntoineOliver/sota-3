package payment_service.application.port;

import common.hexagonal.OutBoundPort;
import payment_service.domain.model.Amount;
import payment_service.domain.model.PaymentId;

@OutBoundPort
public interface PaymentProcessPort {


    void processPayment(
            PaymentId paymentId,
            Amount amount,
            String userId,
            String deliveryId
    );

    void processRefund(
            PaymentId paymentId,
            Amount amount,
            String userId,
            String deliveryId
    );
}
