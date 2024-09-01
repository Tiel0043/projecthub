package com.mwkim.projecthub.minipay.exception.custom;



public class ParticipantNotFoundException extends RuntimeException{
    public ParticipantNotFoundException(String message) {
        super(message);
    }
}
