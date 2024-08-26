package com.mwkim.projecthub.minipay.entity;

import com.mwkim.projecthub.minipay.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement { // 전체 정산 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal totalAmount; // 총 금액
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;


    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL)
    private List<SettlementItem> items = new ArrayList<>();

    @Builder
    public Settlement(User createdBy, BigDecimal totalAmount) {
        this.createdBy = createdBy;
        this.totalAmount = totalAmount;
        this.status = SettlementStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // 연관관계 편의 메소드
    public void addItem(SettlementItem item) {
        this.items.add(item);
        item.addSettlement(this);
    }

    public void complete() {
        this.status = SettlementStatus.COMPLETED;
    }

    public void cancel() {
        this.status = SettlementStatus.CANCELLED;
    }

}
