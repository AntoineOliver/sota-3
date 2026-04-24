package notification_service.infrastructure.event;

import notification_service.application.port.NotificationService;
import notification_service.infrastructure.mapper.UserMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserListener {

    private final UserMapper mapper;
    private final NotificationService notificationService;

    public UserListener(UserMapper mapper,
                             NotificationService notificationService) {
        this.mapper = mapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "user-events", groupId = "notification-service")
    public void onMessage(String json) {

        try {
            UserEvent event = mapper.fromJson(json);

            switch (event.type()) {

                case USER_REGISTERED -> notify(
                        event.userId(),
                        "Welcome!",
                        "Hello " + event.username() + ", your account is created"
                );

                case USER_EMAIL_UPDATED -> notify(
                        event.userId(),
                        "Email Updated",
                        "Your email has been updated"
                );

                case USER_DELETED -> notify(
                        event.userId(),
                        "Email Deleted",
                        "Your email has been deleted"
                );
            }

        } catch (Exception e) {
            System.out.println("[USER-LISTENER] error: " + e.getMessage());
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