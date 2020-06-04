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
    
    void setData(T data);
    
    void setParentAs(T data);
   
    void setParentAs(CompatibleNode<T> node);

     CompatibleNode<T> addChild(T data);
   
     CompatibleNode<T> addChild(CompatibleNode<T> node);
   
     CompatibleNode<T> addSibling(T data);
   
     CompatibleNode<T> addSibling(CompatibleNode<T> node);
   
    default void removeParent() {
        setParentAs((CompatibleNode<T>) null);
        getParentNode().getChildNodes().remove(this);
    }

    CompatibleNode<T> removeChild(T target);

    boolean removeChild(CompatibleNode<T> target);
   
    CompatibleNode<T> removeSibling(T target);

    boolean removeSibling(CompatibleNode<T> target);

//getter

    T getData();
   
    T getParentData();

    CompatibleNode<T> getParentNode();

    T getTreeHeadData();

    CompatibleNode<T> getTreeHeadNode();

    List<T> getChildData();

    List<CompatibleNode<T>> getChildNodes();

    List<T> getSiblingData();

    default List<CompatibleNode<T>> getSiblingNodes() {
        return getParentNode().getChildNodes();
    }

    List<T> getOtherSiblingData();

    default List<CompatibleNode<T>> getOtherSiblingNodes() {
        List<CompatibleNode<T>> list = new ArrayList<>(getParentNode().getChildNodes());
        list.remove(this);
        return list;
    }

    List<T> getDescendingData();

    List<CompatibleNode<T>> getDescendingNodes();

    List<T> getAllData();

    List<CompatibleNode<T>> getAllNodes();

//query

    default boolean isChild() {
        return getParentNode() != null;
    }

    default boolean isLeaf() {
        return getChildNodes().isEmpty();
    }

    default boolean isHead() {
        return getParentNode() == null;
    }

    default boolean isParent() {
        return !getChildNodes().isEmpty();
    }

    default boolean containsChild(T target) {
        return getChildData().contains(target);
    }

    default boolean containsChild(CompatibleNode<T> target, CompatibleNode<T> parent) {
        return parent.getChildNodes().contains(target);
    }

    default boolean containsChild(CompatibleNode<T> target) {
        return getChildNodes().contains(target);
    }

    default boolean containsSibling(T target) {
        return getOtherSiblingData().contains(target);
    }

    default boolean containsSibling(CompatibleNode<T> target, CompatibleNode<T> node) {
        return node.getOtherSiblingNodes().contains(target);
    }

    default boolean containsSibling(CompatibleNode<T> target) {
        return getOtherSiblingNodes().contains(target);
    }

    default boolean containsDescendant(T target) {
        return getDescendingData().contains(target);
    }

    default boolean containsDescendant(CompatibleNode<T> target, CompatibleNode<T> head) {
        return head.getDescendingNodes().contains(target);
    }

    default boolean containsDescendant(CompatibleNode<T> target) {
        return getDescendingNodes().contains(target);
    }

    default boolean sameData(T data) {
        return this.getData().equals(data);
    }

    default boolean sameData(CompatibleNode<T> node) {
        return this.getData().equals(node.getData());
    }

    default boolean sameTree(T data) {
        return getAllData().contains(data);
    }

    default boolean sameParent(CompatibleNode<T> node) {
        return getParentNode().equals(node.getParentNode());
    }

    default boolean sameTree(CompatibleNode<T> node) {
        return getTreeHeadNode().equals(node.getTreeHeadNode());
    }

}
