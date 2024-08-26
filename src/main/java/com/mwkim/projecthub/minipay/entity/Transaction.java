package com.mwkim.projecthub.minipay.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account toAccount;

    private BigDecimal amount;

    private LocalDateTime timestamp;

    @Builder
    public Transaction(Long id, Account fromAccount, Account toAccount, BigDecimal amount, LocalDateTime timestamp) {
        this.id = id;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }
}