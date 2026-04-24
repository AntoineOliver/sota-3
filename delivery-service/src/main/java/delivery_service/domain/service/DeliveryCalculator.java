package delivery_service.domain.service;

import common.ddd.DomainService;
import delivery_service.domain.model.*;

import java.time.*;
import java.util.Currency;
import java.util.Optional;

public class DeliveryCalculator implements DomainService {

    private static final double BASE_RATE_PER_KM = 1.2;
    private static final double WEIGHT_SURCHARGE_RATE = 0.05; // +5% per kg
    private static final int DRONE_AVG_SPEED_KMH = 15;


    public Price computePrice(Distance distance, Weight weight) {
        double base = distance.kilometers() * BASE_RATE_PER_KM;
        double surcharge = base * (weight.value() * WEIGHT_SURCHARGE_RATE);
        return new Price(base + surcharge, "EUR");
    }


    public static ETA computeETA(Distance distance) {

        if (distance.kilometers() == 0) {
            return new ETA(Instant.now());
        }

        double hours = distance.kilometers() / DRONE_AVG_SPEED_KMH;
        long minutes = (long) Math.ceil(hours * 60);
        if (distance.kilometers() > 0) {
            minutes = Math.max(1, minutes);
        }
        Instant etaInstant = Instant.now().plus(Duration.ofMinutes(minutes));
        return new ETA(etaInstant);
    }

    public static ETA computeETA_2(DeliveryStatus status, Location pickup, Location dropoff, Location droneLocation) {


        double totalMinutes = 0;

        // SEGMENT 1 : drone -> pickup (si pas encore pickup)
        if (status == DeliveryStatus.DRONE_ASSIGNMENT_REQUESTED
                || status == DeliveryStatus.PAYMENT_CONFIRMED || status == DeliveryStatus.WAIT_FOR_PICKUP) {

            Distance d1 = Distance.between(droneLocation, pickup);
            totalMinutes += toMinutes(d1);

            // SEGMENT 2 : pickup -> dropoff (toujours ajouté ici)
            Distance d2 = Distance.between(pickup, dropoff);
            totalMinutes += toMinutes(d2);

        } else {

            // APRES PICKUP : drone -> dropoff uniquement
            Distance d1 = Distance.between(droneLocation, dropoff);
            totalMinutes += toMinutes(d1);
        }

        Instant etaInstant = Instant.now().plus(Duration.ofMinutes((long) totalMinutes));
        return new ETA(etaInstant);
    }

    private static double toMinutes(Distance distance) {
        double hours = distance.kilometers() / DRONE_AVG_SPEED_KMH;
        return Math.ceil(hours * 60);
    }


    public Distance computeDistance(Location pickup, Location dropoff) {
        return Distance.between(pickup, dropoff);
    }

    public static ETA updateETA(Location currentLocation, Location dropoff) {
        Distance remaining = Distance.between(currentLocation, dropoff);
        return computeETA(remaining);
    }


    /**

     * @param eta       Heure d'arrivée estimée
     * @param timeStart Début de la plage demandée (Optional)
     * @param timeEnd   Fin de la plage demandée (Optional)
     * @return true si ETA est dans la plage demandée ou si la plage n'est pas définie
     */
    public boolean isFeasible(ETA eta, Optional<LocalDateTime> timeStart, Optional<LocalDateTime> timeEnd) {
        if (eta == null || eta.getArrivalTime() == null) {
            return false;
        }

        // Convertir l'Instant de ETA en LocalDateTime
        LocalDateTime arrival = LocalDateTime.ofInstant(eta.getArrivalTime(), ZoneId.systemDefault());

        // Vérifie que ETA est après le timeStart si présent
        if (timeStart.isPresent() && arrival.isBefore(timeStart.get())) {
            return false;
        }

        // Vérifie que ETA est avant le timeEnd si présent
        if (timeEnd.isPresent() && arrival.isAfter(timeEnd.get())) {
            return false;
        }

        return true;
    }

}
