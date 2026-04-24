package payment_service.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import payment_service.application.dto.PaymentResponseDTO;
import payment_service.application.port.PaymentService;
import payment_service.domain.model.Amount;
import payment_service.domain.model.PaymentId;
import payment_service.infrastructure.metrics.PaymentMetrics;

@Component
public class PaymentCommandListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentCommandListener.class);

    private final PaymentService paymentService;
    private final PaymentMetrics paymentMetrics;

    public PaymentCommandListener(PaymentService paymentService, PaymentMetrics paymentMetrics) {
        this.paymentService = paymentService;
        this.paymentMetrics = paymentMetrics;
    }

    @KafkaListener(topics = "${topics.payment.commands}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCommand(PaymentCommand command) {
        paymentMetrics.commandReceived(command.type());
        log.info("[KAFKA][PAYMENT][COMMAND] received type={} deliveryId={}", command.type(), command.deliveryId());

        try {
            switch (command.type()) {
                case REQUEST_PAYMENT -> handlePaymentRequest(command);
                case REQUEST_REFUND -> handleRefundRequest(command);
            }
        } catch (Exception exception) {
            paymentMetrics.commandRejected(command.type());
            log.error("[KAFKA][PAYMENT][COMMAND] failed type={} deliveryId={}",
                    command.type(), command.deliveryId(), exception);
        }
    }

    private void handlePaymentRequest(PaymentCommand command) throws Exception {
        PaymentResponseDTO response = paymentService.onPaymentRequested(
                command.deliveryId(),
                command.userId(),
                new Amount(command.amount(), command.currency())
        );

        PaymentId paymentId = new PaymentId(response.paymentId());
        paymentMetrics.commandStarted(command.type(), paymentId.toString(), command.occurredAt());
        paymentService.startPayment(paymentId);
    }

    private void handleRefundRequest(PaymentCommand command) throws Exception {
        PaymentId paymentId = PaymentId.from(command.userId(), command.deliveryId());
        paymentMetrics.commandStarted(command.type(), paymentId.toString(), command.occurredAt());
        paymentService.onRefundRequested(paymentId);
    }
}
