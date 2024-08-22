package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.AccountType;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.exception.UserNotFoundException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("새 사용자 등록 및 메인 계좌 생성 성공")
    void registerUser_Success() {
        // given
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        User registeredUser = userService.registerUser("testuser");

        // then
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(argThat(account ->
                account.getUser().equals(user) &&
                        account.getType() == AccountType.MAIN &&
                        account.getBalance().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @Test
    @DisplayName("기존 사용자의 적금 계좌 생성 성공")
    void createSavingsAccount_Success() {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Account savingsAccount = userService.createSavingsAccount(1L);

        // then
        assertThat(savingsAccount).isNotNull();
        assertThat(savingsAccount.getUser()).isEqualTo(user);
        assertThat(savingsAccount.getType()).isEqualTo(AccountType.SAVINGS);
        assertThat(savingsAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 적금 계좌 생성 실패")
    void createSavingsAccount_UserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.createSavingsAccount(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

}
