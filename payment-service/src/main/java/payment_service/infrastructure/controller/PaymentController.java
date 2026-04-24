package payment_service.infrastructure.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import payment_service.application.dto.*;
import payment_service.application.exception.*;
import payment_service.application.port.*;
import payment_service.domain.event.PaymentEvent;
import payment_service.domain.model.*;
import payment_service.infrastructure.mapper.PaymentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService service;
    private final PaymentRepository repository;
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    public PaymentController(PaymentService service, PaymentRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping("/payment")
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO request) {
        try {
            PaymentResponseDTO p = service.onPaymentRequested(
                    request.deliveryId(),
                    request.userId(),
                    request.toDomainAmount()
            );
            log.info("[PAYMENT][CREATE] Created payment with id={}", p.paymentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(p);
        } catch (UserNotFoundException | DeliveryNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{paymentId}/start")
    public ResponseEntity<Map<String, String>> startPayment(@PathVariable String paymentId) {
        log.info("[PAYMENT][START] Starting payment with id={}", paymentId);
        try {
            service.startPayment(new PaymentId(paymentId));
            log.info("[PAYMENT][START] payment started with id={}", paymentId);
            return ResponseEntity.ok(
                    Map.of(
                            "paymentId", paymentId,
                            "status", "STARTED"
                    )
            );
        } catch (PaymentNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable String paymentId) {
        log.info("[PAYMENT][CANCEL] canceling payment with id={}", paymentId);
        try {
            service.cancelPayment(new PaymentId(paymentId));
            log.info("[PAYMENT][CANCEL] payment canceled with id={}", paymentId);
            return ResponseEntity.noContent().build();
        } catch (PaymentNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{paymentId}/succeeded")
    public ResponseEntity<Void> paymentSucceeded(@PathVariable String paymentId) {
        log.info("[PAYMENT][SUCCEEDED] succeeded callback received with id={}", paymentId);
        try {
            service.onPaymentSucceeded(new PaymentId(paymentId));
            log.info("[PAYMENT][SUCCEEDED] payment confirmed with id={}", paymentId);
            return ResponseEntity.noContent().build();
        } catch (PaymentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{paymentId}/failed")
    public ResponseEntity<Void> paymentFailed(@PathVariable String paymentId) {
        log.info("[PAYMENT][FAILED] failing payment with id={}", paymentId);
        try {
            service.onPaymentFailed(new PaymentId(paymentId));
            log.info("[PAYMENT][FAILED] payment failed with id={}", paymentId);
            return ResponseEntity.noContent().build();
        } catch (PaymentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment.PaymentState> getPaymentState(@PathVariable String paymentId) {
        log.info("[PAYMENT][STATE] getting payment state with id={}", paymentId);
        try {
            Payment.PaymentState state = service.getPaymentState(new PaymentId(paymentId));
            log.info("[PAYMENT][STATE] payment status got id={}", paymentId);
            return ResponseEntity.ok(state);
        } catch (PaymentNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{paymentId}/events")
    public ResponseEntity<java.util.List<PaymentEvent>> getPaymentEvents(@PathVariable String paymentId) {
        PaymentId id = new PaymentId(paymentId);
        if (!repository.isPresent(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, paymentId);
        }
        return ResponseEntity.ok(repository.findEventsById(id));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Void> requestRefund(@PathVariable String paymentId) {
        log.info("[PAYMENT][REFUND REQUEST] requesting refund with id={}", paymentId);
        try {
            service.onRefundRequested(new PaymentId(paymentId));
            log.info("[PAYMENT][REFUND REQUEST] refund processing with id={}", paymentId);
            return ResponseEntity.noContent().build();
        } catch (PaymentNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{paymentId}/refund/succeeded")
    public ResponseEntity<Void> refundSucceeded(@PathVariable String paymentId) {
        log.info("[REFUND][SUCCEEDED] confirming refund with id={}", paymentId);
        try {
            service.onRefundSucceeded(new PaymentId(paymentId));
            log.info("[REFUND][SUCCEEDED] refund confirmed with id={}", paymentId);
            return ResponseEntity.noContent().build();
        } catch (PaymentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{paymentId}/refund/failed")
    public ResponseEntity<Void> refundFailed(@PathVariable String paymentId) {
        log.info("[REFUND][FAILED] failing refund with id={}", paymentId);
        try {
            service.onRefundFailed(new PaymentId(paymentId));
            log.info("[REFUND][FAILED] refund failed with id={}", paymentId);
            return ResponseEntity.noContent().build();
        } catch (PaymentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
