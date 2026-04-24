package payment_service.infrastructure.adapter;

import payment_service.application.port.PaymentProcessPort;
import payment_service.domain.model.*;
import payment_service.domain.event.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;

public class PaymentProcessorAdapter implements PaymentProcessPort {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorAdapter.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final Random rnd = new Random();
    private final double successRatePayment = 0.8;
    private final double successRateRefund = 0.9;

    public PaymentProcessorAdapter(
            RestTemplate restTemplate,
            @Value("${payment.callback.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public void processPayment(PaymentId paymentId, Amount amount, String userId, String deliveryId) {
        log.info("[PAYMENT][PROCESS] scheduling async payment");
        CompletableFuture.runAsync(() -> {
            try {
                log.info("[PAYMENT][PROCESS] async thread started");
                TimeUnit.SECONDS.sleep(1 + rnd.nextInt(3));

                boolean success = rnd.nextDouble() <= successRatePayment;
                log.info("[PAYMENT][PROCESS] success =s {}", success);
                if (success) {
                    log.info("[PAYMENT][PROCESS] payment succeeded externally, id={}", paymentId);
                    log.info("[PAYMENT][PROCESS] calling callback URL{} + /paymentId/succeeded", baseUrl);

                    restTemplate.postForLocation(
                            baseUrl + "/" + paymentId + "/succeeded",
                            null
                    );
                } else {
                    log.info("[PAYMENT][PROCESS] payment failed externally, id={}", paymentId);
                    log.info("[PAYMENT][PROCESS] calling callback URL{} + /paymentId/failed", baseUrl);
                    restTemplate.postForLocation(
                            baseUrl + "/" + paymentId + "/failed",
                            null
                    );
                }

            } catch (Exception e) {
                log.error("[PAYMENT][PROCESS] external payment error, id={}", paymentId, e);
            }
        });
    }

    @Override
    public void processRefund(PaymentId paymentId, Amount amount, String userId, String deliveryId) {

        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1 + rnd.nextInt(3));

                boolean success = rnd.nextDouble() <= successRateRefund;

                if (success) {
                    log.info("[REFUND][PROCESS] payment succeeded externally, id={}", paymentId);
                    restTemplate.postForLocation(
                            baseUrl + "/" + paymentId + "/refund/succeeded",
                            null
                    );
                } else {
                    log.info("[REFUND][PROCESS] payment failed externally, id={}", paymentId);
                    restTemplate.postForLocation(
                            baseUrl + "/" + paymentId + "/refund/failed",
                            null
                    );
                }

            } catch (Exception e) {
                log.error("[REFUND][PROCESS] external payment error, id={}", paymentId, e);
            }
        });
    }
}
