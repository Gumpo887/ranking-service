package net.movievault.ranking_service.application.port.in;

import net.movievault.ranking_service.infrastructure.adapter.in.web.dto.responses.Envelope;
import net.movievault.ranking_service.infrastructure.adapter.in.web.dto.responses.LeaderboardResponse;
import net.movievault.ranking_service.infrastructure.adapter.in.web.dto.responses.UserScoreResponse;

public interface RankingQueryUseCase {

    Envelope<UserScoreResponse> getUserScore(String userId);

    Envelope<LeaderboardResponse> getLeaderboard(int limit);
}
