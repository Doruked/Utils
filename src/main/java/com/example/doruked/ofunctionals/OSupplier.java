package com.example.doruked.ofunctionals;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@inheritDoc}
 *
 * @implSpec is a supplier with an optional return value
 * @apiNote I typically use this to indicate that the supplier
 * isn't focused on "supplying". More importantly, it does other things
 */
public interface OSupplier<T> extends Supplier<Optional<T>> { }
