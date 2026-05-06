package com.banjangNote.banjangnote_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // 로그인 아이디 (이메일 형식 권장)

    @Column(nullable = false)
    private String password; // 비밀번호 (나중에 반드시 암호화해서 저장해야 합니다!)

    @Column(nullable = false)
    private String name; // 사용자 이름 (예: 반장님 성함)

    @Column(nullable = false)
    private String role = "ROLE_USER"; // 권한 (기본값: 일반 사용자, 필요시 ROLE_ADMIN)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 엔티티가 DB에 처음 저장될 때 현재 시간을 자동으로 세팅해 줍니다.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
