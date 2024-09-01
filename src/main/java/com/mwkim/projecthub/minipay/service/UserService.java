package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.enums.AccountType;
import com.mwkim.projecthub.minipay.exception.custom.UserAlreadyExistsException;
import com.mwkim.projecthub.minipay.exception.custom.UserNotFoundException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    // id로 회원 조히
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    // 회원 계좌 조회
    public List<Account> getUserAccounts(Long userId) {
        User user = getUserById(userId);
        return user.getAccounts();
    }


    public User registerUser(Long id, String username) {
        if (userRepository.findById(id).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }

        User user = User.createUser(username);
        user = userRepository.save(user);

        // 메인 계좌 자동 생성
//        accountService.createAccount(user.getId(), AccountType.MAIN, new BigDecimal("3000000"));
        return user;
    }

}
