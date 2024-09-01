package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.Transaction;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.enums.AccountType;
import com.mwkim.projecthub.minipay.enums.TransactionType;
import com.mwkim.projecthub.minipay.exception.custom.AccountNotFoundException;
import com.mwkim.projecthub.minipay.exception.custom.CollisionException;
import com.mwkim.projecthub.minipay.exception.custom.InsufficientBalanceException;
import com.mwkim.projecthub.minipay.exception.custom.UserNotFoundException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final UserService userService;
    private final DailyLimitService dailyLimitService;

    private static final BigDecimal AUTO_CHARGE_UNIT = new BigDecimal("10000"); // 일일 충전 단위

    public Account createAccount(Long userId, AccountType type, BigDecimal dailyLimitAmount) {
        User user = userService.getUserById(userId);

        // 계좌 생성 로직
        Account account = Account.createAccount(user, type, BigDecimal.ZERO, dailyLimitAmount);
        user.addAccount(account);
        return accountRepository.save(account);
    }

    // 입금 메소드
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {

        try {
            // 출입금할 계좌 조회
            Account fromAccount = getAccountById(fromAccountId);
            Account toAccount = getAccountById(toAccountId);
            log.debug("서비스 호출!!");
            log.debug("Transfer from {} to {}: {}", fromAccountId, toAccountId, amount);

            // 자동 충전 로직
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                BigDecimal chargeAmount = amount.subtract(fromAccount.getBalance())
                        .divide(AUTO_CHARGE_UNIT, 0, BigDecimal.ROUND_UP) // 올림처리해서 만원 단위로 충전
                        .multiply(AUTO_CHARGE_UNIT);
                dailyLimitService.checkAndUpdateDailyLimit(fromAccount, chargeAmount);  // 자동 충전에 대한 한도 체크
                deposit(fromAccount, chargeAmount); // 보내는 계좌 충전
            }

            // 이체 로직
            dailyLimitService.checkAndUpdateDailyLimit(fromAccount, amount);
            withdraw(fromAccount, amount); // 보내는 계좌에서 출금
            deposit(toAccount, amount);  // 보내는 계좌에서 입금

            // 로그 작성
            Transaction transaction = Transaction.createTransaction(fromAccount, TransactionType.TRANSFER,
                    amount, "송금: " + fromAccountId + "->" + toAccountId);
            fromAccount.addTransaction(transaction);
            log.debug("Transfer completed. New balance for {}: {}", fromAccountId, fromAccount.getBalance());
        } catch (OptimisticLockingFailureException e) { // 낙관적 락 발생 시
            throw new CollisionException("trasfer collison");
        }


    }


    public void deposit(Account account, BigDecimal amount) {
        account.updateBalance(account.getBalance().add(amount));
        Transaction transaction = Transaction.createTransaction(account,TransactionType.DEPOSIT, amount, "Deposit");
        account.addTransaction(transaction);
    }
    // 출금 메소드

    public void withdraw(Account account, BigDecimal amount) {
        // 현재 계좌가 충분한지 확인
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // 계좌 차감
        account.updateBalance(account.getBalance().subtract(amount));
        Transaction transaction = Transaction.createTransaction(account, TransactionType.WITHDRAW, amount, "Withdraw");
        account.addTransaction(transaction);
    }


    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
    }

}
