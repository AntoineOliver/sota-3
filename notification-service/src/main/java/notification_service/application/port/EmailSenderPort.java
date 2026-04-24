package notification_service.application.port;

import common.hexagonal.OutBoundPort;
import notification_service.domain.NotificationMessage;

@OutBoundPort
public interface EmailSenderPort {
    void sendEmail(NotificationMessage message);
}
