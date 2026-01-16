package net.movievault.ranking_service.infrastructure.config;

import net.movievault.ranking_service.domain.event.DomainEvent;
import net.movievault.ranking_service.domain.event.ItemRarityUpdatedEvent;
import net.movievault.ranking_service.domain.event.UserItemChangedEvent;
import net.movievault.ranking_service.domain.event.UserStatusChangedEvent;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RankingKafkaConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    private final RankingProperties rankingProperties;

    public RankingKafkaConfig(RankingProperties rankingProperties) {
        this.rankingProperties = rankingProperties;
    }

    @Bean
    public KafkaAdmin rankingKafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic userItemChangedTopic() {
        return new NewTopic(rankingProperties.getUserItemChangedTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic userStatusChangedTopic() {
        return new NewTopic(rankingProperties.getUserStatusChangedTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic itemRarityUpdatedTopic() {
        return new NewTopic(rankingProperties.getItemRarityUpdatedTopic(), 1, (short) 1);
    }

    @Bean
    public ProducerFactory<String, DomainEvent> rankingProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, DomainEvent> rankingKafkaTemplate() {
        return new KafkaTemplate<>(rankingProducerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserItemChangedEvent> userItemChangedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserItemChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(UserItemChangedEvent.class, false)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserStatusChangedEvent> userStatusChangedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserStatusChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(UserStatusChangedEvent.class, false)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ItemRarityUpdatedEvent> itemRarityUpdatedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ItemRarityUpdatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(ItemRarityUpdatedEvent.class, false)));
        return factory;
    }

    private Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }
}
