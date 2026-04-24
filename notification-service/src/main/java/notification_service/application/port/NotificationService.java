package notification_service.application.port;

import common.hexagonal.InBoundPort;
import notification_service.domain.NotificationMessage;

@InBoundPort
public interface NotificationService {
    void notify(NotificationMessage message);
}
