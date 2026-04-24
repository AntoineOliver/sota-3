package delivery_service.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import delivery_service.application.port.DomainEventPublisher;
import delivery_service.domain.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class DomainEventPublisherImpl implements DomainEventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String topic;

    private static final Logger log =
            LoggerFactory.getLogger(DomainEventPublisherImpl.class);

    public DomainEventPublisherImpl(
            KafkaTemplate<String, String> kafka,
            ObjectMapper mapper,
            String topic
    ) {
        this.kafka = kafka;
        this.mapper = mapper;
        this.topic = topic;
    }

    @Override
    public void publishEvent(DeliveryEvent event) {
        try {
            ObjectNode node = mapper.valueToTree(event);

            node.put("type", event.type());

            if (event instanceof EtaUpdatedEvent e) {
                node.put("remainingMinutes", e.remainingMinutes());
                node.put("newEta", e.newEta());
            }

            String json = mapper.writeValueAsString(node);

            log.info("[KAFKA][DELIVERY] sending topic={} payload={}", topic, json);

            kafka.send(topic, json);
            kafka.flush();

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}