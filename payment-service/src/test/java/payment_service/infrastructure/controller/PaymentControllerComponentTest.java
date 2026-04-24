package payment_service.infrastructure.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import payment_service.application.dto.PaymentResponseDTO;
import payment_service.application.port.PaymentRepository;
import payment_service.application.port.PaymentService;
import payment_service.domain.event.PaymentInitiatedEvent;
import payment_service.domain.model.Amount;
import payment_service.domain.model.PaymentId;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Tag("component")
class PaymentControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @WithMockUser
    void createPaymentShouldReturnCreatedResponse() throws Exception {
        PaymentResponseDTO response = new PaymentResponseDTO(
                "user-1_order-0001",
                "user-1",
                "order-0001",
                12.50,
                "EUR",
                Instant.parse("2026-04-21T10:15:30Z"),
                "PAYMENT_PENDING"
        );

        Mockito.when(paymentService.onPaymentRequested(eq("order-0001"), eq("user-1"), any(Amount.class)))
                .thenReturn(response);

        mockMvc.perform(post("/payments/payment")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": "user-1",
                                  "deliveryId": "order-0001",
                                  "amount": 12.5,
                                  "currency": "EUR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("user-1_order-0001"))
                .andExpect(jsonPath("$.state").value("PAYMENT_PENDING"));
    }

    @Test
    @WithMockUser
    void getPaymentEventsShouldReturnPublishedStream() throws Exception {
        PaymentId id = new PaymentId("user-2_order-0002");
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                "user-2_order-0002",
                "user-2",
                "order-0002",
                18.75,
                "EUR",
                Instant.parse("2026-04-21T11:00:00Z")
        );

        Mockito.when(paymentRepository.isPresent(id)).thenReturn(true);
        Mockito.when(paymentRepository.findEventsById(id)).thenReturn(List.of(event));

        mockMvc.perform(get("/payments/{paymentId}/events", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value("user-2_order-0002"))
                .andExpect(jsonPath("$[0].currency").value("EUR"));
    }
}
