package net.movievault.ranking_service.domain.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.movievault.ranking_service.domain.model.CompletenessType;
import net.movievault.ranking_service.domain.model.ConditionType;
import net.movievault.ranking_service.domain.model.EditionType;
import net.movievault.ranking_service.domain.model.UserItemAction;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class UserItemChangedEvent extends DomainEvent {

    private String eventId;
    private String userId;
    private String itemId;
    private UserItemAction action;
    private EditionType edition;
    private ConditionType condition;
    private CompletenessType completeness;
    private LocalDateTime occurredOn;

    public UserItemChangedEvent(String eventId,
                                String userId,
                                String itemId,
                                UserItemAction action,
                                EditionType edition,
                                ConditionType condition,
                                CompletenessType completeness) {
        super("UserItemChanged");
        this.eventId = eventId;
        this.userId = userId;
        this.itemId = itemId;
        this.action = action;
        this.edition = edition;
        this.condition = condition;
        this.completeness = completeness;
        this.occurredOn = LocalDateTime.now();
    }
}
