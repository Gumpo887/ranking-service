package net.movievault.ranking_service.domain.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class ItemRarityUpdatedEvent extends DomainEvent {

    private String eventId;
    private String itemId;
    private long owners;
    private long activeUsers;
    private double rarity;
    private LocalDateTime occurredOn;

    public ItemRarityUpdatedEvent(String eventId,
                                  String itemId,
                                  long owners,
                                  long activeUsers,
                                  double rarity) {
        super("ItemRarityUpdated");
        this.eventId = eventId;
        this.itemId = itemId;
        this.owners = owners;
        this.activeUsers = activeUsers;
        this.rarity = rarity;
        this.occurredOn = LocalDateTime.now();
    }
}
