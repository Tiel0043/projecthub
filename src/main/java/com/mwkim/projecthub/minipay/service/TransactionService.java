package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.Transaction;
import com.mwkim.projecthub.minipay.enums.TransactionType;
import com.mwkim.projecthub.minipay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transacionRepository;


    public Transaction createTransaction(Account account, TransactionType type, BigDecimal amount, String description) {
        Transaction transaction = Transaction.createTransaction(account, type, amount, description);
        return transacionRepository.save(transaction);
    }

}
