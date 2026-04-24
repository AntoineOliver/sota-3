package notification_service.infrastructure.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final String serviceName;
    private final HealthEndpoint healthEndpoint;

    public HealthController(@Value("${spring.application.name}") String serviceName,
                            HealthEndpoint healthEndpoint) {
        this.serviceName = serviceName;
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        HealthComponent health = healthEndpoint.health();
        return Map.of(
                "service", serviceName,
                "status", health.getStatus().getCode()
        );
    }
}
