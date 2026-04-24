package notification_service.infrastructure;

import notification_service.application.port.EmailSenderPort;
import notification_service.domain.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSenderSimulator implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderSimulator.class);

    @Override
    public void sendEmail(NotificationMessage message) {

        String formattedMessage = """
                ----------------------------------------------------
                 EMAIL SIMULATED
                → To user: %s
                → Subject: %s
                → Body:
                %s
                → OccurredAt: %s
                ----------------------------------------------------
                """.formatted(
                message.userId(),
                message.subject(),
                message.body(),
                message.occurredAt()
        );

        log.info("[USER={}] \n{}", message.userId(), formattedMessage);
    }
}