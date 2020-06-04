package com.example.doruked;

public interface Effect<T> {


    void apply(T target) throws InterruptedException;

}
