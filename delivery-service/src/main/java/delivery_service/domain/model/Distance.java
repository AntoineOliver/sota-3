package delivery_service.domain.model;

import common.ddd.ValueObject;

public record Distance(double kilometers) implements ValueObject {

    public Distance {
        if (kilometers < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
    }

    public static Distance between(Location a, Location b) {
        double R = 6371; // Earth radius in km

        double lat1 = Math.toRadians(a.latitude());
        double lon1 = Math.toRadians(a.longitude());
        double lat2 = Math.toRadians(b.latitude());
        double lon2 = Math.toRadians(b.longitude());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double h = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dlon / 2) * Math.sin(dlon / 2);

        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));

        return new Distance(R * c);
    }
}
