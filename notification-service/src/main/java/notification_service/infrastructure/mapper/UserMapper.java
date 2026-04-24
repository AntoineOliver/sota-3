package notification_service.infrastructure.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import notification_service.infrastructure.event.UserEvent;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final ObjectMapper objectMapper;

    public UserMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserEvent fromJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            String userId = node.get("userId").asText();
            String typeRaw = node.get("type").asText();

            UserEvent.UserEventType type = switch (typeRaw) {
                case "UserRegisteredEvent" -> UserEvent.UserEventType.USER_REGISTERED;
                case "UserEmailUpdatedEvent" -> UserEvent.UserEventType.USER_EMAIL_UPDATED;
                case "UserDeletedEvent" -> UserEvent.UserEventType.USER_DELETED;
                default -> throw new IllegalArgumentException("Unknown user event: " + typeRaw);
            };

            return new UserEvent(
                    userId,
                    node.has("username") ? node.get("username").asText() : null,
                    node.has("email") ? node.get("email").asText() : null,
                    type,
                    node.has("oldEmail") ? node.get("oldEmail").asText() : null,
                    node.has("newEmail") ? node.get("newEmail").asText() : null
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to map UserEvent", e);
        }
    }
}