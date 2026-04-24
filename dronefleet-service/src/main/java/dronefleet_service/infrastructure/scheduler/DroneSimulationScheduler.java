package dronefleet_service.infrastructure.scheduler;

import dronefleet_service.application.port.DroneTelemetryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler that triggers simulation steps regularly.
 *
 * Tweak fixedRate to control real time frequency.
 * We simulate 10 minutes per step by default.
 */
@Component
public class DroneSimulationScheduler {

    private final DroneTelemetryPort telemetry;
    //private static final Logger log = LoggerFactory.getLogger(DroneSimulationScheduler.class);

    public DroneSimulationScheduler(@Qualifier("droneTelemetryAdapter") DroneTelemetryPort telemetry) {
        this.telemetry = telemetry;
    }

    // every 10 seconds real time => simulate 10 minutes
    @Scheduled(fixedRate = 10_000)
    public void tick() {
        int minutesSimulated = 10;
        //log.info("[SIMULATION][TICK] simulate {} minutes", minutesSimulated);
        telemetry.simulateStep(minutesSimulated);
    }
}
