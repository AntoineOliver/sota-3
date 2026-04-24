package delivery_service.infrastructure.mapper;

import delivery_service.application.dto.*;
import delivery_service.application.exception.InvalidDeliveryRequestException;
import delivery_service.domain.model.*;

import java.time.Instant;

public class DeliveryMapper {

    public static DeliveryRequest toRequest(DeliveryRequestDTO dto) throws InvalidDeliveryRequestException {
        return new DeliveryRequest(
                dto.userId(),
                new Location(dto.pickupLat(), dto.pickupLng()),
                new Location(dto.dropoffLat(), dto.dropoffLon()),
                new Weight(dto.weight()),
                dto.requestedStart(),
                dto.requestedEnd()
        );
    }
}
