package com.example.doruked.responder.futured;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This interface represents a {@link FutureMediator} that stores any failed {@code futures} that it attempted
 * to retrieve the result from. These failed {@code futures} are made available in {@link #getFailed()} and {@link #getAllFailed()}
 *
 * @param <TContext>  the type of information handled by this mediator
 * @param <TResponse> the type of response produced by observers
 * @param <TOptional> the type returned after notification
 * @implSpec A failed {@code future} would be one that threw a {@code checked exception}.
 * However, an implementation may specify otherwise.
 */
public interface StoredFutureMediator<TContext, TResponse, TOptional> extends FutureMediator<TContext, TResponse, TOptional> {

    /**
     * @return an entry containing a previous failed {@code future} and the exception it threw
     * @implSpec (1) this method does not specify which failed {@code future} is returned if there are multiple
     * (2) The returned {@code future} are expected be to removed from this object's collection
     */
    Entry<Future<TResponse>, ExecutionException> getFailed();

    /**
     * @return a list entries containing all previously failed {@code future} and the exception it threw
     * @implSpec the returned {@code futures} are expected to be removed from this object's collection
     */
    List<Entry<Future<TResponse>, ExecutionException>> getAllFailed();

}
