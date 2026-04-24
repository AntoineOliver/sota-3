package delivery_service.application.saga;

import java.util.Optional;

public interface DeliverySagaRepository {
    DeliverySaga save(DeliverySaga saga);
    Optional<DeliverySaga> findByDeliveryId(String deliveryId);
}
