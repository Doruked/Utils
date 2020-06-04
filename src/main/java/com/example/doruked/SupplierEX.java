package com.example.doruked;


/**
 *
 *
 *
 * @param <R> the type returned by operation
 * @param <TException> the type of exception thrown by operation
 */

//don't feel the need to actually wrap as optional given that when people
//call, it will be through a lambda, and in those case, you should know if it has a return
public interface SupplierEX<R, TException extends Exception> {

    R get() throws TException;
}
