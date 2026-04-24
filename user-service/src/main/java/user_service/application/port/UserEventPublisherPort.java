package user_service.application.port;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface UserEventPublisherPort {
    void publish(Object event) throws JsonProcessingException;
}
