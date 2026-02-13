package org.hikikomori.community.domain;

import jakarta.persistence.CascadeType;
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

    private String content;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private final List<Comment> children = new ArrayList<>();

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
