package dronefleet_service.domain.model;

import java.util.Objects;

public record Location(double lat, double lon) {


    public double distanceTo(Location other) {
        // Haversine would be more accurate; here we keep a simple approximation in km
        double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(other.lat - this.lat);
        double dLon = Math.toRadians(other.lon - this.lon);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(other.lat))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public Location moveTowards(Location target, double distanceKm) {
        // Move from this location straight toward target by distanceKm (approximate on sphere).
        // For small distances, linear interpolation on lat/lon is acceptable.
        double total = this.distanceTo(target);
        if (total <= 0.000001 || distanceKm >= total) return target;
        double ratio = distanceKm / total;
        double newLat = this.lat + (target.lat - this.lat) * ratio;
        double newLon = this.lon + (target.lon - this.lon) * ratio;
        return new Location(newLat, newLon);
    }

    public double lat() { return lat; }
    public double lon() { return lon; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location that = (Location) o;
        return Double.compare(that.lat, lat) == 0
                && Double.compare(that.lon, lon) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }
}
