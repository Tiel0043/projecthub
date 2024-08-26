package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.DailyLimit;
import com.mwkim.projecthub.minipay.entity.Transaction;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.enums.AccountType;
import com.mwkim.projecthub.minipay.exception.custom.AccountNotFoundException;
import com.mwkim.projecthub.minipay.exception.custom.InvalidAccountTypeException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {

    // 일일 한도 금액 300만원, 자동 충전은 1만원 단위로 한다.
    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = new BigDecimal("3000000");
    private static final BigDecimal AUTO_CHARGE_UNIT = new BigDecimal("10000");

    private final AccountRepository accountRepository;
    private final DailyLimitService dailyLimitService;
    private final TransactionRepository transactionRepository;

    // 메인계좌 생성
    public Account createMainAccount(User user) {
        log.info("계좌 생성!");
        DailyLimit dailyLimit = DailyLimit.builder().build();

        Account account = Account.builder()
                .user(user)
                .type(AccountType.MAIN)
                .dailyLimit(dailyLimit)
                .build();
        user.addAccount(account);
        return accountRepository.save(account);
    }

    // 적금계좌 생성
    public Account createSavingsAccount(User user) {
        Account account = Account.builder()
                .user(user)
                .type(AccountType.SAVINGS)
                .build();

        user.addAccount(account);
        return accountRepository.save(account);
    }


    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {

        try{
            Account fromAccount = getAccountById(fromAccountId);
            Account toAccount = getAccountById(toAccountId);

            // totalAmount : 충전 된 금액
            BigDecimal totalAmount = ensureSufficientBalance(fromAccount, amount);

            // 일일 한도 검사
            if (fromAccount.isMainAccount()) {
                dailyLimitService.checkAndUpdateLimit(fromAccount, totalAmount);
            }

            executeTransfer(fromAccount, toAccount, amount);
            recordTransaction(fromAccount, toAccount, amount);
        } catch (OptimisticLockingFailureException e) {
        // 재시도 로직 또는 예외 처리
        log.error("Optimistic locking failed", e);
        throw new RuntimeException("Transfer failed due to concurrent modification");
    }


    }


    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다: " + accountId));
    }

    // 계좌 자동충전
    private BigDecimal ensureSufficientBalance(Account account, BigDecimal amount) {
        BigDecimal balance = account.getBalance();
        if (balance.compareTo(amount) < 0) {
            BigDecimal chargeAmount = calculateChargeAmount(amount.subtract(balance));
            account.deposit(chargeAmount);
            log.info("자동 충전: {}원", chargeAmount);
            return amount.add(chargeAmount);
        }
        return amount;
    }

    private BigDecimal calculateChargeAmount(BigDecimal neededAmount) {
        // 올림 처리해서 10,000원씩 충전
        return neededAmount.divide(new BigDecimal("10000"), 0, RoundingMode.UP)
                .multiply(new BigDecimal("10000"));
    }

    private void executeTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    private void recordTransaction(Account fromAccount, Account toAccount, BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .build();
        transactionRepository.save(transaction);
    }

    // 메인계좌에서 적금계좌로 이체
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void transferToSavings(Long mainAccountId, Long savingsAccountId, BigDecimal amount) {
        Account mainAccount = accountRepository.findById(mainAccountId)
                .orElseThrow(() -> new AccountNotFoundException("메인 계좌를 찾을 수 없습니다."));
        Account savingsAccount = accountRepository.findById(savingsAccountId)
                .orElseThrow(() -> new AccountNotFoundException("적금 계좌를 찾을 수 없습니다."));

        if (mainAccount.getType() != AccountType.MAIN || savingsAccount.getType() != AccountType.SAVINGS) {
            throw new InvalidAccountTypeException("올바른 계좌 유형이 아닙니다.");
        }

        transfer(mainAccountId, savingsAccountId, amount);
    }

}
