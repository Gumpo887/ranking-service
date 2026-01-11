package io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.messaging.kafka;

import io.github.cciglesiasmartinez.microservice_template.domain.event.DomainEvent;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.config.RankingProperties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RankingKafkaProducer {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private final RankingProperties rankingProperties;

    public RankingKafkaProducer(KafkaTemplate<String, DomainEvent> kafkaTemplate,
                                RankingProperties rankingProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.rankingProperties = rankingProperties;
    }

    public void sendItemRarityUpdated(DomainEvent event) {
        String topicName = rankingProperties.getItemRarityUpdatedTopic();
        CompletableFuture<SendResult<String, DomainEvent>> future = kafkaTemplate.send(topicName, event.getRequestId(), event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                MDC.put("requestId", event.getRequestId());
                log.info("Sent event {} with offset {}.", event.getEventType(), result.getRecordMetadata().offset());
                MDC.clear();
            } else {
                MDC.put("requestId", event.getRequestId());
                log.info("Unable to send event {} due to {}.", event.getEventType(), ex.getMessage());
                MDC.clear();
                throw new IllegalStateException("Failed to publish event");
            }
        });
    }
}
