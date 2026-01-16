package net.movievault.ranking_service.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "ranking")
public class RankingProperties {

    private int topK = 20;
    private int m = 10;
    private int batchSize = 500;
    private String userItemChangedTopic;
    private String userStatusChangedTopic;
    private String itemRarityUpdatedTopic;
    private String elasticsearchUri;

    private Weights weights = new Weights();

    @Getter
    @Setter
    public static class Weights {
        private Map<String, Double> edition = new HashMap<>();
        private Map<String, Double> condition = new HashMap<>();
        private Map<String, Double> completeness = new HashMap<>();
    }

    public double editionWeight(String edition) {
        return weights.getEdition().getOrDefault(edition, 0.0);
    }

    public double conditionWeight(String condition) {
        return weights.getCondition().getOrDefault(condition, 0.0);
    }

    public double completenessWeight(String completeness) {
        return weights.getCompleteness().getOrDefault(completeness, 0.0);
    }
}
