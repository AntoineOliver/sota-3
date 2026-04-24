package dronefleet_service.application.port;

import dronefleet_service.application.exception.BaseNotFoundException;
import dronefleet_service.application.exception.DroneNotFoundException;
import dronefleet_service.domain.model.*;

import java.util.Optional;

public interface DroneService {

    Base createBase(String name,Location baseLocation,int capacity);
    Drone createDrone(BaseId baseId);

    void onDroneRequested(String deliveryId,
                          Location pickup,
                          Location dropoff) throws Exception;


    void onDroneArrivedAtPickup(DroneId droneId, String deliveryId);

    void onDroneArrivedAtDropoff(DroneId droneId, String deliveryId);

    void onDroneArrivedAtBase(DroneId droneId);

    void onDroneMoved(DroneId droneId, double lat, double lon, double travelledKm);

    Base getBase(BaseId baseId) throws BaseNotFoundException;
    Drone getDrone(DroneId droneId) throws DroneNotFoundException;
    int getNumberOfDrones(BaseId baseId) throws BaseNotFoundException;
    Optional<DroneId> getAssignedDroneId(String deliveryId);
}
