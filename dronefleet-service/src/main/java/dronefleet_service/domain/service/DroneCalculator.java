package dronefleet_service.domain.service;

import dronefleet_service.domain.model.Drone;
import dronefleet_service.domain.model.Location;

import java.util.Comparator;

public class DroneCalculator {

    private final double batteryPercentPerKm;

    public DroneCalculator(double batteryPercentPerKm) {
        this.batteryPercentPerKm = batteryPercentPerKm;
    }

    public boolean canPerformMission(Drone drone, Location pickup, Location dropoff, Location baseLocation) {
        double d1 = drone.location().distanceTo(pickup);
        double d2 = pickup.distanceTo(dropoff);
        double d3 = dropoff.distanceTo(baseLocation);
        double totalKm = d1 + d2 + d3;
        double requiredPercent = Math.ceil(totalKm * batteryPercentPerKm);
        return drone.battery().percent() >= requiredPercent;
    }

    public Comparator<Drone> bestDroneComparator(Location pickup) {
        return Comparator
                .comparingDouble((Drone d) -> d.location().distanceTo(pickup))
                .thenComparing((Drone d) -> -d.battery().percent());
    }

    /** percent per km getter */
    public double percentPerKm() { return batteryPercentPerKm; }
}
