package io.github.cciglesiasmartinez.ranking_service.application.service;

import io.github.cciglesiasmartinez.microservice_template.infrastructure.adapter.in.web.dto.responses.Envelope;
import io.github.cciglesiasmartinez.microservice_template.infrastructure.adapter.in.web.dto.responses.Meta;
import io.github.cciglesiasmartinez.ranking_service.application.port.in.RankingQueryUseCase;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web.dto.responses.LeaderboardEntryResponse;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web.dto.responses.LeaderboardResponse;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web.dto.responses.UserScoreResponse;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.RankingElasticsearchRepository;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.out.persistence.elasticsearch.document.UserScoreDocument;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.config.RankingProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RankingQueryService implements RankingQueryUseCase {

    private final RankingElasticsearchRepository repository;
    private final RankingProperties rankingProperties;

    public RankingQueryService(RankingElasticsearchRepository repository, RankingProperties rankingProperties) {
        this.repository = repository;
        this.rankingProperties = rankingProperties;
    }

    @Override
    public Envelope<UserScoreResponse> getUserScore(String userId) {
        Optional<UserScoreDocument> score = repository.getUserScore(userId);
        UserScoreResponse response = score
                .map(document -> new UserScoreResponse(
                        document.getUserId(),
                        document.getScore(),
                        document.getDistinctOwnedCount(),
                        document.getTopK(),
                        document.getUpdatedAt()))
                .orElse(new UserScoreResponse(userId, 0.0, 0L, rankingProperties.getTopK(), null));
        return new Envelope<>(response, new Meta());
    }

    @Override
    public Envelope<LeaderboardResponse> getLeaderboard(int limit) {
        List<UserScoreDocument> leaders = repository.getLeaderboard(limit);
        List<LeaderboardEntryResponse> entries = leaders.stream()
                .map(document -> new LeaderboardEntryResponse(document.getUserId(), document.getScore()))
                .collect(Collectors.toList());
        return new Envelope<>(new LeaderboardResponse(entries), new Meta());
    }
}
