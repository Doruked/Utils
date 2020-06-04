package com.example.doruked.opfunctionals;

import java.util.Optional;
import java.util.function.Function;

/**
 * {@inheritDoc}
 *
 * @implSpec is a unary operator with an optional return value
 */
public interface OUnaryOperator<T> extends Function<T, Optional<T>> { }
