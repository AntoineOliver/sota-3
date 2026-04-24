package delivery_service.application.saga;

import delivery_service.infrastructure.saga.InMemoryDeliverySagaRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class DeliverySagaOrchestratorUnitTest {

    private final DeliverySagaRepository repository = new InMemoryDeliverySagaRepository();
    private final DeliverySagaOrchestrator orchestrator = new DeliverySagaOrchestrator(repository);

    @Test
    void paymentRequestedShouldAutoCreateSagaWhenMissing() {
        DeliverySaga saga = orchestrator.paymentRequested("order-0001");

        assertEquals("order-0001", saga.deliveryId());
        assertEquals(DeliverySagaState.PAYMENT_REQUESTED, saga.state());
        assertEquals("Payment requested", saga.lastMessage());
    }

    @Test
    void completedShouldPersistLatestState() {
        orchestrator.start("order-0002");
        orchestrator.paymentRequested("order-0002");
        orchestrator.paymentConfirmed("order-0002");
        orchestrator.droneAssignmentRequested("order-0002");
        orchestrator.waitingForPickup("order-0002");
        orchestrator.inProgress("order-0002");

        DeliverySaga saga = orchestrator.completed("order-0002");

        assertEquals(DeliverySagaState.COMPLETED, saga.state());
        assertEquals(DeliverySagaState.COMPLETED, orchestrator.getSaga("order-0002").state());
        assertEquals("Delivery completed", orchestrator.getSaga("order-0002").lastMessage());
    }
}
