
package dronefleet_service.application.port;

public interface DeliveryServicePort {
    boolean deliveryExists(String deliveryId);
    void assignDroneToDelivery(String deliveryId, String droneId);
}
