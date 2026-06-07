package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.hikikomori.community.config.ScoreWeights;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScoreServiceTest {

    ScoreService service = new ScoreService();
    ScoreWeights weights = new ScoreWeights(10, 20);

    @Test
    @DisplayName("power = Wv·순추천 − Wr·신고")
    void 산식() {
        assertThat(service.compute(5, 1, weights)).isEqualTo(10L * 5 - 20L * 1); // 30
    }

    @Test
    @DisplayName("음수면 0으로 하한")
    void 하한() {
        assertThat(service.compute(0, 5, weights)).isEqualTo(0L);   // -100 → 0
        assertThat(service.compute(-3, 0, weights)).isEqualTo(0L);  // -30 → 0
    }
}
