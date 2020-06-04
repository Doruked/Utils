package com.example.doruked.Exceptions;

public class InterruptedExceptionStamped extends InterruptedException {

    private static int count = 0;
    private int id;

    public InterruptedExceptionStamped(String message) {
        super(count + " " + message);
        this.id = count;
        count++;
    }

    public int getId(){
        return id;
    }
}
