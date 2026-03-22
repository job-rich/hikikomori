package org.hikikomori.community.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    private UUID id;

    private Long userId;

    private String nickName;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> children = new ArrayList<>();

    public void updateContent(String content) {
        if (isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다");
        }
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.content = DeleteTaunt.pick();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = LocalDateTime.now();
    }

    @Builder
    public Comment(String content, Long userId, String nickName, Post post, Comment parent) {
        this.id = UUIDGenerator.generate();
        this.content = content;
        this.userId = userId;
        this.nickName = nickName;
        this.post = post;
        this.parent = parent;
        this.createdAt = LocalDateTime.now();
    }
}
