package com.example.doruked.responder.futured;

import com.example.doruked.responder.Mediator;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This interface is an {@link Mediator} that supports returning {@link Future<TResponse>} or retrieving it's result
 * and instead return {@link TResponse}.
 *
 * @param <TContext> the type of information handled by this mediator
 * @param <TResponse> the type of response produced by observers
 */
public interface FutureMediator<TContext, TResponse, TOptional> extends Mediator<TContext, Future<TResponse>, TOptional> {

    /**
     * Works like {@link #getResponse()}, but instead waits for the returned {@link Future} to produce a result.
     *
     * @return the result of a {@link Future} generated from a previous call to {@link #notify(Object)}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     */
    TResponse awaitMessage() throws InterruptedException, ExecutionException;

    /**
     * Works like {@link #getResponse()}, but instead waits for the returned {@link Future} to produce a result.
     * On each retrieval, the method fully waits for the specified {@code time} and {@code unit}.
     * If an attempt times out, that future is handled according to according to the implementation,
     * and the method moves on if possible.
     *
     * @param timeout the maximum time to wait for each response
     * @param unit the time unit of the timeout argument
     * @return the result of a {@link Future} generated from a previous call to {@link #notify(Object)}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     * @throws TimeoutException if the wait timed out
     */
    TResponse awaitMessage(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Works like {@link #getAllResponse()} ()}, but instead waits for the {@link Future} to produce a result.
     *
     * @return the result of all {@link Future} generated from a previous call to {@link #notify(Object)}
     * @throws InterruptedException
     * @throws ExecutionException
     */
    List<TResponse> awaitAllMessages() throws InterruptedException, ExecutionException;

    /**
     * Similar to {@link #getAllResponse()} but with notable differences.
     * The specified {@code maximum} represents how many times this method will attempt to retrieve a response.
     * On each retrieval, the method fully waits for the specified {@code time} and {@code unit}.
     * If an attempt times out, that future is handled according to according to the implementation,
     * and the method moves on if possible.
     *
     * @param timeout the maximum time to wait for each response
     * @param unit the time unit of the timeout argument
     * @param unit the time unit to use
     * @return the result of {@link Future}s generated from a previous call to {@link #notify(Object)}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     * @throws TimeoutException if the wait timed out
     */
    List<TResponse> awaitAllMessages(int maximum, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;



    interface Single<TContext, TResponse> extends Mediator<TContext, Future<TResponse>, Future<TResponse>> {}

}
