package org.hikikomori.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Formula;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import org.hikikomori.community.util.UUIDGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(indexes = @Index(name = "idx_post_user_id", columnList = "user_id"))
@Getter
@NoArgsConstructor
public class Post {

    @Id
    private UUID id;

    private Long userId;

    private String nickName;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private PostTag tag;

    @Formula("(SELECT COUNT(*) FROM comment c WHERE c.post_id = id)")
    private long commentCount;

    @Formula(
        "(SELECT COALESCE(SUM(CASE WHEN v.vote_value = 'UP' THEN v.delta ELSE -v.delta END), 0) "
        + "FROM vote v WHERE v.target_type = 'POST' AND v.target_id = id)")
    private long voteScore;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime hiddenAt;

    public void update(String title, String content, PostTag tag) {
        this.title = title;
        this.content = content;
        this.tag = tag;
        this.updatedAt = LocalDateTime.now();
    }

    public void hide() {
        if (this.hiddenAt == null) {
            this.hiddenAt = LocalDateTime.now();
        }
    }

    public boolean isHidden() {
        return this.hiddenAt != null;
    }

    @Builder
    public Post(Long userId, String nickName, String title, String content, PostTag tag) {
        this.id = UUIDGenerator.generate();
        this.userId = userId;
        this.nickName = nickName;
        this.title = title;
        this.content = content;
        this.tag = tag;
        this.createdAt = LocalDateTime.now();
    }
}
