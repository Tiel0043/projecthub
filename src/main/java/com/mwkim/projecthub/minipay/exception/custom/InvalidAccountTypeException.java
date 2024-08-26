package com.mwkim.projecthub.minipay.exception.custom;

public class InvalidAccountTypeException extends RuntimeException{
    public InvalidAccountTypeException(String message) {
        super(message);
    }
}
