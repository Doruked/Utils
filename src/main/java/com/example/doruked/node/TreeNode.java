package com.example.doruked.node;

import com.example.doruked.View;

import java.util.List;

/**
 * This interface represents a {@code node} that potentially belongs to a {@code tree}.
 * This node is accessed (primarily) by {@code node} arguments. And provides info about it self
 * and it's tree in the form of {@code nodes}
 *
 * @param <T> the type of data contained by the node
 * @see View
 */
@View
public interface TreeNode<T> extends Basic.TreeNode<T, TreeNode<T>> {

//modify

    @Override
    void setParentAs(TreeNode<T> node);

    @Override
    TreeNode<T> addChild(TreeNode<T> TreeNode);

    @Override
    TreeNode<T> addSibling(TreeNode<T> node);

    @Override
    void setData(T data);

    @Override
   default void removeParent(){ setParentAs(null); }

    @Override
    boolean removeChild(TreeNode<T> target);

    @Override
    boolean removeSibling(TreeNode<T> target);

//getter

    @Override
    T getData();

    @Override
    TreeNode<T> getParentNode();

    @Override
    TreeNode<T> getTreeHeadNode();

    @Override
    List<TreeNode<T>> getChildNodes();

    @Override
    default List<TreeNode<T>> getSiblingNodes() {
        return Basic.TreeNode.super.getSiblingNodes();
    }

    @Override
    default List<TreeNode<T>> getOtherSiblingNodes() {
        return Basic.TreeNode.super.getOtherSiblingNodes();
    }

    @Override
    List<TreeNode<T>> getDescendingNodes();

    @Override
    List<TreeNode<T>> getAllNodes();

//query

    @Override
    default boolean isParent() {
        return Basic.TreeNode.super.isParent();
    }

    @Override
    default boolean isChild() {
        return Basic.TreeNode.super.isChild();
    }

    @Override
    default boolean isLeaf() {
        return Basic.TreeNode.super.isLeaf();
    }

    @Override
    default boolean isHead() {
        return Basic.TreeNode.super.isHead();
    }

    @Override
    default boolean containsChild(TreeNode<T> target, TreeNode<T> parent) {
        return Basic.TreeNode.super.containsChild(target, parent);
    }

    @Override
    default boolean containsChild(TreeNode<T> target) {
        return Basic.TreeNode.super.containsChild(target);
    }

    @Override
    default boolean containsSibling(TreeNode<T> target, TreeNode<T> node) {
        return Basic.TreeNode.super.containsSibling(target, node);
    }

    @Override
    default boolean containsSibling(TreeNode<T> target) {
        return Basic.TreeNode.super.containsSibling(target);
    }

    @Override
    default boolean containsDescendant(TreeNode<T> target, TreeNode<T> head) {
        return Basic.TreeNode.super.containsDescendant(target, head);
    }

    @Override
    default boolean containsDescendant(TreeNode<T> target) {
        return containsDescendant(target);
    }

    @Override
    default boolean sameData(TreeNode<T> node) {
        return Basic.TreeNode.super.sameData(node);
    }

    @Override
    default boolean sameParent(TreeNode<T> node) {
        return Basic.TreeNode.super.sameParent(node);
    }

    @Override
    default boolean sameTree(TreeNode<T> node) {
        return Basic.TreeNode.super.sameTree(node);
    }

}
