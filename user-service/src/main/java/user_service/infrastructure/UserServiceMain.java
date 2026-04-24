package user_service.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import user_service.application.port.DeliveryPort;
import user_service.application.port.UserEventPublisherPort;
import user_service.application.repository.UserRepository;
import user_service.application.service.UserService;
import user_service.application.service.UserServiceImpl;
import user_service.infrastructure.adapter.DeliveryAdapter;
import user_service.infrastructure.event.UserEventPublisher;
import user_service.infrastructure.repository.InMemoryUserRepository;
import user_service.infrastructure.controller.UserController;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"common", "user_service"})
public class UserServiceMain {

    @Value("${services.delivery.base-url}")
    private String deliveryBaseUrl;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UserServiceMain.class);
        app.run(args);
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // --- REPOSITORY ---
    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    // --- ADAPTER : DELIVERY → REST ---
    @Bean
    public DeliveryPort deliveryPort(RestTemplate rest) {
        return new DeliveryAdapter(rest, deliveryBaseUrl);
    }

    // --- ADAPTER : KAFKA EVENTS ---
    @Bean
    public UserEventPublisherPort userEventPublisher(KafkaTemplate<String, String> kafka,
                                                     ObjectMapper mapper) {
        return new UserEventPublisher(kafka, mapper, "user-events");
    }


    // --- SERVICE (business logic) ---
    @Bean
    public UserService userService(
            UserRepository userRepository,
            DeliveryPort deliveryPort,
            UserEventPublisherPort eventPublisher
    ) {
        return new UserServiceImpl(userRepository, deliveryPort, eventPublisher);
    }

    // --- CONTROLLER (REST API) ---
    @Bean
    public UserController userController(UserService userService) {
        return new UserController(userService);
    }
}
