package com.example.doruked.applier.resumable;


import com.example.doruked.applier.Applier;
import com.example.doruked.exceptions.ExecutionExceptionStamped;
import com.example.doruked.exceptions.InterruptedExceptionStamped;

import java.util.concurrent.ExecutionException;

/**
 * This class represents an applier with a particular means of reapplying failed applications.
 * Failed applications may be reapplied by providing a {@link TContext} that contains the relevant
 * information for reapplication.
 *
 * @param <TInput>   the input used by effects
 * @param <TEffect>  the effect applied to input or it's members
 * @param <ROptional> the type return when after applying effects
 * @param <TContext> the context that can be used to resume execution from specific points
 * @implSpec  it's possible to use this interface for purposes other than strictly "resuming".
 * If {@link #applyFrom(Object)} is invoked in isolation of any past failure or interruption etc,
 * the method will still work. The only important detail is that is applies appropriately given
 * the presented {@link TContext}
 */
public interface ContextualApplier<TInput, TEffect, ROptional, TContext> extends Applier<TInput, TEffect, ROptional> {


    /**
     * This method accepts a context that specifies a position within in {@link #apply(Object, Object)} execution,
     * and attempts to execute as if starting from that position. The context also carries the additional info n
     * ended such as the {@code effect} and {@code input} used.
     *
     * @param from the context containing information used to resume application from a previously failed point
     * @return a value specified by the implementation with implementation specific meaning
     * @throws ExecutionExceptionStamped   if applying the effect to the input results in an exception
     * @throws InterruptedExceptionStamped if the current thread was interrupted during execution
     * @throws NullPointerException        if input or effect is null and null is not permitted
     * @implSpec this method does not specify from where in execution it will be resumable. That depends
     * on the implementation. However, a notable point is to have it make sense. Resuming from where
     * an {@code exception} was thrown seems reasonable.
     */
    ROptional applyFrom(TContext from) throws InterruptedException, ExecutionException;


}
