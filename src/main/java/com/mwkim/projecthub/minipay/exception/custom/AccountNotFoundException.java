package com.mwkim.projecthub.minipay.exception.custom;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
