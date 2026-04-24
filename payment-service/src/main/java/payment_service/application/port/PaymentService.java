package payment_service.application.port;

import com.fasterxml.jackson.core.JsonProcessingException;
import common.hexagonal.InBoundPort;
import payment_service.application.dto.PaymentResponseDTO;
import payment_service.application.exception.*;
import payment_service.domain.model.*;
import org.springframework.stereotype.Component;

@InBoundPort
@Component
public interface PaymentService {

    /**
     * Create a payment
     * @param deliveryId
     * @param userName
     * @param amount
     * @return
     * @throws DeliveryNotFoundException
     * @throws UserNotFoundException
     */
    PaymentResponseDTO onPaymentRequested(String deliveryId, String userName, Amount amount) throws DeliveryNotFoundException, UserNotFoundException, JsonProcessingException;

    /**
     * Appelle externe pour vraiment faire la payment
     * @param paymentId
     * @throws PaymentNotFoundException
     */
    void startPayment(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException;

    /**
     * Annule payment avant qu'il est lieu
     * @param paymentId
     * @throws PaymentNotFoundException
     */
    void cancelPayment(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException;

    /**
     * state -> completed et publish event
     * @param paymentId
     * @throws PaymentNotFoundException
     */
    void onPaymentSucceeded(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException;

    /**
     *
     * @param paymentId
     * @throws PaymentNotFoundException
     */
    void onPaymentFailed(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException;

    /**
     *
     * @param paymentId
     * @return
     * @throws PaymentNotFoundException
     */
    Payment onRefundRequested(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException;


    /**
     *
     * @param paymentId
     * @throws PaymentNotFoundException
     */
    void onRefundSucceeded(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException;

    /**
     *
     * @param paymentId
     * @throws PaymentNotFoundException
     */
    void onRefundFailed(PaymentId paymentId) throws PaymentNotFoundException, JsonProcessingException;

    /**
     *
     * @param paymentId
     * @return
     * @throws PaymentNotFoundException
     */
    Payment.PaymentState getPaymentState(PaymentId paymentId) throws PaymentNotFoundException;

}