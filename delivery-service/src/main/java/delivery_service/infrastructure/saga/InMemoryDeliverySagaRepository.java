package delivery_service.infrastructure.saga;

import delivery_service.application.saga.DeliverySaga;
import delivery_service.application.saga.DeliverySagaRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryDeliverySagaRepository implements DeliverySagaRepository {

    private final Map<String, DeliverySaga> store = new ConcurrentHashMap<>();

    @Override
    public DeliverySaga save(DeliverySaga saga) {
        store.put(saga.deliveryId(), saga);
        return saga;
    }

    @Override
    public Optional<DeliverySaga> findByDeliveryId(String deliveryId) {
        return Optional.ofNullable(store.get(deliveryId));
    }
}
