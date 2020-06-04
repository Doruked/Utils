package com.example.doruked;

public interface ExceptionalLambda<T extends Exception> {


    void execute() throws T;



}
