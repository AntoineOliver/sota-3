package delivery_service.application.port;

import common.hexagonal.OutBoundPort;
import delivery_service.application.exception.DroneAssignmentFailedException;
import delivery_service.domain.model.Delivery;

@OutBoundPort
public interface DroneFleetPort {

    String requestDrone(Delivery delivery) throws DroneAssignmentFailedException;
}
