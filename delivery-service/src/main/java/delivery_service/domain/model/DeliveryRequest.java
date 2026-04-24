package delivery_service.domain.model;

import common.ddd.ValueObject;
import delivery_service.application.exception.InvalidDeliveryRequestException;
import delivery_service.application.exception.UserNotFoundException;

import java.time.Instant;
import java.util.Objects;

public final class DeliveryRequest implements ValueObject {

    private final String userId;
    private final Location pickupLocation;
    private final Location dropoffLocation;
    private final Weight weight;
    private final Instant requestedStart;
    private final Instant requestedEnd;

    public DeliveryRequest(
            String userId,
            Location pickupLocation,
            Location dropoffLocation,
            Weight weight,
            Instant requestedStart,
            Instant requestedEnd
    ) throws InvalidDeliveryRequestException {

        if (userId == null) {
            throw new InvalidDeliveryRequestException("user id is null");
        }
        if (pickupLocation == null || dropoffLocation == null) {
            throw new InvalidDeliveryRequestException("Locations cannot be null");
        }
        if (weight == null || weight.value() <= 0) {
            throw new InvalidDeliveryRequestException("Weight must be positive");
        }
        if (requestedStart == null || requestedEnd == null || requestedStart.isAfter(requestedEnd)) {
            throw new InvalidDeliveryRequestException("Invalid requested time window");
        }
        this.userId = userId;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.weight = weight;
        this.requestedStart = requestedStart;
        this.requestedEnd = requestedEnd;
    }

    public String userId() {return this.userId;}
    public Location pickupLocation() { return pickupLocation; }
    public Location dropoffLocation() { return dropoffLocation; }
    public Weight weight() { return weight; }
    public Instant requestedStart() { return requestedStart; }
    public Instant requestedEnd() { return requestedEnd; }


}
