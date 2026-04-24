package delivery_service.application.port;

import common.hexagonal.OutBoundPort;
import delivery_service.application.exception.PaymentFailedException;
import delivery_service.application.exception.RefundFailedException;
import delivery_service.domain.model.DeliveryId;
import delivery_service.domain.model.Price;

@OutBoundPort
public interface PaymentPort {
    void requestPayment(String userId, DeliveryId deliveryId, Price amount) throws PaymentFailedException;

    void requestRefund(String userId, DeliveryId deliveryId, Price amount) throws RefundFailedException;
}
