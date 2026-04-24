package delivery_service.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import delivery_service.application.exception.PaymentFailedException;
import delivery_service.application.exception.RefundFailedException;
import delivery_service.application.port.PaymentPort;
import delivery_service.domain.model.DeliveryId;
import delivery_service.domain.model.Price;
import delivery_service.infrastructure.event.PaymentCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

public class PaymentAdapter implements PaymentPort {

    private static final Logger log = LoggerFactory.getLogger(PaymentAdapter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String paymentCommandsTopic;

    public PaymentAdapter(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            String paymentCommandsTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.paymentCommandsTopic = paymentCommandsTopic;
    }

    @Override
    public void requestPayment(String userId, DeliveryId deliveryId, Price amount) throws PaymentFailedException {
        publishCommand(new PaymentCommand(
                UUID.randomUUID().toString(),
                PaymentCommand.PaymentCommandType.REQUEST_PAYMENT,
                userId,
                deliveryId.id(),
                amount.amount().doubleValue(),
                amount.currency().toString(),
                Instant.now()
        ), PaymentFailedException::new);
    }

    @Override
    public void requestRefund(String userId, DeliveryId deliveryId, Price amount) throws RefundFailedException {
        publishCommand(new PaymentCommand(
                UUID.randomUUID().toString(),
                PaymentCommand.PaymentCommandType.REQUEST_REFUND,
                userId,
                deliveryId.id(),
                amount.amount().doubleValue(),
                amount.currency().toString(),
                Instant.now()
        ), RefundFailedException::new);
    }

    private <T extends Exception> void publishCommand(
            PaymentCommand command,
            ExceptionFactory<T> exceptionFactory
    ) throws T {
        try {
            String payload = objectMapper.writeValueAsString(command);
            kafkaTemplate.send(paymentCommandsTopic, command.deliveryId(), payload);
            kafkaTemplate.flush();
            log.info("[KAFKA][DELIVERY][PAYMENT-COMMAND] topic={} command={} deliveryId={}",
                    paymentCommandsTopic, command.type(), command.deliveryId());
        } catch (Exception exception) {
            log.error("[KAFKA][DELIVERY][PAYMENT-COMMAND] failed for deliveryId={}", command.deliveryId(), exception);
            throw exceptionFactory.create();
        }
    }

    @FunctionalInterface
    private interface ExceptionFactory<T extends Exception> {
        T create();
    }
}
