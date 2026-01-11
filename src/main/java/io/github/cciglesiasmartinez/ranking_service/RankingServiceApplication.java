package io.github.cciglesiasmartinez.ranking_service;

import io.github.cciglesiasmartinez.ranking_service.infrastructure.config.RankingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {
        "io.github.cciglesiasmartinez.ranking_service",
        "io.github.cciglesiasmartinez.microservice_template.infrastructure.logging"
})
@EnableConfigurationProperties(RankingProperties.class)
public class RankingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RankingServiceApplication.class, args);
    }
}
