package org.hikikomori.community.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl {

    private final UserJpaRepository jpaRepository;

    public Optional<User> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    public User getByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + userId));
    }

    public User save(User user) {
        return jpaRepository.save(user);
    }

    public Page<UserJpaRepository.RankingRow> findRanking(int wVote, int wReport, Pageable pageable) {
        return jpaRepository.findRanking(wVote, wReport, pageable);
    }

    public long countHigherPower(int wVote, int wReport, long myPower) {
        return jpaRepository.countHigherPower(wVote, wReport, myPower);
    }
}
