package org.hikikomori.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hikikomori.community.util.UUIDGenerator;

@Entity
@Table(name = "ban")
@Getter
@NoArgsConstructor
public class Ban {

    @Id
    private UUID id;

    @Column(unique = true)
    private Long userId;

    private String reason;

    private LocalDateTime bannedAt;

    @Builder
    public Ban(Long userId, String reason) {
        this.id = UUIDGenerator.generate();
        this.userId = userId;
        this.reason = reason;
        this.bannedAt = LocalDateTime.now();
    }
}
