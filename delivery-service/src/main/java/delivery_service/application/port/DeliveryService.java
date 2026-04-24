package delivery_service.application.port;

import common.hexagonal.InBoundPort;
import delivery_service.application.dto.*;
import delivery_service.application.exception.*;
import delivery_service.domain.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@InBoundPort
public interface DeliveryService {

    /**
     *
     * @param request
     * @return
     * @throws InvalidDeliveryRequestException
     */
    Delivery createDelivery(DeliveryRequestDTO request) throws InvalidDeliveryRequestException;

    /**
     *
     * @param deliveryId
     * @return
     * @throws InvalidDeliveryRequestException
     */
    Delivery requestDeliveryStart(DeliveryId deliveryId) throws PaymentFailedException, DeliveryNotFoundException;

    /**
     *
     * @param deliveryId
     * @throws DeliveryNotFoundException
     */
    void onPaymentAccepted(DeliveryId deliveryId) throws DeliveryNotFoundException, DroneAssignmentFailedException;

    // -------------------------------------------------------------------------
    // DRONE ASSIGNED
    // -------------------------------------------------------------------------
    @Transactional
    void retryDroneAssignment(DeliveryId id) throws DeliveryNotFoundException, DroneAssignmentFailedException;

    void onDroneAssigned(DeliveryId deliveryId, String droneId)
            throws DeliveryNotFoundException;

    /**
     *
     * @param deliveryId
     * @throws DeliveryNotFoundException
     * @throws DeliveryAlreadyCompletedException
     */
    void cancelDelivery(DeliveryId deliveryId) throws DeliveryNotFoundException, DeliveryAlreadyCompletedException, RefundFailedException, DeliveryAlreadyStartedException;

    void failDelivery(DeliveryId deliveryId, String reason) throws DeliveryNotFoundException, DeliveryAlreadyCompletedException, RefundFailedException;

    void onDroneAtPickup(DeliveryId deliveryId) throws DeliveryNotFoundException;

    void onDroneLocationUpdated(DeliveryId deliveryId, String droneId, double latitude, double longitude) throws DeliveryNotFoundException;

    void onDroneDelivered(DeliveryId deliveryId) throws DeliveryNotFoundException;

    DeliveryStatus getDeliveryStatus(DeliveryId deliveryId) throws DeliveryNotFoundException;

    RemainingDuration getTimeLeft(DeliveryId deliveryId) throws DeliveryNotFoundException;

    boolean deliveryExists(DeliveryId id);

    Location getPickupLocation(DeliveryId id) throws DeliveryNotFoundException;

    Location getDropoffLocation(DeliveryId id) throws DeliveryNotFoundException;


    Optional<Location> getDroneLocation(DeliveryId id) throws DeliveryNotFoundException;
}
