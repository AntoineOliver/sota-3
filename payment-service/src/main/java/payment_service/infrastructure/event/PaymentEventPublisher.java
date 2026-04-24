package payment_service.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import payment_service.application.port.EventPublisherPort;
import payment_service.application.service.PaymentServiceImpl;
import payment_service.domain.event.PaymentEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class PaymentEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String,String> kafka;
    private final ObjectMapper mapper;
    private final String topic;

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    @PostConstruct
    public void init() {
        log.info("[KAFKA][PAYMENT] KafkaTemplate class = {}", kafka.getClass().getName());
    }

    public PaymentEventPublisher(KafkaTemplate<String,String> kafka,ObjectMapper mapper, String topic) {
        this.kafka = kafka;
        this.mapper = mapper;
        this.topic = topic;
    }

    @Override
    public void publishEvent(PaymentEvent event) throws JsonProcessingException {
        try {
            ObjectNode node = mapper.valueToTree(event);
            node.put("type", event.type());

            String json = mapper.writeValueAsString(node);

            log.info("[KAFKA][PAYMENT] sending event topic={} payload={}", topic, json);

            kafka.send(topic, json);
            kafka.flush();

        } catch (Exception e) {
            log.error("[KAFKA][PAYMENT] exception during publish", e);
            throw e;
        }
    }




}
