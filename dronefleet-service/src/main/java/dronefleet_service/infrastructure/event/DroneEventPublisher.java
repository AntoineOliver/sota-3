package dronefleet_service.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dronefleet_service.application.port.EventPublisherPort;
import dronefleet_service.domain.event.DroneEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class DroneEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String topic;

    private static final Logger log = LoggerFactory.getLogger(DroneEventPublisher.class);

    @PostConstruct
    public void init() {
        log.info("[KAFKA][DRONE] KafkaTemplate class = {}", kafka.getClass().getName());
    }

    public DroneEventPublisher(
            KafkaTemplate<String, String> kafka,
            ObjectMapper mapper,
            String topic
    ) {
        this.kafka = kafka;
        this.mapper = mapper;
        this.topic = topic;
    }

    @Override
    public void publish(DroneEvent event) {
        try {
            ObjectNode node = mapper.valueToTree(event);
            node.put("type", event.type());

            String json = mapper.writeValueAsString(node);

            log.info("[KAFKA][DRONE] sending event topic={} payload={}", topic, json);

            kafka.send(topic, json);
            kafka.flush();

        } catch (Exception e) {
            log.error("[KAFKA][DRONE] exception during publish", e);
            throw new RuntimeException("Failed to publish drone event", e);
        }
    }
}
