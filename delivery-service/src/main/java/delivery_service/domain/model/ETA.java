package delivery_service.domain.model;

import common.ddd.ValueObject;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class ETA implements ValueObject {

    private final Instant arrivalTime;

    public ETA(Instant arrivalTime) {
        if (arrivalTime == null) {
            throw new IllegalArgumentException("ETA arrivalTime cannot be null");
        }
        this.arrivalTime = arrivalTime;
    }

    public Instant getArrivalTime() {
        return arrivalTime;
    }

    public boolean isBefore(ETA other) {
        if (other == null) return false;
        return this.arrivalTime.isBefore(other.arrivalTime);
    }

    public boolean isAfter(ETA other) {
        if (other == null) return false;
        return this.arrivalTime.isAfter(other.arrivalTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ETA eta)) return false;
        return arrivalTime.equals(eta.arrivalTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arrivalTime);
    }

    @Override
    public String toString() {
        return "ETA{" + "arrivalTime=" + arrivalTime + '}';
    }

    public Duration remainingFromNow() {
        return Duration.between(Instant.now(), arrivalTime);
    }
}
