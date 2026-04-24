package delivery_service.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import delivery_service.application.port.*;
import delivery_service.application.saga.DeliverySagaOrchestrator;
import delivery_service.application.service.DeliveryServiceImpl;
import delivery_service.domain.service.DeliveryCalculator;
import delivery_service.infrastructure.adapter.DroneFleetAdapter;
import delivery_service.infrastructure.adapter.PaymentAdapter;
import delivery_service.infrastructure.event.DomainEventPublisherImpl;
import delivery_service.infrastructure.repository.InMemoryDeliveryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"common", "delivery_service"})
public class DeliveryServiceMain {

    @Value("${services.dronefleet.base-url}")
    private String droneFleetBaseUrl;

    @Value("${topics.payment.commands}")
    private String paymentCommandsTopic;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DeliveryServiceMain.class);
        app.run(args);
    }

    // -------------------------------------------------------------------------
    // REST TEMPLATE
    // -------------------------------------------------------------------------
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // -------------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------------
    @Bean
    public DeliveryRepository deliveryRepository() {
        return new InMemoryDeliveryRepository();
    }

    // -------------------------------------------------------------------------
    // DOMAIN SERVICE
    // -------------------------------------------------------------------------
    @Bean
    public DeliveryCalculator deliveryCalculator() {
        return new DeliveryCalculator();
    }

    // -------------------------------------------------------------------------
    // ADAPTERS : Payment + DroneFleet
    // -------------------------------------------------------------------------
    @Bean
    public PaymentPort paymentPort(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        return new PaymentAdapter(kafkaTemplate, objectMapper, paymentCommandsTopic);
    }

    @Bean
    public DroneFleetPort droneFleetPort(RestTemplate rest) {
        return new DroneFleetAdapter(rest, droneFleetBaseUrl);
    }

    // -------------------------------------------------------------------------
    // DOMAIN EVENT PUBLISHER (Kafka)
    // -------------------------------------------------------------------------
    @Bean
    public DomainEventPublisher domainEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                                     ObjectMapper objectMapper) {
        return new DomainEventPublisherImpl(kafkaTemplate, objectMapper, "delivery-events");
    }

    // -------------------------------------------------------------------------
    // APPLICATION SERVICE
    // -------------------------------------------------------------------------
    @Bean
    public DeliveryService deliveryService(
            DeliveryRepository repository,
            DeliveryCalculator calculator,
            PaymentPort paymentPort,
            DroneFleetPort droneFleetPort,
            DomainEventPublisher domainEventPublisher,
            DeliverySagaOrchestrator sagaOrchestrator
    ) {
        return new DeliveryServiceImpl(
                repository,
                calculator,
                paymentPort,
                droneFleetPort,
                domainEventPublisher,
                sagaOrchestrator
        );
    }
}
