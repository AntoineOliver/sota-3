package dronefleet_service.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import dronefleet_service.application.port.*;
import dronefleet_service.application.service.DroneServiceImpl;
import dronefleet_service.domain.service.DroneCalculator;
import dronefleet_service.infrastructure.adapter.DeliveryAdapter;
import dronefleet_service.infrastructure.adapter.DroneTelemetryAdapter;
import dronefleet_service.infrastructure.event.DroneEventPublisher;
import dronefleet_service.infrastructure.repository.InMemoryBaseRepository;
import dronefleet_service.infrastructure.repository.InMemoryDroneRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"common", "dronefleet_service"})
@EnableScheduling
public class DroneServiceMain {

    @Value("${services.delivery.base-url}")
    private String deliveryBaseUrl;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DroneServiceMain.class);
        app.run(args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    // -------------------------------------------------------------------------
    // REPOSITORIES
    // -------------------------------------------------------------------------

    @Bean
    public DroneRepository droneRepository() {
        return new InMemoryDroneRepository();
    }

    @Bean
    public BaseRepository baseRepository() {
        return new InMemoryBaseRepository();
    }

    // -------------------------------------------------------------------------
    // ADAPTERS (REST → DeliveryService)
    // -------------------------------------------------------------------------

    @Bean
    public DeliveryServicePort deliveryServicePort(RestTemplate rest) {
        return new DeliveryAdapter(rest,deliveryBaseUrl);
    }

    // -------------------------------------------------------------------------
    // EVENT PUBLISHER (Kafka → "drone-events")
    // -------------------------------------------------------------------------

    @Bean
    public EventPublisherPort eventPublisherPort(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        return new DroneEventPublisher(kafka,mapper,"drone-events");
    }

    // -------------------------------------------------------------------------
    // DOMAIN SERVICE
    // -------------------------------------------------------------------------

    @Bean
    public DroneCalculator droneCalculator() {
        double batteryPercentPerKm = 1.2;
        return new DroneCalculator(batteryPercentPerKm);
    }

    // -------------------------------------------------------------------------
    // TELEMETRY SIMULATION
    // -------------------------------------------------------------------------

    @Bean
    public DroneTelemetryPort droneTelemetryPort(
            DroneRepository droneRepository,
            DroneService service
    ) {
        return new DroneTelemetryAdapter(droneRepository, service);
    }

    // -------------------------------------------------------------------------
    // APPLICATION SERVICE (DRONE SERVICE)
    // -------------------------------------------------------------------------

    @Bean
    public DroneService droneService(
            DroneRepository droneRepository,
            BaseRepository baseRepository,
            DeliveryServicePort deliveryServicePort,
            EventPublisherPort eventPublisherPort,
            DroneCalculator droneCalculator
    ) {
        double maintenanceThreshold = 200.0;

        return new DroneServiceImpl(
                droneRepository,
                baseRepository,
                deliveryServicePort,
                eventPublisherPort,
                droneCalculator,
                maintenanceThreshold
        );
    }
}
