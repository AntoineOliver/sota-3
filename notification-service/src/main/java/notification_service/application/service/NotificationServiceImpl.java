package notification_service.application.service;

import notification_service.application.port.EmailSenderPort;
import notification_service.application.port.NotificationService;
import notification_service.domain.NotificationMessage;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final EmailSenderPort emailSender;

    public NotificationServiceImpl(EmailSenderPort emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void notify(NotificationMessage message) {
        emailSender.sendEmail(message);
    }
}
