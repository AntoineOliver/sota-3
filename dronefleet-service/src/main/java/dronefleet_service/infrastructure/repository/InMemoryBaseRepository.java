package dronefleet_service.infrastructure.repository;

import common.hexagonal.Adapter;
import dronefleet_service.application.port.BaseRepository;
import dronefleet_service.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Adapter
@Component
public class InMemoryBaseRepository implements BaseRepository {

    private final Map<BaseId, Base> basesById = new ConcurrentHashMap<>();
    private final Map<Location, Base> basesByLoc = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(InMemoryBaseRepository.class);


    @Override
    public boolean isPresent(BaseId baseId) {
        log.info("[BASE][REPO] is base = {} present", baseId);
        return basesById.containsKey(baseId);
    }

    @Override
    public boolean isPresentByLoc(Location location) {
        log.info("[BASE][REPO] is base in lat ={} and lon = {} is present", location.lat(),location.lon());
        return basesByLoc.containsKey(location);
    }

    @Override
    public Optional<Base> findById(BaseId baseId) {
        log.info("[BASE][REPO] find base id = {} present", baseId);
        return Optional.ofNullable(basesById.get(baseId));
    }

    @Override
    public Optional<Base> findClosestBase(Location location) {
        log.info("[BASE][REPO] find clothest base from {} and {}", location.lat(), location.lon());
        if (basesByLoc.isEmpty()) {
            log.info("[BASE][REPO] NO BASE REGISTERED");
            return Optional.empty();
        }

        return basesByLoc.values().stream()
                .min(Comparator.comparingDouble(
                        base -> base.location().distanceTo(location)
                ));
    }

    @Override
    public void save(Base base) {
        log.info("[BASE][REPO] save base {}", base);
        basesById.put(base.id(), base);
        basesByLoc.put(base.location(), base);
    }
}
