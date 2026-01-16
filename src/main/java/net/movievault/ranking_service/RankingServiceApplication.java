package net.movievault.ranking_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import net.movievault.ranking_service.infrastructure.config.RankingProperties;

@SpringBootApplication(scanBasePackages = {
        "net.movievault.ranking_service",
        "net.movievault.infrastructure.logging"
})
@EnableConfigurationProperties(RankingProperties.class)
public class RankingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RankingServiceApplication.class, args);
    }
}
