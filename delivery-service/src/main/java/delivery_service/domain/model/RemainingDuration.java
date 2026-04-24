package delivery_service.domain.model;

import common.ddd.ValueObject;
import java.time.Duration;

public class RemainingDuration implements ValueObject {
    private final Duration remaining;

    public RemainingDuration(ETA eta) {
        if (eta == null) {
            this.remaining = Duration.ZERO;
            return;
        }

        Duration rawRemaining = eta.remainingFromNow();
        if (rawRemaining.isZero() || rawRemaining.isNegative()) {
            this.remaining = Duration.ZERO;
            return;
        }

        long totalSeconds = rawRemaining.getSeconds();
        if (rawRemaining.getNano() > 0) {
            totalSeconds++;
        }

        long roundedUpMinutes = Math.max(1, (totalSeconds + 59) / 60);
        this.remaining = Duration.ofMinutes(roundedUpMinutes);
    }

    public long toMinutes() {
        return remaining.toMinutes();
    }

    public Duration value() {
        return remaining;
    }
}
