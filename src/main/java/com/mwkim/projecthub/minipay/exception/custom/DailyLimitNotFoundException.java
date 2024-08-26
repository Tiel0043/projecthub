package com.mwkim.projecthub.minipay.exception.custom;

public class DailyLimitNotFoundException extends RuntimeException{
    public DailyLimitNotFoundException(String message) {
        super(message);
    }
}
