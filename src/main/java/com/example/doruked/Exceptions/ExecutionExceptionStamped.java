package com.example.doruked.Exceptions;

import java.util.concurrent.ExecutionException;

public class ExecutionExceptionStamped extends ExecutionException {

    private static int count = 0;
    private int id;

    public ExecutionExceptionStamped(String message, Throwable cause) {
        super(message, cause);
        this.id = count++;
    }

    public int getId(){
        return id;
    }
}
