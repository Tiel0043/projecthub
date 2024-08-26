package com.mwkim.projecthub.minipay.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementItem { // 참여자 별 정산 항목

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal amount;
    private boolean isPaid;

    @Builder
    public SettlementItem(User user, BigDecimal amount) {
        this.user = user;
        this.amount = amount;
        this.isPaid = false;
    }

    public void addSettlement(Settlement settlement) {
        this.settlement = settlement;
    }

    public void markAsPaid() {
        this.isPaid = true;
    }

}
