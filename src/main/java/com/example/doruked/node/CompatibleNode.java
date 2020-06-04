package com.example.doruked.node;

import java.util.ArrayList;
import java.util.List;

/**
 * This interface represents a {@code node} that potentially belongs to a {@code tree}.
 * A CompatibleNode provides basic {@code node} operations that are usable by specifying
 * a {@code node} or simply the {@code data}. These operations don't guarantee to operate
 * exactly the same, as specifying a {@code node} may lend itself to more accurate results,
 * assuming the {@code tree} allows {@code nodes} with duplicate {@code data}
 *
 * @param <T> the data contained by the node
 * @implSpec This interface exists to reduce the needed parameters to use {@link Basic.CompatibleNode}, given that
 * this interface does not intend to extend behavior.
 * @see Basic.TreeNode
 * @see Basic.DataNode
 */
public interface CompatibleNode<T>  extends Basic.CompatibleNode<T, CompatibleNode<T>> {
    //No new methods, just provides a view. Changes to this interface are pulled up to appropriate parent


//modify

    /** {@inheritDoc} */
    void setData(T data);

    /** {@inheritDoc} */
    void setParentAs(T data);

    /** {@inheritDoc} */
    void setParentAs(CompatibleNode<T> node);

    /** {@inheritDoc} */
     CompatibleNode<T> addChild(T data);

    /** {@inheritDoc} */
     CompatibleNode<T> addChild(CompatibleNode<T> node);

    /** {@inheritDoc} */
     CompatibleNode<T> addSibling(T data);

    /** {@inheritDoc} */
     CompatibleNode<T> addSibling(CompatibleNode<T> node);

    /** {@inheritDoc} */
    default void removeParent() {
        setParentAs((CompatibleNode<T>) null);
        getParentNode().getChildNodes().remove(this);
    }

    /** {@inheritDoc} */
    CompatibleNode<T> removeChild(T target);

    /** {@inheritDoc} */
    boolean removeChild(CompatibleNode<T> target);

    /** {@inheritDoc} */
    CompatibleNode<T> removeSibling(T target);

    /** {@inheritDoc} */
    boolean removeSibling(CompatibleNode<T> target);

//getter

    /** {@inheritDoc} */
    T getData();

    /** {@inheritDoc} */
    T getParentData();

    /** {@inheritDoc} */
    CompatibleNode<T> getParentNode();

    /** {@inheritDoc} */
    T getTreeHeadData();

    /** {@inheritDoc} */
    CompatibleNode<T> getTreeHeadNode();

    /** {@inheritDoc} */
    List<T> getChildData();

    /** {@inheritDoc} */
    List<CompatibleNode<T>> getChildNodes();

    /** {@inheritDoc} */
    List<T> getSiblingData();

    /** {@inheritDoc} */
    default List<CompatibleNode<T>> getSiblingNodes() {
        return getParentNode().getChildNodes();
    }

    /** {@inheritDoc} */
    List<T> getOtherSiblingData();

    /** {@inheritDoc} */
    default List<CompatibleNode<T>> getOtherSiblingNodes() {
        List<CompatibleNode<T>> list = new ArrayList<>(getParentNode().getChildNodes());
        list.remove(this);
        return list;
    }

    /** {@inheritDoc} */
    List<T> getDescendingData();

    /** {@inheritDoc} */
    List<CompatibleNode<T>> getDescendingNodes();

    /** {@inheritDoc} */
    List<T> getAllData();

    /** {@inheritDoc} */
    List<CompatibleNode<T>> getAllNodes();

//query

    /** {@inheritDoc} */
    default boolean isChild() {
        return getParentNode() != null;
    }

    /** {@inheritDoc} */
    default boolean isLeaf() {
        return getChildNodes().isEmpty();
    }

    /** {@inheritDoc} */
    default boolean isHead() {
        return getParentNode() == null;
    }

    /** {@inheritDoc} */
    default boolean isParent() {
        return !getChildNodes().isEmpty();
    }

    /** {@inheritDoc} */
    default boolean containsChild(T target) {
        return getChildData().contains(target);
    }

    /** {@inheritDoc} */
    default boolean containsChild(CompatibleNode<T> target, CompatibleNode<T> parent) {
        return parent.getChildNodes().contains(target);
    }

    /** {@inheritDoc} */
    default boolean containsChild(CompatibleNode<T> target) {
        return getChildNodes().contains(target);
    }

    /** {@inheritDoc} */
    default boolean containsSibling(T target) {
        return getOtherSiblingData().contains(target);
    }

    /** {@inheritDoc} */
    default boolean containsSibling(CompatibleNode<T> target, CompatibleNode<T> node) {
        return node.getOtherSiblingNodes().contains(target);
    }

    /** {@inheritDoc} */
    default boolean containsSibling(CompatibleNode<T> target) {
        return getOtherSiblingNodes().contains(target);
    }

    /** {@inheritDoc} */
    default boolean containsDescendant(T target) {
        return getDescendingData().contains(target);
    }

    /** {@inheritDoc} */
    default boolean containsDescendant(CompatibleNode<T> target, CompatibleNode<T> head) {
        return head.getDescendingNodes().contains(target);
    }

    /** {@inheritDoc} */
    default boolean containsDescendant(CompatibleNode<T> target) {
        return getDescendingNodes().contains(target);
    }

    /** {@inheritDoc} */
    default boolean sameData(T data) {
        return this.getData().equals(data);
    }

    /** {@inheritDoc} */
    default boolean sameData(CompatibleNode<T> node) {
        return this.getData().equals(node.getData());
    }

    /** {@inheritDoc} */
    default boolean sameTree(T data) {
        return getAllData().contains(data);
    }

    /** {@inheritDoc} */
    default boolean sameParent(CompatibleNode<T> node) {
        return getParentNode().equals(node.getParentNode());
    }

    /** {@inheritDoc} */
    default boolean sameTree(CompatibleNode<T> node) {
        return getTreeHeadNode().equals(node.getTreeHeadNode());
    }

}
