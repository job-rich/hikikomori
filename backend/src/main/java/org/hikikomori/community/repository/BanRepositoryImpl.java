package org.hikikomori.community.repository;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Ban;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BanRepositoryImpl {

    private final BanJpaRepository jpaRepository;

    public boolean isBanned(Long userId) {
        return jpaRepository.existsByUserId(userId);
    }

    public Ban save(Ban ban) {
        return jpaRepository.save(ban);
    }
}
