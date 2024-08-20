package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.AccountType;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.exception.UserNotFoundException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public User registerUser(String username) {
        User user = new User();
        user.setUsername(username);
        user = userRepository.save(user);

        Account mainAccount = new Account();
        mainAccount.setUser(user);
        mainAccount.setType(AccountType.MAIN);
        mainAccount.setBalance(BigDecimal.ZERO);
        accountRepository.save(mainAccount);

        return user;
    }

    public Account createSavingsAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Account savingsAccount = new Account();
        savingsAccount.setUser(user);
        savingsAccount.setType(AccountType.SAVINGS);
        savingsAccount.setBalance(BigDecimal.ZERO);
        return accountRepository.save(savingsAccount);
    }

}
