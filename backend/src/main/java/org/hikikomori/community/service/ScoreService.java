package org.hikikomori.community.service;

import org.hikikomori.community.config.ScoreWeights;
import org.springframework.stereotype.Service;

@Service
public class ScoreService {

    public long compute(long voteNet, long reports, ScoreWeights w) {
        return Math.max(0L, (long) w.vote() * voteNet - (long) w.report() * reports);
    }
}
