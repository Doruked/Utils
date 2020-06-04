package com.example.doruked.Responder;


/**
 * This interface represents an {@code observer} of some context({@link T}). It is expected
 * that this observer will be notified/supplied some {@code context}, to which it generates it's response.
 *
 * @param <T> the type to process/notify this observer of
 * @param <R> the type returned from processing a given {@link T}
 */
public interface Observer<T,R> {


    /**
     * Processes the specified {@code context} and produces a response.
     *
     * @param context the information to be processed
     * @return a the result of processing the specified {@code context}
     * @throws NullPointerException if context is null and null is not permitted
     * @implSpec The response should be, generally, a response to the received {@code context}.
     * However, it is possible to have an observer return a constant response.
     * @throws InterruptedException if the executing thread was interrupted while processing
     */
    R process(T context) throws InterruptedException;


}
