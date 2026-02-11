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

    private String title;

    private String content;

    private LocalDateTime createdAt;

    @Builder
    public Post(String title, String content) {
        this.id = UUIDGenerator.generate();
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
