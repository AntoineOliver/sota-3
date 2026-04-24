package user_service.application.port;

import common.hexagonal.OutBoundPort;
import user_service.application.dto.*;

@OutBoundPort
public interface DeliveryPort {

    String createDelivery(DeliveryRequestDTO request) throws Exception;

    DeliveryStatusDTO getDeliveryStatus(String deliveryId) throws Exception;

    RemainingDurationDTO getRemainingDuration(String deliveryId) throws Exception;
}
