package com.mwkim.projecthub.minipay.service;


import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.exception.custom.DailyLimitExceedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DailyLimitService {

    private AccountService accountService;

    private static final BigDecimal DAILY_LIMIT = new BigDecimal("3000000");

    public void checkAndUpdateDailyLimit(Account account, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();

        // 날짜가 바꼈다면, 일일한도 갱신
        if (account.getLastWithdrawalReset().toLocalDate().isBefore(now.toLocalDate())) {
            account.updateDailyUseAmount(BigDecimal.ZERO);
            account.updateLastWithdrawalReset(now);
        }

        // 일일한도가 초과되었는지 확인
        if (account.getDailyUseAmount().add(amount).compareTo(DAILY_LIMIT) > 0) {
            throw new DailyLimitExceedException("Daily deposit limit exceeded");
        }

        // 일일 사용량 증가
        account.updateDailyUseAmount(account.getDailyUseAmount().add(amount));
    }


}
