package delivery_service.application.dto;

import java.time.Instant;

public record DeliveryRequestDTO(
        String userId,
        double pickupLocationLat,
        double pickupLocationLon,
        double dropoffLocationLat,
        double dropoffLocationLon,
        double weight,
        Instant requestedTimeStart,
        Instant requestedTimeEnd
) {
    public String userId() {return userId;}
    public double pickupLat() {return pickupLocationLat;}
    public double pickupLng() {return pickupLocationLon;}
    public double dropoffLat() {return dropoffLocationLat;}
    public double dropoffLon() {return dropoffLocationLon;}
    public double weight() {return weight;}
    public Instant requestedStart() {return requestedTimeStart;}
    public Instant requestedEnd() {return requestedTimeEnd;}

}
