package com.mwkim.projecthub.minipay.entity;

import com.mwkim.projecthub.minipay.enums.SettlementParticipantStatus;
import com.mwkim.projecthub.minipay.enums.SettlementType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementParticipant {
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

    @Enumerated(EnumType.STRING)
    private SettlementParticipantStatus status;

    @Builder
    public SettlementParticipant(User user, BigDecimal amount) {
        this.user = user;
        this.amount = amount;
        this.status = SettlementParticipantStatus.PENDING;
    }

    public void addSettlement(Settlement settlement) {
        this.settlement = settlement;
    }

    public void approve() {
        this.status = SettlementParticipantStatus.APPROVED;
    }

    public void reject() {
        this.status = SettlementParticipantStatus.REJECTED;
    }

    public static SettlementParticipant createSettlementParticipant(User user, BigDecimal amount) {
        return SettlementParticipant.builder()
                .user(user)
                .amount(amount)
                .build();
    }
}