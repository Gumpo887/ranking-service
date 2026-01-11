package io.github.cciglesiasmartinez.ranking_service.application.port.in;

import io.github.cciglesiasmartinez.microservice_template.infrastructure.adapter.in.web.dto.responses.Envelope;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web.dto.responses.LeaderboardResponse;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web.dto.responses.UserScoreResponse;

public interface RankingQueryUseCase {

    Envelope<UserScoreResponse> getUserScore(String userId);

    Envelope<LeaderboardResponse> getLeaderboard(int limit);
}
