package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.DailyLimit;
import com.mwkim.projecthub.minipay.exception.custom.DailyLimitExceedException;
import com.mwkim.projecthub.minipay.exception.custom.DailyLimitNotFoundException;
import com.mwkim.projecthub.minipay.repository.DailyLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DailyLimitService {

    private final DailyLimitRepository dailyLimitRepository;

    public void checkAndUpdateLimit(Account account, BigDecimal amount) {
        DailyLimit dailyLimit = account.getDailyLimit();
        if (dailyLimit == null) {
            throw new DailyLimitNotFoundException("일일 한도 정보를 찾을 수 없습니다.");
        }

        if (isNewDay(dailyLimit)) {
            dailyLimit.resetLimit();
        }

        if (!dailyLimit.checkWithDraw(amount)) {
            throw new DailyLimitExceedException("일일 출금 한도를 초과했습니다.");
        }

        dailyLimit.addUsedAmount(amount);
        dailyLimitRepository.save(dailyLimit);
        log.info("일일 한도 갱신: {}원", amount);
    }

    private boolean isNewDay(DailyLimit dailyLimit) {
        return !dailyLimit.getDate().equals(LocalDate.now());
    }
}
