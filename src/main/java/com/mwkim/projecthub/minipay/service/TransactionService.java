package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.Transaction;
import com.mwkim.projecthub.minipay.enums.TransactionType;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;


    public Transaction createTransaction(Account account, TransactionType type, BigDecimal amount, String description) {
        Transaction transaction = Transaction.createTransaction(account, type, amount, description);
        return transactionRepository.save(transaction);
    }



}
