package com.mwkim.projecthub.minipay.entity;

import com.mwkim.projecthub.minipay.enums.AccountType;
import com.mwkim.projecthub.minipay.enums.SettlementType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private SettlementType type; // 균등정산 or 랜덤 정산

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SettlementParticipant> participants = new ArrayList<>();

    @Builder
    public Settlement(User requester, BigDecimal totalAmount, SettlementType type, List<SettlementParticipant> participants) {
        this.requester = requester;
        this.totalAmount = totalAmount;
        this.participants = participants;
        this.type = type;
    }

    public void addParticipant(SettlementParticipant participant) {
        this.participants.add(participant);
        participant.addSettlement(this);
    }

    // factory-method
    public static Settlement createSettlement(User requester, BigDecimal totalAmount, SettlementType type) {
        return Settlement.builder()
                .requester(requester)
                .totalAmount(totalAmount)
                .type(type)
                .build();
    }
}

