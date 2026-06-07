package org.hikikomori.community.repository;

import java.util.Optional;
import java.util.UUID;
import org.hikikomori.community.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserId(Long userId);
}
