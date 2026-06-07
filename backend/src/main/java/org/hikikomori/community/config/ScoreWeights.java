package org.hikikomori.community.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "score.weight")
public record ScoreWeights(int vote, int report) {
}
