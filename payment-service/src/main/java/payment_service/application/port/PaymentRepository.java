package payment_service.application.port;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import payment_service.domain.event.PaymentEvent;
import payment_service.domain.model.Payment;
import payment_service.domain.model.PaymentId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
@OutBoundPort
@Component
public interface PaymentRepository extends Repository {

    void append(PaymentEvent event);

    Optional<Payment> findById(PaymentId paymentId);

    List<PaymentEvent> findEventsById(PaymentId paymentId);

    boolean isPresent(PaymentId paymentId);
}
