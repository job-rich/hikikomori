package org.hikikomori.community.repository;

import java.util.UUID;
import org.hikikomori.community.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, UUID> {
}
