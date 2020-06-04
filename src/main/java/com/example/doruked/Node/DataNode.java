package com.example.doruked.Node;

/**
 * This interface represents a {@code node} that potentially belongs to a {@code tree}.
 * This node is accessed (primarily) by {@code data} arguments. And provides info about itself
 * and it's tree in the form of {@code data}
 *
 * @param <T> the type of data contained by the node
 * @implSpec This interface exists to reduce the needed parameters to use {@link Basic.DataNode}, given that
 * this interface does not intend to extend behavior.
 */
public interface DataNode<T> extends Basic.DataNode<T, DataNode<T>> { }