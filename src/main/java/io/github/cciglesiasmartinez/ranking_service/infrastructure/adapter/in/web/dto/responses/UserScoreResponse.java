package io.github.cciglesiasmartinez.ranking_service.infrastructure.adapter.in.web.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserScoreResponse {

    private String userId;
    private double score;
    private long distinctOwnedCount;
    private int topK;
    private String updatedAt;

    public UserScoreResponse() {
    }

    public UserScoreResponse(String userId, double score, long distinctOwnedCount, int topK, String updatedAt) {
        this.userId = userId;
        this.score = score;
        this.distinctOwnedCount = distinctOwnedCount;
        this.topK = topK;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public long getDistinctOwnedCount() {
        return distinctOwnedCount;
    }

    public void setDistinctOwnedCount(long distinctOwnedCount) {
        this.distinctOwnedCount = distinctOwnedCount;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
