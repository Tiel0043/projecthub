package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.DailyLimit;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AccountService accountService;

    public User createUser(String username, String email) {

        User user = User.builder()
                .username(username)
                .build();

        user = userRepository.save(user);

        Account mainAccount = accountService.createMainAccount(user);
        user.addAccount(mainAccount);

        return user;
    }

}
