package net.movievault.ranking_service.infrastructure.adapter.out.persistence.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Component
public class ElasticsearchIndexInitializer {

    private final ElasticsearchClient client;

    public ElasticsearchIndexInitializer(ElasticsearchClient client) {
        this.client = client;
    }

    @PostConstruct
    public void initializeIndices() {
        try {
            createUserItemsIndex();
            createItemRarityIndex();
            createUserScoreIndex();
            createCountersIndex();
            createProcessedEventsIndex();
        } catch (IOException ex) {
        	System.out.println("Se rompe");
           // throw new IllegalStateException("Failed to initialize Elasticsearch indices", ex);
        }
    }

    private void createUserItemsIndex() throws IOException {
        if (client.indices().exists(request -> request.index(RankingElasticsearchRepository.USER_ITEMS_INDEX)).value()) {
            return;
        }
        client.indices().create(request -> request
                .index(RankingElasticsearchRepository.USER_ITEMS_INDEX)
                .mappings(mappings -> mappings
                        .properties("user_id", prop -> prop.keyword(keyword -> keyword))
                        .properties("item_id", prop -> prop.keyword(keyword -> keyword))
                        .properties("edition_weight", prop -> prop.double_(number -> number))
                        .properties("condition_weight", prop -> prop.double_(number -> number))
                        .properties("completeness_weight", prop -> prop.double_(number -> number))
                        .properties("rarity", prop -> prop.double_(number -> number))
                        .properties("score_item", prop -> prop.double_(number -> number))
                        .properties("updated_at", prop -> prop.date(date -> date))));
    }

    private void createItemRarityIndex() throws IOException {
        if (client.indices().exists(request -> request.index(RankingElasticsearchRepository.ITEM_RARITY_INDEX)).value()) {
            return;
        }
        client.indices().create(request -> request
                .index(RankingElasticsearchRepository.ITEM_RARITY_INDEX)
                .mappings(mappings -> mappings
                        .properties("item_id", prop -> prop.keyword(keyword -> keyword))
                        .properties("owners", prop -> prop.long_(number -> number))
                        .properties("active_users", prop -> prop.long_(number -> number))
                        .properties("rarity", prop -> prop.double_(number -> number))
                        .properties("updated_at", prop -> prop.date(date -> date))));
    }

    private void createUserScoreIndex() throws IOException {
        if (client.indices().exists(request -> request.index(RankingElasticsearchRepository.USER_SCORE_INDEX)).value()) {
            return;
        }
        client.indices().create(request -> request
                .index(RankingElasticsearchRepository.USER_SCORE_INDEX)
                .mappings(mappings -> mappings
                        .properties("user_id", prop -> prop.keyword(keyword -> keyword))
                        .properties("score", prop -> prop.double_(number -> number))
                        .properties("distinct_owned_count", prop -> prop.long_(number -> number))
                        .properties("top_k", prop -> prop.integer(number -> number))
                        .properties("updated_at", prop -> prop.date(date -> date))));
    }

    private void createCountersIndex() throws IOException {
        if (client.indices().exists(request -> request.index(RankingElasticsearchRepository.COUNTERS_INDEX)).value()) {
            return;
        }
        client.indices().create(request -> request
                .index(RankingElasticsearchRepository.COUNTERS_INDEX)
                .mappings(mappings -> mappings
                        .properties("active_users", prop -> prop.long_(number -> number))
                        .properties("updated_at", prop -> prop.date(date -> date))));
    }

    private void createProcessedEventsIndex() throws IOException {
        if (client.indices().exists(request -> request.index(RankingElasticsearchRepository.PROCESSED_EVENTS_INDEX)).value()) {
            return;
        }
        client.indices().create(request -> request
                .index(RankingElasticsearchRepository.PROCESSED_EVENTS_INDEX)
                .mappings(mappings -> mappings
                        .properties("event_id", prop -> prop.keyword(keyword -> keyword))
                        .properties("processed_at", prop -> prop.date(date -> date))));
    }
}
