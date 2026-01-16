package net.movievault.ranking_service.domain.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.movievault.ranking_service.domain.model.UserStatus;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class UserStatusChangedEvent extends DomainEvent {

    private String eventId;
    private String userId;
    private UserStatus status;
    private LocalDateTime occurredOn;

    public UserStatusChangedEvent(String eventId, String userId, UserStatus status) {
        super("UserStatusChanged");
        this.eventId = eventId;
        this.userId = userId;
        this.status = status;
        this.occurredOn = LocalDateTime.now();
    }
}
