package notification_service.infrastructure.event;

import notification_service.application.port.NotificationService;
import notification_service.infrastructure.mapper.PaymentMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PaymentListener {

    private final PaymentMapper mapper;
    private final NotificationService notificationService;

    public PaymentListener(PaymentMapper mapper,
                                NotificationService notificationService) {
        this.mapper = mapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void onMessage(String json) {

        try {
            PaymentEvent event = mapper.fromJson(json);

            switch (event.type()) {

                case PAYMENT_SUCCEEDED -> notify(
                        event.userId(),
                        "Payment Success",
                        "Payment successful for delivery " + event.deliveryId()
                );

                case PAYMENT_FAILED -> notify(
                        event.userId(),
                        "Payment Failed",
                        "Payment failed: " + event.reason()
                );

                case REFUND_SUCCEEDED -> notify(
                        event.userId(),
                        "Refund Success",
                        "Refund processed"
                );

                case REFUND_FAILED -> notify(
                        event.userId(),
                        "Refund Failed",
                        "Refund failed: " + event.reason()
                );
            }

        } catch (Exception e) {
            System.out.println("[PAYMENT-LISTENER] error: " + e.getMessage());
        }
    }

    private void notify(String userId, String subject, String body) {
        notificationService.notify(
                new notification_service.domain.NotificationMessage(
                        userId,
                        subject,
                        body,
                        Instant.now()
                )
        );
    }
}