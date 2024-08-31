package com.mwkim.projecthub.minipay.entity;


import com.mwkim.projecthub.minipay.enums.AccountType;
import com.mwkim.projecthub.minipay.exception.custom.DailyLimitExceedException;
import com.mwkim.projecthub.minipay.exception.custom.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import javax.naming.LimitExceededException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private BigDecimal balance;

    private BigDecimal dailyLimitAmount;

    private BigDecimal dailyUseAmount; // 매번 출금 내역을 DB에서 조회해 합산하는 것보다, 메모리에 누적액을 유지하자.

    private LocalDateTime lastWithdrawalReset; // 마지막 리셋 시간

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @Builder
    public Account(Long id, AccountType type, BigDecimal balance, BigDecimal dailyLimitAmount) {
        this.id = id;
        this.type = type;
        this.balance = balance;
        this.dailyLimitAmount = dailyLimitAmount;
        this.dailyUseAmount = BigDecimal.ZERO;
        this.lastWithdrawalReset = LocalDateTime.now();
    }


    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        transaction.addAccount(this);
    }

    // factory-method
    public static Account createAccount(User user, AccountType type, BigDecimal balance, BigDecimal dailyLimitAmount) {
        Account account = Account.builder()
                .type(type)
                .balance(balance)
                .dailyLimitAmount(dailyLimitAmount)
                .build();

        account.addUser(user);
        return account;
    }

    public void addUser(User user) {
        this.user = user;
    }

    public void updateDailyUseAmount(BigDecimal amouont) {
        this.dailyLimitAmount = amouont;
    }

    public void updateLastWithdrawalReset(LocalDateTime time) {
        this.lastWithdrawalReset = time;
    }

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
