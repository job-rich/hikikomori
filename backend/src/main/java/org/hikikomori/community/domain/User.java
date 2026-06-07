package org.hikikomori.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hikikomori.community.util.UUIDGenerator;

// 테이블명 "users" — user는 일부 DB 예약어
@Entity
@Table(name = "users", indexes = @Index(name = "uk_users_user_id", columnList = "userId", unique = true))
@Getter
@NoArgsConstructor
public class User {

    @Id
    private UUID id;

    @Column(unique = true)
    private Long userId;

    private String nickName;

    private boolean banned;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public User(Long userId, String nickName) {
        this.id = UUIDGenerator.generate();
        this.userId = userId;
        this.nickName = nickName;
        this.banned = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void updateNickName(String nickName) {
        this.nickName = nickName;
        this.updatedAt = LocalDateTime.now();
    }

    public void markBanned() {
        this.banned = true;
        this.updatedAt = LocalDateTime.now();
    }
}
