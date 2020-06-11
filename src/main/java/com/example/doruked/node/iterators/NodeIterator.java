package com.example.doruked.node.iterators;

import java.util.Iterator;

/**
 * An iterator with additional operations for nodes.
 *
 * @param <E> the type of nodes traversed
 * @implSpec this interface is still in development and more methods will be added in the future
 * depending on the time frame, they will be implemented as default unsupported.
 */
public interface NodeIterator<E> extends Iterator<E> {

    /**
     * Sets the last returned node's {@code data} to be {@code null}
     *
     * @throws IllegalStateException if the {@code next} method has not yet been called,
     */
     void clearData();

}
