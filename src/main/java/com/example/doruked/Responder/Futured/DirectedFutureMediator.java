package com.example.doruked.Responder.Futured;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This interface represents a {@link FutureMediator} that optionally keeps any {@code future} that threw
 * {@link ExecutionException}. A {@code future} can be made available for later retrieval by specifying {@code true}
 * for the argument {@code keepIfExecutionFails}.
 *
 * @implSpec (1)This interface does not specify how an implementation should "keep" it's response
 * (2) This interface was not intended for general use. Rather, it exists to help reduce the code needed to implement
 * other {@code mediator} behavior requiring similar logic.
 *
 * @param <TContext>  the type of information handled by this mediator
 * @param <TResponse> the type of response produced by observers
 * @param <TOptional> the type returned after notification
 * @see AbstractFutureMediator.Directed
 */
public interface DirectedFutureMediator<TContext, TResponse, TOptional> extends FutureMediator<TContext, TResponse, TOptional> {

    /**
     * Attempts to retrieve the result from a contained response of an {@code observer}, blocking if not available.
     * If no response exists, this method is not expected to wait and an {@code null} should be returned if so.
     *
     * @param keepIfExecutionFails whether the {@code future} should be kept if it throws {@link ExecutionException}
     * @return the result from a response of an {@code observer} or null if no responses were available
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if retrieving the response threw an exception
     */
    TResponse awaitMessage(boolean keepIfExecutionFails) throws InterruptedException, ExecutionException;

    /**
     * Attempts to retrieve the result from all contained response of an {@code observers}, blocking if not available.
     * If no response exists, this method is not expected to wait and an empty {@code list} should be returned if so.
     *
     * @param keepIfExecutionFails whether the {@code future} should be kept if it throws {@link ExecutionException}
     * @return the result from all response of an {@code observer} or an empty list if no responses were available
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if retrieving the response threw an exception
     * @implSpec this method may choose to either stop or continue when a {@code future} throws {@link ExecutionException}
     * However, the choice should be consistent.
     */
    List<TResponse> awaitAllMessages(boolean keepIfExecutionFails) throws InterruptedException, ExecutionException;

    /**
     * Works like
     * {@link #awaitAllMessages(boolean)}, except you specify whether execution
     * stops if {@link ExecutionException} is thrown
     * @param stopOnExecutionFailure whether retrieval should stop if {@link ExecutionException} is thrown
     */
    List<TResponse> awaitAllMessages(boolean keepIfExecutionFails, boolean stopOnExecutionFailure) throws InterruptedException, ExecutionException;

//timed

    /**
     * Works like
     * {@link #awaitAllMessages(boolean)}, except it's a timed wait
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     */
    TResponse awaitMessage(boolean keepIfExecutionFails, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * This method attempts to get the result from all contained responses.
     * On each attempt, the method fully waits for the specified {@code time} and {@code unit}.
     *
     * @param keepIfExecutionFails whether the {@code future} should be kept if it throws {@link ExecutionException}
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the retrieved results or empty list if no responses were available
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if retrieving the response threw an exception
     */
    List<TResponse> awaitAllMessages(boolean keepIfExecutionFails, long timeout, TimeUnit unit)  throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Works like
     * {@link #awaitAllMessages(boolean, long, TimeUnit)}, except you specify whether execution
     * stops if {@link ExecutionException} is thrown
     * @param stopOnExecutionFailure whether retrieval should stop if {@link ExecutionException} is thrown
     */
    List<TResponse> awaitAllMessages(boolean keepIfExecutionFails, boolean stopOnExecutionFailure, long timeout, TimeUnit unit)  throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * This method attempts to retrieve responses, and makes attempts equal to the specified {@code maximum}
     * On each attempt, the method fully waits for the specified {@code time} and {@code unit}.
     * If an attempt times out, that future is handled according to the implementation.
     *
     * @param maximum the max attempts to to wait
     * @param keepIfExecutionFails whether the {@code future} should be kept if it throws {@link ExecutionException}
     * @param stopOnExecutionFailure whether retrieval should stop if {@link ExecutionException} is thrown
     * @return the retrieved results or empty list if no responses were available
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if retrieving the response threw an exception
     */
    List<TResponse> awaitAllMessages(int maximum, boolean keepIfExecutionFails, boolean stopOnExecutionFailure) throws InterruptedException, ExecutionException;

}
