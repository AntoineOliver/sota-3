package notification_service.infrastructure.event;


import notification_service.application.port.NotificationService;
import notification_service.domain.NotificationMessage;
import notification_service.infrastructure.mapper.DeliveryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DeliveryListener {

    private final DeliveryMapper mapper;
    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(DeliveryListener.class);


    public DeliveryListener(DeliveryMapper mapper,
                                 NotificationService notificationService) {
        this.mapper = mapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "delivery-events", groupId = "notification-service")
    public void onMessage(String json) {
        log.info("[DELIVERY RAW] {}", json);
        try {
            DeliveryEvent event = mapper.fromJson(json);

            switch (event.type()) {

                case DELIVERY_STARTED -> notify(
                        event.userId(),
                        "Delivery Started",
                        "Your delivery " + event.deliveryId() + " has started"
                );

                case DELIVERY_COMPLETED -> notify(
                        event.userId(),
                        "Delivery Completed",
                        "Your delivery " + event.deliveryId() + " is completed"
                );

                case DELIVERY_FAILED -> notify(
                        event.userId(),
                        "Delivery Failed",
                        "Delivery failed: " + event.reason()
                );

                case DELIVERY_CANCELED -> notify(
                        event.userId(),
                        "Delivery Canceled",
                        "Your delivery was canceled"
                );

                case ETA_UPDATED -> notify(
                        event.userId(),
                        "ETA Updated",
                        "New ETA: " + event.eta()
                                + " | Remaining: " + event.remainingMinutes() + " min"
                );

                case DELIVERY_CREATED -> notify(
                        event.userId(),
                        "Delivery Created",
                        "Your delivery " + event.deliveryId() + " has been created"
                );

                case WAIT_FOR_PICKUP -> notify(
                        event.userId(),
                        "Waiting for Pickup",
                        "Your delivery " + event.deliveryId() + " is waiting for pickup"
                );
            }

        } catch (Exception e) {
            System.out.println("[DELIVERY-LISTENER] error: " + e.getMessage());
        }
    }

    private void notify(String userId, String subject, String body) {
        notificationService.notify(
                new NotificationMessage(
                        userId,
                        subject,
                        body,
                        Instant.now()
                )
        );
    }
}