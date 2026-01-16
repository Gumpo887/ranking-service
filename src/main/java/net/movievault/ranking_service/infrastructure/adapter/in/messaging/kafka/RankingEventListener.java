package net.movievault.ranking_service.infrastructure.adapter.in.messaging.kafka;

import lombok.extern.slf4j.Slf4j;
import net.movievault.ranking_service.application.service.RankingEventProcessor;
import net.movievault.ranking_service.domain.event.ItemRarityUpdatedEvent;
import net.movievault.ranking_service.domain.event.UserItemChangedEvent;
import net.movievault.ranking_service.domain.event.UserStatusChangedEvent;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RankingEventListener {

    private final RankingEventProcessor eventProcessor;

    public RankingEventListener(RankingEventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @KafkaListener(
            topics = "${ranking.user-item-changed-topic}",
            groupId = "${spring.application.name}",
            containerFactory = "userItemChangedKafkaListenerContainerFactory"
    )
    public void onUserItemChanged(UserItemChangedEvent event) {
        log.info("Consumed UserItemChanged event for user {} and item {}.", event.getUserId(), event.getItemId());
        eventProcessor.handleUserItemChanged(event);
    }

    @KafkaListener(
            topics = "${ranking.user-status-changed-topic}",
            groupId = "${spring.application.name}",
            containerFactory = "userStatusChangedKafkaListenerContainerFactory"
    )
    public void onUserStatusChanged(UserStatusChangedEvent event) {
        log.info("Consumed UserStatusChanged event for user {}.", event.getUserId());
        eventProcessor.handleUserStatusChanged(event);
    }

    @KafkaListener(
            topics = "${ranking.item-rarity-updated-topic}",
            groupId = "${spring.application.name}",
            containerFactory = "itemRarityUpdatedKafkaListenerContainerFactory"
    )
    public void onItemRarityUpdated(ItemRarityUpdatedEvent event) {
        log.info("Consumed ItemRarityUpdated event for item {}.", event.getItemId());
        eventProcessor.handleItemRarityUpdated(event);
    }
}
