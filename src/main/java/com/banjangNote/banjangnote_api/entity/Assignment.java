package com.banjangNote.banjangnote_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Getter @Setter @NoArgsConstructor
public class Assignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Column(columnDefinition = "DECIMAL(5,1) DEFAULT 0.0")
    private Double days = 0.0;

    @Column(name = "applied_daily_rate", nullable = false)
    private Integer appliedDailyRate;

    @Column(name = "is_paid")
    private Boolean isPaid = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}