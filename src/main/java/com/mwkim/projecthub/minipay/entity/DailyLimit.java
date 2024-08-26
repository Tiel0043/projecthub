package com.mwkim.projecthub.minipay.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "dailyLimit")
    private Account account;

    private LocalDate date = LocalDate.now();

    private BigDecimal usedAmount = BigDecimal.ZERO;

    private BigDecimal maxLimit = new BigDecimal("3000000");

    // 인출 가능여부 출력
    public boolean checkWithDraw(BigDecimal amount) {
        System.out.println("한도 검사!");
        System.out.println("amount = " + usedAmount.add(amount));
        return usedAmount.add(amount).compareTo(maxLimit) <= 0;
    }

    // 사용 금액 누적합
    public void addUsedAmount(BigDecimal amount) {
        this.usedAmount = this.usedAmount.add(amount);
    }

    // 한도 초기화
    public void resetLimit() {
        this.date = LocalDate.now();
        this.usedAmount = BigDecimal.ZERO;
    }

    @Builder
    public DailyLimit(Long id, Account account, LocalDate date, BigDecimal usedAmount, BigDecimal maxLimit) {
        this.id = id;
        this.account = account;

        this.date = date != null ? date : LocalDate.now();
        this.usedAmount = usedAmount != null ? usedAmount : BigDecimal.ZERO;
        this.maxLimit = maxLimit != null ? maxLimit : new BigDecimal("3000000");
    }
}
