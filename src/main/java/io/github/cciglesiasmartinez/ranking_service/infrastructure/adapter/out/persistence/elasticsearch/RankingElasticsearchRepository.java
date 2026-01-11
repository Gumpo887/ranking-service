package io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateByQueryResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.document.CounterDocument;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.document.ItemRarityDocument;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.document.ProcessedEventDocument;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.document.UserItemDocument;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.document.UserScoreDocument;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.config.RankingProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RankingElasticsearchRepository {

    public static final String USER_ITEMS_INDEX = "user-items-v1";
    public static final String ITEM_RARITY_INDEX = "item-rarity-v1";
    public static final String USER_SCORE_INDEX = "user-score-v1";
    public static final String COUNTERS_INDEX = "counters-v1";
    public static final String PROCESSED_EVENTS_INDEX = "processed-events-v1";
    public static final String GLOBAL_COUNTER_ID = "global";

    private final ElasticsearchClient client;
    private final RankingProperties rankingProperties;

    public RankingElasticsearchRepository(ElasticsearchClient client, RankingProperties rankingProperties) {
        this.client = client;
        this.rankingProperties = rankingProperties;
    }

    public boolean isEventProcessed(String eventId) {
        try {
            return client.exists(request -> request.index(PROCESSED_EVENTS_INDEX).id(eventId)).value();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to check processed event", ex);
        }
    }

    public void markEventProcessed(String eventId) {
        String ts = Instant.now().toString();
        ProcessedEventDocument document = new ProcessedEventDocument(eventId, ts);
        try {
            client.index(request -> request.index(PROCESSED_EVENTS_INDEX).id(eventId).document(document));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to persist processed event", ex);
        }
    }

    public Optional<UserItemDocument> getUserItem(String userId, String itemId) {
        String id = userItemId(userId, itemId);
        try {
            GetResponse<UserItemDocument> response = client.get(
                    request -> request.index(USER_ITEMS_INDEX).id(id),
                    UserItemDocument.class
            );
            if (!response.found()) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.source());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load user item", ex);
        }
    }

    public void upsertUserItem(UserItemDocument document) {
        try {
            client.index(request -> request.index(USER_ITEMS_INDEX).id(document.getId()).document(document));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to upsert user item", ex);
        }
    }

    public void deleteUserItem(String userId, String itemId) {
        try {
            client.delete(request -> request.index(USER_ITEMS_INDEX).id(userItemId(userId, itemId)));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete user item", ex);
        }
    }

    public long updateItemOwners(String itemId, int delta) {
        String ts = Instant.now().toString();

        Map<String, JsonData> params = new HashMap<>();
        params.put("delta", JsonData.of((long) delta));
        params.put("itemId", JsonData.of(itemId));
        params.put("updatedAt", JsonData.of(ts));

        // upsert: (String, long, long, double, String)
        ItemRarityDocument upsert = new ItemRarityDocument(
                itemId,
                Math.max(0L, (long) delta),
                0L,
                0.0,
                ts
        );

        try {
            client.update(UpdateRequest.of(builder -> builder
                            .index(ITEM_RARITY_INDEX)
                            .id(itemId)
                            .script(script -> script.inline(inline -> inline
                                    .source(
                                            "ctx._source.item_id = params.itemId;" +
                                            "ctx._source.owners = (ctx._source.owners == null ? 0 : ctx._source.owners) + params.delta;" +
                                            "if (ctx._source.owners < 0) { ctx._source.owners = 0; }" +
                                            "ctx._source.updated_at = params.updatedAt;"
                                    )
                                    .params(params)
                            ))
                            .upsert(upsert)),
                    ItemRarityDocument.class
            );

            return getItemRarity(itemId).map(ItemRarityDocument::getOwners).orElse(0L);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to update item owners", ex);
        }
    }

    public Optional<ItemRarityDocument> getItemRarity(String itemId) {
        try {
            GetResponse<ItemRarityDocument> response = client.get(
                    request -> request.index(ITEM_RARITY_INDEX).id(itemId),
                    ItemRarityDocument.class
            );
            if (!response.found()) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.source());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load item rarity", ex);
        }
    }

    public void upsertItemRarity(ItemRarityDocument document) {
        try {
            client.index(request -> request.index(ITEM_RARITY_INDEX).id(document.getItemId()).document(document));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to update item rarity", ex);
        }
    }

    public long getActiveUsers() {
        try {
            GetResponse<CounterDocument> response = client.get(
                    request -> request.index(COUNTERS_INDEX).id(GLOBAL_COUNTER_ID),
                    CounterDocument.class
            );
            if (!response.found() || response.source() == null) {
                return 0L;
            }
            return response.source().getActiveUsers();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read active users", ex);
        }
    }

    public long updateActiveUsers(int delta) {
        String ts = Instant.now().toString();

        Map<String, JsonData> params = new HashMap<>();
        params.put("delta", JsonData.of((long) delta));
        params.put("updatedAt", JsonData.of(ts));

        // CounterDocument: (long, String)
        CounterDocument upsert = new CounterDocument(
                Math.max(0L, (long) delta),
                ts
        );

        try {
            client.update(UpdateRequest.of(builder -> builder
                            .index(COUNTERS_INDEX)
                            .id(GLOBAL_COUNTER_ID)
                            .script(script -> script.inline(inline -> inline
                                    .source(
                                            "ctx._source.active_users = (ctx._source.active_users == null ? 0 : ctx._source.active_users) + params.delta;" +
                                            "if (ctx._source.active_users < 0) { ctx._source.active_users = 0; }" +
                                            "ctx._source.updated_at = params.updatedAt;"
                                    )
                                    .params(params)
                            ))
                            .upsert(upsert)),
                    CounterDocument.class
            );

            return getActiveUsers();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to update active users", ex);
        }
    }

    public UpdateByQueryResponse updateUserItemsRarity(String itemId, double rarity) {
        String ts = Instant.now().toString();

        Map<String, JsonData> params = new HashMap<>();
        params.put("rarity", JsonData.of(rarity));
        params.put("updatedAt", JsonData.of(ts));

        try {
            return client.updateByQuery(request -> request
                    .index(USER_ITEMS_INDEX)
                    .query(query -> query.term(term -> term.field("item_id").value(itemId)))
                    .script(script -> script.inline(inline -> inline
                            .source(
                                    "ctx._source.rarity = params.rarity;" +
                                    "ctx._source.score_item = 100 * (0.60 * params.rarity + 0.20 * ctx._source.edition_weight + 0.10 * ctx._source.condition_weight + 0.10 * ctx._source.completeness_weight);" +
                                    "ctx._source.updated_at = params.updatedAt;"
                            )
                            .params(params)
                    )));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to update user items rarity", ex);
        }
    }

    public UpdateByQueryResponse updateAllItemRarities(long activeUsers, int m) {
        String ts = Instant.now().toString();

        Map<String, JsonData> params = new HashMap<>();
        params.put("activeUsers", JsonData.of(activeUsers));
        params.put("m", JsonData.of(m));
        params.put("updatedAt", JsonData.of(ts));

        try {
            return client.updateByQuery(request -> request
                    .index(ITEM_RARITY_INDEX)
                    .script(script -> script.inline(inline -> inline
                            .source(
                                    "double owners = (ctx._source.owners == null ? 0 : ctx._source.owners);" +
                                    "double raw = Math.log((params.activeUsers + params.m) / (double) (owners + params.m));" +
                                    "double rarity = 1 - Math.exp(-raw);" +
                                    "if (rarity < 0) { rarity = 0; }" +
                                    "if (rarity > 1) { rarity = 1; }" +
                                    "ctx._source.active_users = params.activeUsers;" +
                                    "ctx._source.rarity = rarity;" +
                                    "ctx._source.updated_at = params.updatedAt;"
                            )
                            .params(params)
                    )));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to update item rarities", ex);
        }
    }

    public List<String> findUserIdsByItemId(String itemId) {
        List<String> userIds = new ArrayList<>();
        List<FieldValue> searchAfter = null;

        while (true) {
            SearchResponse<UserItemDocument> response = searchUserItems(itemId, searchAfter);
            List<Hit<UserItemDocument>> hits = response.hits().hits();
            if (hits.isEmpty()) {
                break;
            }
            for (Hit<UserItemDocument> hit : hits) {
                if (hit.source() != null) {
                    userIds.add(hit.source().getUserId());
                }
            }
            searchAfter = hits.get(hits.size() - 1).sort();
            if (searchAfter == null || hits.size() < rankingProperties.getBatchSize()) {
                break;
            }
        }

        return userIds.stream().distinct().collect(Collectors.toList());
    }

    private SearchResponse<UserItemDocument> searchUserItems(String itemId, List<FieldValue> searchAfter) {
        try {
            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index(USER_ITEMS_INDEX)
                    .query(query -> query.term(term -> term.field("item_id").value(itemId)))
                    .sort(sort -> sort.field(field -> field.field("user_id").order(SortOrder.Asc)))
                    .size(rankingProperties.getBatchSize());

            if (searchAfter != null) {
                builder.searchAfter(searchAfter);
            }

            return client.search(builder.build(), UserItemDocument.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to search user items", ex);
        }
    }

    public List<ItemRarityDocument> findAllItemRarities() {
        List<ItemRarityDocument> items = new ArrayList<>();
        List<FieldValue> searchAfter = null;

        while (true) {
            SearchResponse<ItemRarityDocument> response = searchItemRarities(searchAfter);
            List<Hit<ItemRarityDocument>> hits = response.hits().hits();
            if (hits.isEmpty()) {
                break;
            }
            for (Hit<ItemRarityDocument> hit : hits) {
                if (hit.source() != null) {
                    items.add(hit.source());
                }
            }
            searchAfter = hits.get(hits.size() - 1).sort();
            if (searchAfter == null || hits.size() < rankingProperties.getBatchSize()) {
                break;
            }
        }

        return items;
    }

    private SearchResponse<ItemRarityDocument> searchItemRarities(List<FieldValue> searchAfter) {
        try {
            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index(ITEM_RARITY_INDEX)
                    .sort(sort -> sort.field(field -> field.field("item_id").order(SortOrder.Asc)))
                    .size(rankingProperties.getBatchSize());

            if (searchAfter != null) {
                builder.searchAfter(searchAfter);
            }

            return client.search(builder.build(), ItemRarityDocument.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to search item rarities", ex);
        }
    }

    public List<UserItemDocument> getTopUserItems(String userId, int topK) {
        try {
            SearchResponse<UserItemDocument> response = client.search(request -> request
                            .index(USER_ITEMS_INDEX)
                            .query(query -> query.term(term -> term.field("user_id").value(userId)))
                            .sort(sort -> sort.field(field -> field.field("score_item").order(SortOrder.Desc)))
                            .size(topK),
                    UserItemDocument.class
            );

            if (response.hits() == null) {
                return Collections.emptyList();
            }

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read user items", ex);
        }
    }

    public long countUserItems(String userId) {
        try {
            return client.count(CountRequest.of(request -> request
                            .index(USER_ITEMS_INDEX)
                            .query(query -> query.term(term -> term.field("user_id").value(userId)))))
                    .count();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to count user items", ex);
        }
    }

    public void upsertUserScore(UserScoreDocument document) {
        try {
            client.index(request -> request.index(USER_SCORE_INDEX).id(document.getUserId()).document(document));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to update user score", ex);
        }
    }

    public Optional<UserScoreDocument> getUserScore(String userId) {
        try {
            GetResponse<UserScoreDocument> response = client.get(
                    request -> request.index(USER_SCORE_INDEX).id(userId),
                    UserScoreDocument.class
            );

            if (!response.found()) {
                return Optional.empty();
            }

            return Optional.ofNullable(response.source());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read user score", ex);
        }
    }

    public List<UserScoreDocument> getLeaderboard(int limit) {
        try {
            SearchResponse<UserScoreDocument> response = client.search(request -> request
                            .index(USER_SCORE_INDEX)
                            .sort(sort -> sort.field(field -> field.field("score").order(SortOrder.Desc)))
                            .size(limit),
                    UserScoreDocument.class
            );

            if (response.hits() == null) {
                return Collections.emptyList();
            }

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read leaderboard", ex);
        }
    }

    public String userItemId(String userId, String itemId) {
        return userId + "|" + itemId;
    }
}
