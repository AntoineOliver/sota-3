package delivery_service.infrastructure.scheduler;

import delivery_service.application.port.DeliveryRepository;
import delivery_service.application.port.DeliveryService;
import delivery_service.domain.model.Delivery;
import delivery_service.domain.model.DeliveryStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class schedulerDelivery {

    private final DeliveryRepository repository;
    private final DeliveryService deliveryService;

    public schedulerDelivery(DeliveryRepository repository,
                                  DeliveryService deliveryService) {
        this.repository = repository;
        this.deliveryService = deliveryService;
    }

    @Scheduled(fixedRate = 10_000)
    public void retryAssignments() {

        List<Delivery> pending = repository.findAllByStatus(
                DeliveryStatus.DRONE_ASSIGNMENT_REQUESTED
        );

        for (Delivery delivery : pending) {
            try {
                deliveryService.retryDroneAssignment(delivery.getId());
            } catch (Exception e) {
                System.err.println("Retry failed for " + delivery.getId());
            }
        }
    }
}
