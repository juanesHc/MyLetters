package com.myletters.auth.exception;

public class InvalidAccountDataException extends RuntimeException {

    public InvalidAccountDataException(String message) {
        super(message);
    }
}
