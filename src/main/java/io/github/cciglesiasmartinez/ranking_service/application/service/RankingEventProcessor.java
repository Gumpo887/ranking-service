package io.github.cciglesiasmartinez.ranking_service.application.service;

import io.github.cciglesiasmartinez.ranking_service.domain.event.ItemRarityUpdatedEvent;
import io.github.cciglesiasmartinez.ranking_service.domain.event.UserItemChangedEvent;
import io.github.cciglesiasmartinez.ranking_service.domain.event.UserStatusChangedEvent;
import io.github.cciglesiasmartinez.ranking_service.domain.model.UserItemAction;
import io.github.cciglesiasmartinez.ranking_service.domain.model.UserStatus;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.messaging.kafka.RankingKafkaProducer;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.RankingElasticsearchRepository;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.document.ItemRarityDocument;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.document.UserItemDocument;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.document.UserScoreDocument;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.config.RankingProperties;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RankingEventProcessor {

    private final RankingElasticsearchRepository repository;
    private final RankingKafkaProducer kafkaProducer;
    private final RankingProperties rankingProperties;

    public RankingEventProcessor(RankingElasticsearchRepository repository,
                                 RankingKafkaProducer kafkaProducer,
                                 RankingProperties rankingProperties) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
        this.rankingProperties = rankingProperties;
    }

    public void handleUserItemChanged(UserItemChangedEvent event) {
        if (repository.isEventProcessed(event.getEventId())) {
            return;
        }
        repository.markEventProcessed(event.getEventId());
        MDC.put("requestId", event.getEventId());

        String userId = event.getUserId();
        String itemId = event.getItemId();
        boolean existing = repository.getUserItem(userId, itemId).isPresent();

        if (event.getAction() == UserItemAction.REMOVE) {
            if (existing) {
                repository.deleteUserItem(userId, itemId);
                repository.updateItemOwners(itemId, -1);
            }
        } else {
            if (!existing) {
                repository.updateItemOwners(itemId, 1);
            }
            double editionWeight = rankingProperties.editionWeight(event.getEdition().name());
            double conditionWeight = rankingProperties.conditionWeight(event.getCondition().name());
            double completenessWeight = rankingProperties.completenessWeight(event.getCompleteness().name());
            long activeUsers = repository.getActiveUsers();
            long owners = repository.getItemRarity(itemId).map(ItemRarityDocument::getOwners).orElse(0L);
            double rarity = calculateRarity(activeUsers, owners);
            double scoreItem = calculateScoreItem(rarity, editionWeight, conditionWeight, completenessWeight);
            UserItemDocument document = new UserItemDocument(
                    repository.userItemId(userId, itemId),
                    userId,
                    itemId,
                    editionWeight,
                    conditionWeight,
                    completenessWeight,
                    rarity,
                    scoreItem,
                    LocalDateTime.now());
            repository.upsertUserItem(document);
        }

        long activeUsers = repository.getActiveUsers();
        long owners = repository.getItemRarity(itemId).map(ItemRarityDocument::getOwners).orElse(0L);
        double rarity = calculateRarity(activeUsers, owners);
        repository.upsertItemRarity(new ItemRarityDocument(itemId, owners, activeUsers, rarity, LocalDateTime.now()));
        publishItemRarityUpdated(itemId, owners, activeUsers, rarity);
        recalculateUserScore(userId);
        MDC.clear();
    }

    public void handleUserStatusChanged(UserStatusChangedEvent event) {
        if (repository.isEventProcessed(event.getEventId())) {
            return;
        }
        repository.markEventProcessed(event.getEventId());
        MDC.put("requestId", event.getEventId());

        int delta = event.getStatus() == UserStatus.ACTIVE ? 1 : -1;
        long activeUsers = repository.updateActiveUsers(delta);
        repository.updateAllItemRarities(activeUsers, rankingProperties.getM());
        List<ItemRarityDocument> items = repository.findAllItemRarities();
        for (ItemRarityDocument document : items) {
            publishItemRarityUpdated(
                    document.getItemId(),
                    document.getOwners(),
                    document.getActiveUsers(),
                    document.getRarity());
        }
        MDC.clear();
    }

    public void handleItemRarityUpdated(ItemRarityUpdatedEvent event) {
        if (repository.isEventProcessed(event.getEventId())) {
            return;
        }
        repository.markEventProcessed(event.getEventId());
        MDC.put("requestId", event.getEventId());

        repository.updateUserItemsRarity(event.getItemId(), event.getRarity());
        List<String> userIds = repository.findUserIdsByItemId(event.getItemId());
        Set<String> distinctUserIds = userIds.stream().collect(Collectors.toSet());
        for (String userId : distinctUserIds) {
            recalculateUserScore(userId);
        }
        MDC.clear();
    }

    private void recalculateUserScore(String userId) {
        List<UserItemDocument> topItems = repository.getTopUserItems(userId, rankingProperties.getTopK());
        double score = topItems.stream().mapToDouble(UserItemDocument::getScoreItem).sum();
        long distinctOwnedCount = repository.countUserItems(userId);
        UserScoreDocument document = new UserScoreDocument(
                userId,
                score,
                distinctOwnedCount,
                rankingProperties.getTopK(),
                LocalDateTime.now());
        repository.upsertUserScore(document);
    }

    private void publishItemRarityUpdated(String itemId, long owners, long activeUsers, double rarity) {
        String eventId = UUID.randomUUID().toString();
        ItemRarityUpdatedEvent updateEvent = new ItemRarityUpdatedEvent(eventId, itemId, owners, activeUsers, rarity);
        updateEvent.setRequestId(eventId);
        kafkaProducer.sendItemRarityUpdated(updateEvent);
    }

    private double calculateRarity(long activeUsers, long owners) {
        double raw = Math.log((activeUsers + rankingProperties.getM()) / (double) (owners + rankingProperties.getM()));
        double rarity = 1 - Math.exp(-raw);
        return clamp(rarity, 0, 1);
    }

    private double calculateScoreItem(double rarity, double editionWeight, double conditionWeight, double completenessWeight) {
        return 100 * (0.60 * rarity + 0.20 * editionWeight + 0.10 * conditionWeight + 0.10 * completenessWeight);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
