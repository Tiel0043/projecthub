package com.mwkim.projecthub.minipay.exception.custom;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
