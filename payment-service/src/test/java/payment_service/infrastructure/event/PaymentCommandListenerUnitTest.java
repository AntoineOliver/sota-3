package payment_service.infrastructure.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import payment_service.application.dto.PaymentResponseDTO;
import payment_service.application.port.PaymentService;
import payment_service.domain.model.PaymentId;
import payment_service.infrastructure.metrics.PaymentMetrics;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@Tag("unit")
class PaymentCommandListenerUnitTest {

    private PaymentService paymentService;
    private PaymentMetrics paymentMetrics;
    private PaymentCommandListener listener;

    @BeforeEach
    void setUp() {
        paymentService = Mockito.mock(PaymentService.class);
        paymentMetrics = Mockito.mock(PaymentMetrics.class);
        listener = new PaymentCommandListener(paymentService, paymentMetrics);
    }

    @Test
    void shouldCreateAndStartPaymentWhenRequestPaymentCommandIsReceived() throws Exception {
        PaymentCommand command = new PaymentCommand(
                "cmd-1",
                PaymentCommand.PaymentCommandType.REQUEST_PAYMENT,
                "user-1",
                "delivery-1",
                42.0,
                "EUR",
                Instant.parse("2026-04-21T10:15:30Z")
        );

        Mockito.when(paymentService.onPaymentRequested(eq("delivery-1"), eq("user-1"), any()))
                .thenReturn(new PaymentResponseDTO(
                        "user-1_delivery-1",
                        "user-1",
                        "delivery-1",
                        42.0,
                        "EUR",
                        Instant.parse("2026-04-21T10:15:31Z"),
                        "PAYMENT_PENDING"
                ));

        listener.onCommand(command);

        Mockito.verify(paymentMetrics).commandReceived(PaymentCommand.PaymentCommandType.REQUEST_PAYMENT);
        Mockito.verify(paymentMetrics).commandStarted(
                PaymentCommand.PaymentCommandType.REQUEST_PAYMENT,
                "user-1_delivery-1",
                command.occurredAt()
        );
        Mockito.verify(paymentService).startPayment(new PaymentId("user-1_delivery-1"));
    }

    @Test
    void shouldRequestRefundWhenRefundCommandIsReceived() throws Exception {
        PaymentCommand command = new PaymentCommand(
                "cmd-2",
                PaymentCommand.PaymentCommandType.REQUEST_REFUND,
                "user-9",
                "delivery-9",
                18.0,
                "EUR",
                Instant.parse("2026-04-21T10:20:30Z")
        );

        listener.onCommand(command);

        Mockito.verify(paymentMetrics).commandReceived(PaymentCommand.PaymentCommandType.REQUEST_REFUND);
        Mockito.verify(paymentMetrics).commandStarted(
                PaymentCommand.PaymentCommandType.REQUEST_REFUND,
                "user-9_delivery-9",
                command.occurredAt()
        );
        Mockito.verify(paymentService).onRefundRequested(new PaymentId("user-9_delivery-9"));
    }
}
