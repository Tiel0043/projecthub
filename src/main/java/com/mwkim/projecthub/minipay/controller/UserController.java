package com.mwkim.projecthub.minipay.controller;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/minipay/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestParam("username") String username) {
        User user = userService.registerUser(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/savings-account")
    public ResponseEntity<Account> createSavingsAccount(@PathVariable("userId") Long userId) {
        Account account = userService.createSavingsAccount(userId);
        return ResponseEntity.ok(account);
    }
}
