package com.mwkim.projecthub.minipay.controller;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/minipay/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("{accountId}/deposit")
    public ResponseEntity<BigDecimal> deposit(@PathVariable("accountId") Long accountId,
                                              @RequestParam("amount") BigDecimal amount) {
        BigDecimal newBalance = accountService.depositToMain(accountId, amount);
        return ResponseEntity.ok(newBalance);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @RequestParam("fromAccountId") Long fromAccountId,
            @RequestParam("toAccountId") Long toAccountId,
            @RequestParam("amount") BigDecimal amount) {
        accountService.transfer(fromAccountId, toAccountId, amount);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable("accountId") Long accountId) {
        Account account = accountService.getAccount(accountId);
        return ResponseEntity.ok(account);
    }
}
