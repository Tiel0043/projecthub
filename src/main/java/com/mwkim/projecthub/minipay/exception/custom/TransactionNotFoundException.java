package com.mwkim.projecthub.minipay.exception.custom;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
