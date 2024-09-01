package com.mwkim.projecthub.minipay.exception.custom;

public class SettlementNotFoundException extends RuntimeException{
    public SettlementNotFoundException(String message) {
        super(message);
    }
}
