package com.banjangNote.banjangnote_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "workers")
@Getter @Setter @NoArgsConstructor
public class Worker {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String role;

    @Column(name = "default_daily_rate")
    private Integer defaultDailyRate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}