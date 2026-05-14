package com.banjangNote.banjangnote_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter @Setter @NoArgsConstructor
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 업체(Client)와 N:1 연결
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    private Client client;

    // 회원(Member)와 N:1 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, length = 100)
    private String name;

    private String address;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_settled")
    private Boolean isSettled = false;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public void setAddress(String address) {
        this.address = (address == null || address.trim().isEmpty()) ? null : address.trim();
    }

    public void setMemo(String memo) {
        this.memo = (memo == null || memo.trim().isEmpty()) ? null : memo.trim();
    }
}
