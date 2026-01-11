package io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web;

import io.github.cciglesiasmartinez.microservice_template.infrastructure.adapter.in.web.dto.responses.Envelope;
import io.github.cciglesiasmartinez.microservice_template.infrastructure.adapter.in.web.dto.responses.Meta;
import io.github.cciglesiasmartinez.ranking_service.application.port.in.RankingQueryUseCase;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web.dto.responses.HealthResponse;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web.dto.responses.LeaderboardResponse;
import io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web.dto.responses.UserScoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/")
@Tag(
        name = "Ranking service.",
        description = "Endpoints related to user ranking and leaderboards."
)
public class RankingController {

    private final RankingQueryUseCase rankingQueryUseCase;

    public RankingController(RankingQueryUseCase rankingQueryUseCase) {
        this.rankingQueryUseCase = rankingQueryUseCase;
    }

    @Operation(
            summary = "Health check endpoint.",
            description = "Verifies that the ranking service is running."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy.")
    })
    @GetMapping("/health")
    public ResponseEntity<Envelope<HealthResponse>> health() {
        Envelope<HealthResponse> response = new Envelope<>(
                new HealthResponse("ok", LocalDateTime.now()),
                new Meta());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Retrieves a user score.",
            description = "Returns the aggregated ranking score for a user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User score retrieved successfully.")
    })
    @GetMapping("/users/{userId}/score")
    public ResponseEntity<Envelope<UserScoreResponse>> getUserScore(@PathVariable String userId) {
        Envelope<UserScoreResponse> response = rankingQueryUseCase.getUserScore(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Retrieves leaderboard.",
            description = "Returns the leaderboard sorted by score."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leaderboard retrieved successfully.")
    })
    @GetMapping("/leaderboard")
    public ResponseEntity<Envelope<LeaderboardResponse>> getLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {
        Envelope<LeaderboardResponse> response = rankingQueryUseCase.getLeaderboard(limit);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
