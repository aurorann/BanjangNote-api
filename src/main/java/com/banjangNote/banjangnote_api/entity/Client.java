package com.banjangNote.banjangnote_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Getter @Setter @NoArgsConstructor
public class Client {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "contact_name", length = 50)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "business_number", length = 20)
    private String businessNumber;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void setContactName(String contactName) {
        // 값이 null이거나, 공백을 제거(trim)했을 때 비어있다면 null을 넣고, 아니면 공백을 제거한 깔끔한 값 넣음
        this.contactName = (contactName == null || contactName.trim().isEmpty()) ? null : contactName.trim();
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = (contactPhone == null || contactPhone.trim().isEmpty()) ? null : contactPhone.trim();
    }

    public void setBusinessNumber(String businessNumber) {
        this.businessNumber = (businessNumber == null || businessNumber.trim().isEmpty()) ? null : businessNumber.trim();
    }
}
