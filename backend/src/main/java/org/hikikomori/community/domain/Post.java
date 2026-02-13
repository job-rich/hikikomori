package org.hikikomori.community.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Post {

    @Id
    private UUID id;

    private Long userId;

    private String nickName;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    @Builder
    public Post(Long userId, String nickName, String title, String content) {
        this.id = UUIDGenerator.generate();
        this.userId = userId;
        this.nickName = nickName;
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
