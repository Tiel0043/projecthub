package com.mwkim.projecthub.minipay.exception.custom;

public class DailyLimitExceedException extends RuntimeException{
    public DailyLimitExceedException(String message) {
        super(message);
    }
}
