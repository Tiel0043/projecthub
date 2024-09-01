package com.mwkim.projecthub.minipay.entity;


import com.mwkim.projecthub.minipay.enums.SettlementType;
import com.mwkim.projecthub.minipay.enums.TransactionStatus;
import com.mwkim.projecthub.minipay.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // 거래 분류 (입금, 출금, 송금)

    private BigDecimal amount;

    private String description;

    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // 거래 생명주기 관리

    @Builder
    public Transaction(Account account, TransactionType type, BigDecimal amount, String description) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.transactionDate = LocalDateTime.now().plusDays(1);
        this.status = TransactionStatus.PENDING;
    }

    public void addAccount(Account account) {
        this.account = account;
    }

    public void complete() {
        this.status = TransactionStatus.COMPLETED;
    }

    public void cancel() {
        this.status = TransactionStatus.CANCELLED;
    }

    public static Transaction createTransaction(Account account, TransactionType type, BigDecimal amount, String description) {
        return Transaction.builder()
                .type(type)
                .amount(amount)
                .description(description)
                .account(account)
                .build();
    }

}