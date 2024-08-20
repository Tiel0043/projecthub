package com.mwkim.projecthub.minipay.exception;

public class DailyLimitExceedException extends RuntimeException{
    public DailyLimitExceedException(String message) {
        super(message);
    }
}
