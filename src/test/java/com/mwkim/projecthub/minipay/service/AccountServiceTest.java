package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.AccountType;
import com.mwkim.projecthub.minipay.entity.DailyLimit;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.exception.DailyLimitExceedException;
import com.mwkim.projecthub.minipay.exception.InsufficientFundsException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.DailyLimitRepository;
import com.mwkim.projecthub.minipay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DailyLimitRepository dailyLimitRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("메인 계좌에서 적금 계좌로 이체 성공")
    void transferMainToSavings_Success() {
        // given
        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setType(AccountType.MAIN);
        fromAccount.setBalance(new BigDecimal("1000"));

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setType(AccountType.SAVINGS);
        toAccount.setBalance(new BigDecimal("500"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount)); // stub
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        // when
        accountService.transfer(1L, 2L, new BigDecimal("500"));

        // then
        assertThat(fromAccount.getBalance()).isEqualTo(new BigDecimal("500"));
        assertThat(toAccount.getBalance()).isEqualTo(new BigDecimal("1000"));
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("메인 계좌 잔액 부족으로 이체 실패")
    void transferMainToSavings_InsufficientFunds() {
        // given
        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setType(AccountType.MAIN);
        fromAccount.setBalance(new BigDecimal("100"));

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setType(AccountType.SAVINGS);
        toAccount.setBalance(new BigDecimal("500"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        // when & then
        assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("500")))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds in main account");
    }

    @Test
    @DisplayName("메인 계좌 예금 성공")
    void depositToMain_Success() {
        // given
        Account account = new Account();
        account.setId(1L);
        account.setType(AccountType.MAIN);
        account.setBalance(new BigDecimal("1000"));
        account.setUser(new User());

        DailyLimit dailyLimit = new DailyLimit();
        dailyLimit.setUsedAmount(new BigDecimal("1000000"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(dailyLimitRepository.findByUserAndDate(any(User.class), any(LocalDate.class)))
                .thenReturn(Optional.of(dailyLimit));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            return ((TransactionCallback<BigDecimal>) invocation.getArgument(0)).doInTransaction(null);
        });

        // when
        BigDecimal newBalance = accountService.depositToMain(1L, new BigDecimal("500000"));

        // then
        assertThat(newBalance).isEqualTo(new BigDecimal("501000"));
        assertThat(dailyLimit.getUsedAmount()).isEqualTo(new BigDecimal("1500000"));
        verify(accountRepository).save(account);
        verify(dailyLimitRepository).save(dailyLimit);
    }

    @Test
    @DisplayName("일일 예금 한도 초과로 예금 실패")
    void depositToMain_DailyLimitExceeded() {
        // given
        Account account = new Account();
        account.setId(1L);
        account.setType(AccountType.MAIN);
        account.setBalance(new BigDecimal("1000"));
        account.setUser(new User());

        DailyLimit dailyLimit = new DailyLimit();
        dailyLimit.setUsedAmount(new BigDecimal("2800000"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(dailyLimitRepository.findByUserAndDate(any(User.class), any(LocalDate.class)))
                .thenReturn(Optional.of(dailyLimit));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            return ((TransactionCallback<BigDecimal>) invocation.getArgument(0)).doInTransaction(null);
        });

        // when & then
        // 280 + 30으로 310이 되어 한도 발생
        assertThatThrownBy(() -> accountService.depositToMain(1L, new BigDecimal("300000")))
                .isInstanceOf(DailyLimitExceedException.class)
                .hasMessageContaining("Daily deposit limit exceed");
    }

}
