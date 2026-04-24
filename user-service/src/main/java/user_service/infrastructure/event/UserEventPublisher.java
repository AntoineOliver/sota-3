package user_service.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import user_service.application.port.UserEventPublisherPort;
import user_service.domain.event.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class UserEventPublisher implements UserEventPublisherPort {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String topic;

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    @PostConstruct
    public void init() {
        log.info("[KAFKA][USER] KafkaTemplate class = {}", kafka.getClass().getName());
    }

    public UserEventPublisher(KafkaTemplate<String, String> kafka,
                              ObjectMapper mapper,
                              String topic) {
        this.kafka = kafka;
        this.mapper = mapper;
        this.topic = topic;
    }

    @Override
    public void publish(Object event) {
        try {
            ObjectNode node = mapper.createObjectNode();

            node.put("type", event.getClass().getSimpleName());

            if (event instanceof UserRegisteredEvent e) {
                node.put("userId", e.userId());
                node.put("username", e.username());
                node.put("email", e.email());
            }

            else if (event instanceof UserEmailUpdatedEvent e) {
                node.put("userId", e.userId());
                node.put("oldEmail", e.oldEmail());
                node.put("newEmail", e.newEmail());
            }

            else if (event instanceof UserDeletedEvent e) {
                node.put("userId", e.userId());
            }

            String json = mapper.writeValueAsString(node);

            kafka.send(topic, json);
            kafka.flush();

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}