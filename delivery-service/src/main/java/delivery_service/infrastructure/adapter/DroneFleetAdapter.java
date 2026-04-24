package delivery_service.infrastructure.adapter;

import delivery_service.application.exception.DroneAssignmentFailedException;
import delivery_service.application.dto.DroneAssignmentRequestDTO;
import delivery_service.application.port.DroneFleetPort;
import delivery_service.domain.model.Delivery;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DroneFleetAdapter implements DroneFleetPort {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private static final Logger log = LoggerFactory.getLogger(DroneFleetAdapter.class);

    public DroneFleetAdapter(RestTemplate restTemplate,
                             @Value("${services.dronefleet.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    @CircuitBreaker(name = "droneFleetService", fallbackMethod = "requestDroneFallback")
    public String requestDrone(Delivery delivery) throws DroneAssignmentFailedException {
        String url = baseUrl + "/assign";
        log.info("[DELIVERY][DRONE] requesting drone for deliveryId={}", delivery.getId().id());

        DroneAssignmentRequestDTO body = new DroneAssignmentRequestDTO(
                delivery.getId().id(),
                delivery.getRequest().pickupLocation().latitude(),
                delivery.getRequest().pickupLocation().longitude(),
                delivery.getRequest().dropoffLocation().latitude(),
                delivery.getRequest().dropoffLocation().longitude(),
                delivery.getRequest().weight().value(),
                delivery.getRequest().userId()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DroneAssignmentRequestDTO> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            log.info("[DELIVERY][DRONE] drone request successfully sent");
        } catch (Exception e) {
            log.error("[DELIVERY][DRONE] failed to send drone request", e);
            throw new DroneAssignmentFailedException(delivery.getId());
        }

        return null;
    }

    private String requestDroneFallback(Delivery delivery, Throwable throwable) throws DroneAssignmentFailedException {
        log.warn("[DELIVERY][CB][DRONE] drone assignment blocked/open for deliveryId={} cause={}",
                delivery.getId().id(), throwable.getClass().getSimpleName());
        throw new DroneAssignmentFailedException(delivery.getId());
    }

}
