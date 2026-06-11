package org.hikikomori.community.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hikikomori.community.util.UUIDGenerator;

@Entity
@Table(
        name = "post_like",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_like_user_post",
                columnNames = {"userId", "postId"}
        ),
        indexes = @Index(name = "idx_post_like_user", columnList = "userId")
)
@Getter
@NoArgsConstructor
public class PostLike {

    @Id
    private UUID id;

    private Long userId;

    private UUID postId;

    private LocalDateTime createdAt;

    @Builder
    public PostLike(Long userId, UUID postId) {
        this.id = UUIDGenerator.generate();
        this.userId = userId;
        this.postId = postId;
        this.createdAt = LocalDateTime.now();
    }
}
