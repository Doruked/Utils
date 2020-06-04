package com.example.doruked;

public class DefaultBehaviorException  extends UnsupportedOperationException{

    public DefaultBehaviorException() {
        super("Method might be subject to change or removal");
    }

    public DefaultBehaviorException(String message) {
        super(message);
    }
}
