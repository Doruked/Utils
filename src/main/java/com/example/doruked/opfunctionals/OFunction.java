package com.example.doruked.opfunctionals;

import java.util.Optional;
import java.util.function.Function;

/**
 * {@inheritDoc}
 *
 * @implSpec is a function with an optional return value
 * @apiNote This interface is perhaps more of like a consumer
 * that "may" return a result.
 */
public interface OFunction<T,R> extends Function<T, Optional<R>> { }
