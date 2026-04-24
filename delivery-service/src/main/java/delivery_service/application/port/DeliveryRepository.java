package delivery_service.application.port;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import delivery_service.domain.model.*;

import java.util.List;
import java.util.Optional;

@OutBoundPort
public interface DeliveryRepository extends Repository{

    void updateDelivery(Delivery delivery);

    void saveDelivery(Delivery delivery);

    void deleteDelivery(Delivery delivery);

    Optional<Delivery> findById(DeliveryId id);

    List<Delivery> findByUserId(String userId);

    List<Delivery> findByDroneId(String droneId);

    String nextId();

    boolean existsById(DeliveryId id);

    List<Delivery> findAllByStatus(DeliveryStatus deliveryStatus);
}
