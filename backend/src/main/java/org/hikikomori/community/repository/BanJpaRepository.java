package org.hikikomori.community.repository;

import java.util.UUID;
import org.hikikomori.community.domain.Ban;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanJpaRepository extends JpaRepository<Ban, UUID> {
    boolean existsByUserId(Long userId);
}
