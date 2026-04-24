package user_service.infrastructure.adapter;

import user_service.application.dto.*;
import user_service.application.port.DeliveryPort;
import user_service.application.service.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Adapter REST du côté User Service pour communiquer avec le Delivery Service.
 * Aucune classe du delivery-service n’est importée → uniquement nos DTOs locaux.
 */
@Component
public class DeliveryAdapter implements DeliveryPort {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private static final Logger log = LoggerFactory.getLogger(DeliveryAdapter.class);

    public DeliveryAdapter(RestTemplate restTemplate,
                           @Value("${services.delivery.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public String createDelivery(DeliveryRequestDTO request) throws Exception {
        try {
            log.info("[USER][DELIVERY] calling DELIVERY at {}", baseUrl);
            return restTemplate.postForObject(
                    baseUrl,
                    request,
                    String.class
            );
        } catch (RestClientException ex) {
            throw new Exception("Error calling Delivery Service (createDelivery)", ex);
        }
    }

    @Override
    public DeliveryStatusDTO getDeliveryStatus(String deliveryId) throws Exception {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/" + deliveryId + "/status",
                    DeliveryStatusDTO.class
            );
        } catch (RestClientException ex) {
            throw new Exception("Error calling Delivery Service (getDeliveryStatus)", ex);
        }
    }

    @Override
    public RemainingDurationDTO getRemainingDuration(String deliveryId) throws Exception {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/" + deliveryId + "/remaining-time",
                    RemainingDurationDTO.class
            );
        } catch (RestClientException ex) {
            throw new Exception("Error calling Delivery Service (getRemainingDuration)", ex);
        }
    }
}
