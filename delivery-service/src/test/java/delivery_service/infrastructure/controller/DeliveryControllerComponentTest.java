package delivery_service.infrastructure.controller;

import delivery_service.application.port.DeliveryService;
import delivery_service.application.saga.DeliverySaga;
import delivery_service.application.saga.DeliverySagaOrchestrator;
import delivery_service.application.saga.DeliverySagaState;
import delivery_service.domain.model.DeliveryId;
import delivery_service.domain.model.DeliveryStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryController.class)
@Tag("component")
class DeliveryControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @MockBean
    private DeliverySagaOrchestrator sagaOrchestrator;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @WithMockUser
    void startShouldDelegateToServiceAndReturnOk() throws Exception {
        mockMvc.perform(post("/deliveries/{deliveryId}/start", "order-0001").with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(deliveryService).requestDeliveryStart(DeliveryId.of("order-0001"));
    }

    @Test
    @WithMockUser
    void sagaShouldExposeCurrentDistributedWorkflowState() throws Exception {
        Mockito.when(sagaOrchestrator.getSaga("order-0002"))
                .thenReturn(new DeliverySaga(
                        "order-0002",
                        DeliverySagaState.WAITING_FOR_PICKUP,
                        "Drone assigned, waiting for pickup",
                        Instant.parse("2026-04-21T12:00:00Z")
                ));

        mockMvc.perform(get("/deliveries/{deliveryId}/saga", "order-0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryId").value("order-0002"))
                .andExpect(jsonPath("$.state").value("WAITING_FOR_PICKUP"))
                .andExpect(jsonPath("$.lastMessage").value("Drone assigned, waiting for pickup"));
    }
}
