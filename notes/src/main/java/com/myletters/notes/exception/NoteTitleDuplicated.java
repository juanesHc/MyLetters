package com.myletters.notes.exception;

public class NoteTitleDuplicated extends RuntimeException {
    public NoteTitleDuplicated(String message) {
        super(message);
    }
}
