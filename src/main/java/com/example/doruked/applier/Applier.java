package com.example.doruked.applier;

import com.example.doruked.exceptions.ExecutionExceptionStamped;
import com.example.doruked.exceptions.InterruptedExceptionStamped;

import java.util.concurrent.ExecutionException;

/**
 * This class represents a particular way of applying some effect over some input.
 *
 * @param <TInput>    the input used by effects
 * @param <TEffect>   the effect applied to input or it's members
 * @param <ROptional> the type returned after applying effects
 * @apiNote This is useful when you have a known effect and input, but there are rules or additional
 * procedures required to execute the process, not achievable by simplr iteration and applying
 * the {@link TEffect} to each {@code element} within {@link TInput}.
 * @implSpec  It reasonably entails that {@link TInput} is a collection of {@code elements}.
 * and those {@code elements} are accepted {@link TEffect}. Such constraints, however, are not enforced.
 */
public interface Applier<TInput, TEffect, ROptional> {


    /**
     * This method accepts an input and execute the specified {@code effect} on that input.
     *
     * @param input the input used by effects
     * @param effect the effect applied to input or it's members
     * @return a value specified by the implementation with implementation specific meaning
     * @throws ExecutionExceptionStamped   if applying the effect to the input results in an exception
     * @throws InterruptedExceptionStamped if the current thread was interrupted during execution
     * @throws NullPointerException        if input or effect is null and null is not permitted
     * @implSpec this method lends it to having implementation specific rules for how
     * effects are applied to some input. For any given implementation is recommending that one
     * examines it's documented behavior.
     */
    ROptional apply(TInput input, TEffect effect) throws InterruptedException, ExecutionException;

}








