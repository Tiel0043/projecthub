package com.mwkim.projecthub.minipay.entity;

import com.mwkim.projecthub.minipay.enums.AccountType;
import com.mwkim.projecthub.minipay.exception.custom.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    // 계좌 별 일일 한도
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_limit_id")
    private DailyLimit dailyLimit;

    private BigDecimal balance;

    // 낙관적 락
    @Version
    private Long version;


    // 예금
    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    // 출금
    public void withdraw(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("잔액이 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }

    // 주계좌 인지 확인
    public boolean isMainAccount() {
        return this.type == AccountType.MAIN;
    }


    void addUser(User user) {
        this.user = user;
    }

    @Builder
    public Account(Long id, User user, AccountType type, DailyLimit dailyLimit, BigDecimal balance, Long version) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.dailyLimit = dailyLimit;
        this.balance = balance != null ? balance : BigDecimal.ZERO;  // 기본값 설정
        this.version = version;
    }
}
