package notification_service.infrastructure;

import notification_service.application.port.EmailSenderPort;
import notification_service.application.port.NotificationService;
import notification_service.application.service.NotificationServiceImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"common", "notification_service"})
public class NotificationServiceMain {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceMain.class, args);
    }

    // ========================= CORE BEANS =========================

    @Bean
    public EmailSenderPort emailSenderPort() {
        return new EmailSenderSimulator();
    }

    @Bean
    public NotificationService notificationService(EmailSenderPort emailSenderPort) {
        return new NotificationServiceImpl(emailSenderPort);
    }
}
