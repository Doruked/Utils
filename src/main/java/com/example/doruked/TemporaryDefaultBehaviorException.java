package com.example.doruked;

public class TemporaryDefaultBehaviorException extends Exception {

    public TemporaryDefaultBehaviorException(String message) {
        super(message);
    }

    public TemporaryDefaultBehaviorException() {
        super("Method might change or be removed");
    }
}
