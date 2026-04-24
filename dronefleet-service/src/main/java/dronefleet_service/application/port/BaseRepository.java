// file: src/main/java/distributed_sota/dronefleet_service/application/port/BaseRepositoryPort.java
package dronefleet_service.application.port;

import dronefleet_service.domain.model.Base;
import dronefleet_service.domain.model.BaseId;
import dronefleet_service.domain.model.Location;

import java.util.Optional;

public interface BaseRepository {
    boolean  isPresent(BaseId baseId);
    boolean isPresentByLoc(Location location);
    Optional<Base> findById(BaseId baseId);
    Optional<Base> findClosestBase(Location location);
    void save(Base base);
}
