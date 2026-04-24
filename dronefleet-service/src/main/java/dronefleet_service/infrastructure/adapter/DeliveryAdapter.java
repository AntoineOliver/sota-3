package dronefleet_service.infrastructure.adapter;

import common.hexagonal.Adapter;
import dronefleet_service.application.port.DeliveryServicePort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Adapter
@Component
public class DeliveryAdapter implements DeliveryServicePort {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private static final Logger log = LoggerFactory.getLogger(DeliveryAdapter.class);


    public DeliveryAdapter(RestTemplate restTemplate,
                           @Value("${services.delivery.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    @CircuitBreaker(name = "deliveryService", fallbackMethod = "deliveryExistsFallback")
    public boolean deliveryExists(String deliveryId) {
        log.info("[DRONE][DELIVERY-PORT] checking delivery existence id={}", deliveryId);
        String url = baseUrl + "/" + deliveryId + "/exists";
        ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
        return Boolean.TRUE.equals(response.getBody());
    }

    @Override
    @CircuitBreaker(name = "deliveryService", fallbackMethod = "assignDroneToDeliveryFallback")
    public void assignDroneToDelivery(String deliveryId, String droneId) {
        String url = baseUrl + "/" + deliveryId + "/assignDrone";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        AssignDroneRequest request = new AssignDroneRequest(droneId);
        HttpEntity<AssignDroneRequest> entity = new HttpEntity<>(request, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    private boolean deliveryExistsFallback(String deliveryId, Throwable throwable) {
        log.warn("[DRONE][CB][DELIVERY] delivery existence check blocked/open id={} cause={}",
                deliveryId, throwable.getClass().getSimpleName());
        return false;
    }

    private void assignDroneToDeliveryFallback(String deliveryId, String droneId, Throwable throwable) {
        log.warn("[DRONE][CB][DELIVERY] assign-drone callback blocked/open deliveryId={} droneId={} cause={}",
                deliveryId, droneId, throwable.getClass().getSimpleName());
        throw new IllegalStateException("Delivery service circuit breaker is open", throwable);
    }

    // -------------------------------------------------
    // DTO internes
    // -------------------------------------------------

    private record AssignDroneRequest(String droneId) {}
}
