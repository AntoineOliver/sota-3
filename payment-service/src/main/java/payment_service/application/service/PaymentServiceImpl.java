package payment_service.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import payment_service.application.dto.PaymentResponseDTO;
import payment_service.application.port.*;
import payment_service.application.exception.*;
import payment_service.domain.event.*;
import payment_service.domain.model.*;
import payment_service.infrastructure.metrics.PaymentMetrics;
import payment_service.infrastructure.mapper.PaymentMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository repository;
    private final EventPublisherPort eventPublisher;
    private final PaymentProcessPort paymentProcess;
    private final PaymentMetrics paymentMetrics;

    public PaymentServiceImpl(
            PaymentRepository repository,
            EventPublisherPort eventPublisher,
            PaymentProcessPort paymentProcess,
            PaymentMetrics paymentMetrics
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.paymentProcess = paymentProcess;
        this.paymentMetrics = paymentMetrics;
    }

    @PostConstruct
    public void init() {
        log.info("[PAYMENT] EventPublisher injected = {}", eventPublisher.getClass().getName());
    }

    @Override
    public PaymentResponseDTO onPaymentRequested(String deliveryId, String userId, Amount amount)
            throws DeliveryNotFoundException, UserNotFoundException, JsonProcessingException {

        log.info("[PAYMENT][ON REQUESTED] creating payment for delivery with id={} and user id={}", deliveryId,userId);

        PaymentId paymentId = PaymentId.from(userId, deliveryId);
        log.info("[PAYMENT][ON REQUESTED] payment Id created, id ={} for delivery with id={} and user id={}",paymentId, deliveryId,userId);
        Payment payment = Payment.create(paymentId, userId, deliveryId, amount);
        log.info("[PAYMENT][ON REQUESTED] payment id ={} for delivery with id={} and user id={}",paymentId, deliveryId,userId);
        PaymentInitiatedEvent ev = payment.markPaymentInitiated();
        log.info("[PAYMENT][ON REQUESTED] payment initiated + event created, id={}",paymentId);
        repository.append(ev);
        log.info("[PAYMENT][ON REQUESTED] event store updated for payment id = {}", paymentId);
        eventPublisher.publishEvent(ev);
        log.info("[PAYMENT][ON REQUESTED] payment created, id={}", paymentId);

        Payment persistedPayment = repository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment event stream missing after append for " + paymentId));
        PaymentResponseDTO dto = PaymentMapper.toResponseDto(persistedPayment);
        return dto;
    }

    @Override
    public void startPayment(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException {
        log.info("[PAYMENT][START] payment starting, id={}", paymentId);
        Payment p = getPayment(paymentId);
        log.info("[PAYMENT][START] payment found, id={}", paymentId);
        paymentProcess.processPayment(p.getPaymentId(), p.getAmount(), p.getUserId(), p.getDeliveryId());
        log.info("[PAYMENT][START] payment starting, id={}", paymentId);
    }

    @Override
    public void cancelPayment(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException {
        log.info("[PAYMENT][CANCEL] payment canceling, id={}", paymentId);
        Payment p = getPayment(paymentId);
        log.info("[PAYMENT][CANCEL] payment found, id={}", paymentId);
        PaymentFailedEvent ev = p.markPaymentFailed("cancelled");
        log.info("[PAYMENT][CANCEL] payment canceled, id={}", paymentId);
        repository.append(ev);
        log.info("[PAYMENT][CANCEL] event appended, id={}", paymentId);
        eventPublisher.publishEvent(ev);
        log.info("[PAYMENT][CANCEL] payment-fail-event published, id={}", paymentId);
    }

    @Override
    public void onPaymentSucceeded(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException {
        log.info("[PAYMENT][ON SUCCEEDED] confirming payment, id={}", paymentId);
        Payment p = getPayment(paymentId);
        log.info("[PAYMENT][ON SUCCEEDED] payment found, id={}", paymentId);
        PaymentSucceededEvent ev = p.markPaymentConfirmed();
        log.info("[PAYMENT][ON SUCCEEDED] payment confirmed + event created, id={}", paymentId);
        repository.append(ev);
        log.info("[PAYMENT][ON SUCCEEDED] event appended, id={}", paymentId);
        eventPublisher.publishEvent(ev);
        log.info("[PAYMENT][ON SUCCEEDED] event published, id={}", paymentId);
        paymentMetrics.commandCompleted(paymentId.toString(), "SUCCEEDED");
    }

    @Override
    public void onPaymentFailed(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException {
        log.info("[PAYMENT][ON FAILED] failing payment, id={}", paymentId);
        Payment p = getPayment(paymentId);
        log.info("[PAYMENT][ON FAILED] payment found, id={}", paymentId);
        PaymentFailedEvent ev = p.markPaymentFailed("external-failed");
        log.info("[PAYMENT][ON FAILED] payment failed + event created, id={}", paymentId);
        repository.append(ev);
        log.info("[PAYMENT][ON FAILED] event appended, id={}", paymentId);
        eventPublisher.publishEvent(ev);
        log.info("[PAYMENT][ON FAILED] event published, id={}", paymentId);
        paymentMetrics.commandCompleted(paymentId.toString(), "FAILED");
    }

    @Override
    public Payment onRefundRequested(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException {
        log.info("[PAYMENT][ON REFUND REQUESTED] refund starting, id={}", paymentId);
        Payment p = getPayment(paymentId);
        log.info("[PAYMENT][ON REFUND REQUESTED] payment found, id={}", paymentId);
        RefundIssuedEvent ev = p.markRefundIssued();
        log.info("[PAYMENT][ON REFUND REQUESTED] refund being issued + event created, id={}", paymentId);
        repository.append(ev);
        log.info("[PAYMENT][ON REFUND REQUESTED] event appended, id={}", paymentId);
        eventPublisher.publishEvent(ev);
        log.info("[PAYMENT][ON REFUND REQUESTED] event published, id={}", paymentId);
        paymentProcess.processRefund(p.getPaymentId(), p.getAmount(), p.getUserId(), p.getDeliveryId());
        log.info("[PAYMENT][ON REFUND REQUESTED] process refund, id={}", paymentId);
        return p;
    }


    @Override
    public void onRefundSucceeded(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException {
        log.info("[REFUND][ON SUCCEEDED] confirming refund, id={}", paymentId);
        Payment p = getPayment(paymentId);
        log.info("[REFUND][ON SUCCEEDED] refund found, id={}", paymentId);
        RefundSucceededEvent ev = p.markRefundSucceeded();
        log.info("[REFUND][ON SUCCEEDED] refund succeeded + event created, id={}", paymentId);
        repository.append(ev);
        log.info("[REFUND][ON SUCCEEDED] event appended, id={}", paymentId);
        eventPublisher.publishEvent(ev);
        log.info("[REFUND][ON SUCCEEDED] event published, id={}", paymentId);
        paymentMetrics.commandCompleted(paymentId.toString(), "SUCCEEDED");
    }

    @Override
    public void onRefundFailed(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException {
        log.info("[REFUND][ON FAILED] failing refund, id={}", paymentId);
        Payment p = getPayment(paymentId);
        log.info("[REFUND][ON FAILED] refund found, id={}", paymentId);
        RefundFailedEvent ev = p.markRefundFailed("external-refund-failed");
        log.info("[REFUND][ON FAILED] refund failed + event created, id={}", paymentId);
        repository.append(ev);
        log.info("[REFUND][ON FAILED] event appended, id={}", paymentId);
        eventPublisher.publishEvent(ev);
        log.info("[REFUND][ON FAILED] event published, id={}", paymentId);
        paymentMetrics.commandCompleted(paymentId.toString(), "FAILED");
    }

    @Override
    public Payment.PaymentState getPaymentState(PaymentId paymentId) throws PaymentNotFoundException {
        log.info("[PAYMENT][STATE] state of payment with id={}", paymentId);
        Payment p = getPayment(paymentId);
        log.info("[PAYMENT][STATE] state got, id={} and state={}", paymentId,p.getState());
        return p.getState();
    }

    public Payment getPayment(PaymentId paymentId) throws PaymentNotFoundException {
        return repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId.toString()));
    }
}
