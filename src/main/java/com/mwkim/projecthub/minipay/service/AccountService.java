package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.AccountType;
import com.mwkim.projecthub.minipay.entity.DailyLimit;
import com.mwkim.projecthub.minipay.exception.AccountNotFoundException;
import com.mwkim.projecthub.minipay.exception.DailyLimitExceedException;
import com.mwkim.projecthub.minipay.exception.InsufficientFundsException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.DailyLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final DailyLimitRepository dailyLimitRepository;
    private final TransactionTemplate transactionTemplate;

    // 한 트랜잭션 내에서 동일한 결과를 보장하지만, 새로운 레코드가 추가되면 부정합이 생길 수 있다.(Phantom Read가능)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException("From account not found"));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException("To account not found"));

        // 메인계좌와 적금 계좌는 다른 비즈니스 규칙을 가질 수 있기에 분리했다.
        // ex) 적금 계좌에서 메인 계좌로의 이체는 특정 조건(만기일 등)을 충족해야 할 수 있다.
        if (fromAccount.getType() == AccountType.MAIN && toAccount.getType() == AccountType.MAIN) {
            transferMainToSavings(fromAccount, toAccount, amount);
        } else if (fromAccount.getType() == AccountType.SAVINGS && toAccount.getType() == AccountType.SAVINGS) {
            transferSavingsToMain(fromAccount, toAccount, amount);
        } else {
            throw new IllegalStateException("Invalid transfer between account types");
        }

    }

    // 메인계좌 -> 적금 계좌
    private void transferMainToSavings(Account mainAccount, Account savingsAccount, BigDecimal amount) {
        if (mainAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in main account");
        }

        mainAccount.setBalance(mainAccount.getBalance().subtract(amount)); // 메인계좌 - 금액
        savingsAccount.setBalance(savingsAccount.getBalance().add(amount)); // 적금계좌 + 금액

        accountRepository.save(mainAccount);
        accountRepository.save(savingsAccount);
    }

    // 적금계좌 -> 메인계좌
    private void transferSavingsToMain(Account savingsAccount, Account mainAccount, BigDecimal amount) {
        if (savingsAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in savings account");
        }

        savingsAccount.setBalance(savingsAccount.getBalance().subtract(amount));
        mainAccount.setBalance(mainAccount.getBalance().add(amount));

        accountRepository.save(savingsAccount);
        accountRepository.save(mainAccount);
    }

    // 메인계좌 예금
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BigDecimal depositToMain(Long accountId, BigDecimal amount) {
        return transactionTemplate.execute(status -> {
            // 계좌 조회
            Account account = accountRepository.findById(accountId).
                    orElseThrow(() -> new AccountNotFoundException("Account not found"));

            // 계좌 유형 검증
            if (account.getType() != AccountType.MAIN) {
                throw new IllegalArgumentException("Deposit is only allowed to main account");
            }

            // 계좌 일일 한도 조회, 없으면 새로운 객체 생성
            DailyLimit dailyLimit = dailyLimitRepository.findByUserAndDate(account.getUser(), LocalDate.now())
                    .orElse(new DailyLimit(null, account.getUser(), LocalDate.now(), BigDecimal.ZERO));

            // 일일 한도 확인
            BigDecimal newUsedAmount = dailyLimit.getUsedAmount().add(amount);
            if (newUsedAmount.compareTo(new BigDecimal("3000000")) > 0) {
                throw new DailyLimitExceedException("Daily deposit limit exceed");
            }

            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);

            dailyLimit.setUsedAmount(newUsedAmount);
            dailyLimitRepository.save(dailyLimit);

            return account.getBalance(); // transactionTemplate이 제네릭<T> 반환을 요구하기에 null 리턴
        });
    }

    @Transactional(readOnly = true)
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
    }
}
