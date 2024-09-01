package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.enums.AccountType;
import com.mwkim.projecthub.minipay.exception.custom.DailyLimitExceedException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private DailyLimitService dailyLimitService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;


    private Account mainAccount;
    private Account savingsAccount;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mainAccount = Account.builder()
                .id(1L)
                .type(AccountType.MAIN) // 메인계좌 300만원 세팅
                .balance(new BigDecimal("3000000"))
                .build();


        savingsAccount = Account.builder()
                .id(2L)
                .type(AccountType.SAVINGS) // 적금 계좌
                .balance(BigDecimal.ZERO)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(mainAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(savingsAccount));

        // DailyLimitService의 기본 동작 설정
        doNothing().when(dailyLimitService).checkAndUpdateDailyLimit(any(Account.class), any(BigDecimal.class));
    }

    @Test
    @DisplayName("충분한 잔액이 있을 때 정상적인 이체")
    void transfer_SufficientBalance_Success() {
        BigDecimal transferAmount = new BigDecimal("1000000"); // 100만원 이체

        accountService.transfer(mainAccount.getId(), savingsAccount.getId(), transferAmount);

        assertThat(mainAccount.getBalance()).isEqualTo(new BigDecimal("2000000"));
        assertThat(savingsAccount.getBalance()).isEqualTo(new BigDecimal("1000000"));
        verify(dailyLimitService, times(1)).checkAndUpdateDailyLimit(eq(mainAccount), eq(transferAmount));
    }

    @Test
    @DisplayName("잔액 부족 시 자동 충전 후 이체")
    void transfer_InsufficientBalance_AutoCharge() {
        BigDecimal transferAmount = new BigDecimal("3500000"); // 350만원 이체 (잔액 초과)

        accountService.transfer(mainAccount.getId(), savingsAccount.getId(), transferAmount);

        // 자동 충전 후 이체 확인
        assertThat(mainAccount.getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(savingsAccount.getBalance()).isEqualTo(new BigDecimal("3500000"));
        verify(dailyLimitService, times(2)).checkAndUpdateDailyLimit(eq(mainAccount), any());
    }

    @Test
    @DisplayName("일일 한도 초과 시 예외 발생")
    void transfer_DailyLimitExceeded() {
        BigDecimal transferAmount = new BigDecimal("3500000"); // 350만원 이체

        doThrow(new DailyLimitExceedException("Daily limit exceeded"))
                .when(dailyLimitService).checkAndUpdateDailyLimit(eq(mainAccount), any());

        assertThatThrownBy(() -> accountService.transfer(mainAccount.getId(), savingsAccount.getId(), transferAmount))
                .isInstanceOf(DailyLimitExceedException.class)
                .hasMessage("Daily limit exceeded");

        // 이체가 실행되지 않았음을 확인
        assertThat(mainAccount.getBalance()).isEqualTo(new BigDecimal("3000000"));
        assertThat(savingsAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("여러 번의 이체 시도에서 일일 한도 적용")
    void transfer_MultipleTransactions_DailyLimitCheck() {
        // Given
        BigDecimal transferAmount1 = new BigDecimal("1000000"); // 100만원 이체 (첫 번째는 성공)
        BigDecimal transferAmount2 = new BigDecimal("2500000"); // 250만원 이체 (두 번째는 실패해야 함)

        Long fromAccountId = 1L;
        Long toAccountId = 2L;


        // When & Then
        // 첫 번째 이체 (성공해야 함)
        assertDoesNotThrow(() -> accountService.transfer(fromAccountId, toAccountId, transferAmount1));

        // 두 번째 이체 (실패해야 함 - 일일 한도 초과)
        doThrow(new DailyLimitExceedException("Daily deposit limit exceeded"))
                .when(dailyLimitService).checkAndUpdateDailyLimit(mainAccount, transferAmount2);

        assertThrows(DailyLimitExceedException.class,
                () -> accountService.transfer(fromAccountId, toAccountId, transferAmount2));

        // Verify
        verify(dailyLimitService, times(1)).checkAndUpdateDailyLimit(mainAccount, transferAmount1);
        verify(dailyLimitService, times(1)).checkAndUpdateDailyLimit(mainAccount, transferAmount2);
        verify(accountRepository, times(2)).findById(fromAccountId);
        verify(accountRepository, times(2)).findById(toAccountId);
    }
}
