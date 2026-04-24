package delivery_service.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import delivery_service.application.exception.PaymentFailedException;
import delivery_service.domain.model.DeliveryId;
import delivery_service.domain.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Tag("integration")
class PaymentAdapterIntegrationTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private PaymentAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        adapter = new PaymentAdapter(kafkaTemplate, objectMapper, "payment-commands");
    }

    @Test
    void requestPaymentShouldPublishExpectedCommandToKafka() throws Exception {
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        assertDoesNotThrow(() ->
                adapter.requestPayment("user-1", DeliveryId.of("order-0001"), new Price(12.5, "EUR"))
        );

        verify(kafkaTemplate).send(org.mockito.Mockito.eq("payment-commands"), org.mockito.Mockito.eq("order-0001"), payloadCaptor.capture());
        verify(kafkaTemplate).flush();

        JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
        assertEquals("REQUEST_PAYMENT", payload.get("type").asText());
        assertEquals("user-1", payload.get("userId").asText());
        assertEquals("order-0001", payload.get("deliveryId").asText());
        assertEquals(12.5, payload.get("amount").asDouble());
        assertEquals("EUR", payload.get("currency").asText());
    }

    @Test
    void requestPaymentShouldTranslateKafkaErrorToDomainException() {
        doThrow(new RuntimeException("kafka down"))
                .when(kafkaTemplate)
                .send(anyString(), anyString(), anyString());

        assertThrows(PaymentFailedException.class, () ->
                adapter.requestPayment("user-2", DeliveryId.of("order-0002"), new Price(9.99, "EUR"))
        );
    }
}
