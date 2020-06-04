package com.example.doruked.node.mynodes;

import com.example.doruked.ListUtil;
import com.example.doruked.node.Basic;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This interface represents a {@code node} that potentially belongs to a {@code tree}.
 * The aim of this interface is to provide a more than basic way of interacting with a {@code node}
 * and it's {@code tree}.
 *
 * Safety: if the various getters this interface publish references to it's internal contents
 * it leaves the possibility for some other object to mutate this {@code node} or {@code tree}
 * in a way that produces an inconsistent state. For example, suppose removing a child from a node
 * involves updating it's child state and also it's own state. If we suppose {@code node.getChildren}
 * is a reference to it's internal contents. Another object can modify that collection while failing
 * to updating the child's state
 *
 * @param <T> the type of data contained by the node
 * @implSpec
 *
 * Structure:
 * (1)This interface provides multiple versions of actions, that target different locations of the tree.
 * In general, the versions will be of: self, children, siblings, descendants, any, and sometimes parent.
 *
 * <p> (2)Further, this interface provides multiple overloads for each action. Overloads follow the
 * pattern of: {@code foo(T), foo(Node<T>) and foo(Predicate<Node<T>>)}
 *
 * <p> Note: For sibling methods, by default, this interface does not separate a caller from it's siblings when performing actions.
 * So, when a given node accessing is their siblings or performs some action on them, the action is may
 * target the calling node. To exclude the caller, one must override the appropriate methods
 * or perform the action using a predicate that will filter the caller.
 *
 * <p> Self-Use:
 * To provide default methods self-use was necessary. However it will only occurs in a structured pattern.
 *
 * <p> Self use may occur if
 * (1) The method is delegating to an overload
 * (2) It's using a normal getter/setter
 * (3) It's using a utility like {@link #sameData(T)} or "forEach"
 * (4) it's a sibling method that delegates to the child method version
 *
 * <p> I will now explain these conditions in more detail...
 *
 * <p> (1)Many methods follow the pattern of: {@code foo(T), foo(Node<T>) and foo(Predicate<Node<T>>)}
 *
 * <p> For these methods the predicate method will be left abstract, and the other methods delegate to it
 * and are thus implemented as default. So, to change the overarching behavior of some method "foo", override
 * the overload that accepts a predicate
 *
 * <p> (4) Sibling Methods - All sibling methods are default. They mostly follow the self-use patterns noted in (1),
 * except that the predicate method is not abstract. This is because a "sibling" can be stated to be "my parent's children"
 * So, this interface fulfill it's sibling methods by calling it's parent and accessing it's children. For example,
 * {@code removeSibling} becomes {@code getParent.removeChild}
 *
 * <p> To separate sibling methods from child methods, it is recommended you follow the guidelines in (1), or in short,
 * override the sibling method that accepts a predicate.
 *
 * <p> API Changes:
 * This interfaces represents what I need out of a {@code node/tree}. Methods are unlikely to be removed (unless
 * they're viewed as redundant). But methods are likely to be added. There will be a preference towards default methods.
 */
public interface Node<T>  extends Basic.CompatibleNode<T, Node<T>>, Iterable<Node<T>>  {

//modify - have to consider making unmodifiable trees? or at least removing? //TODO refactor default methods to an interface that supports modification

    /**
     * Inserts a node containing the specified {@code data} at the specified {@code index}
     * of this object's parent's collection of children
     *
     * @param data the data the added child with contain
     * @param index the position to insert at
     * @return the added node
     * @throws NullPointerException if data is null and null is not permitted
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     *
     */
    Node<T> addChild(T data, int index);

    /**
     * Inserts a copy of the specified {@code node} at the specified {@code index}
     * of this object's collection of children. The copied {@code node} may
     * optionally keeps other relations, as specified by the implementation.
     * It is most reasonably expected to to keep child relations.
     *
     * @param node the child to add
     * @param index the position to insert at
     * @return the added node
     * @throws NullPointerException if node is null and null is not permitted
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    Node<T> addChild(Node<T> node, int index);

    /**
     * Inserts a {@code node} containing the specified {@code data} at the specified {@code index}
     * of this object's parent's collection of children
     *
     * @param data the data the added sibling will contain
     * @return the added node
     * @throws NullPointerException if data is null and null is not permitted
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    default Node<T> addSibling(T data, int index) {
       return getParentNode().addChild(data, index);
    }

    /**
     * Inserts a copy of the specified {@code node} at the specified {@code index}
     * of this object's parent's collection of children. The copied {@code node} may
     * optionally keeps other relations, as specified by the implementation.
     * It is most reasonably expected to to keep child relations.
     *
     * @param node the sibling to add
     * @param index the position to insert at
     * @return the added node
     * @throws NullPointerException if node is null and null is not permitted
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    default Node<T> addSibling(Node<T> node, int index) {
       return getParentNode().addChild(node, index);
    }

    /** {@inheritDoc} */
    @Override
    default Node<T> addSibling(T data) {
       return getParentNode().addChild(data);
    }

    /** {@inheritDoc} */
    @Override
    default Node<T> addSibling(Node<T> node) {
       return getParentNode().addChild(node);
    }

    //remove

    /**
     * Removes this object's child {@code node} located at the specified {@code index}
     *
     * @param index the index of the node to be removed
     * @return the node previously at the specified position
     * @throws UnsupportedOperationException if this node does not support indexed removals of children
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    Node<T> removeChild(int index);

    /**
     * Removes a child from this object that matches the specified {@code predicate}
     *
     * @param pred the predicate to match
     * @return the removed node or null if none were removed
     * @throws NullPointerException if predicate is null
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @implSpec if multiple nodes match the predicate, the first to match should be
     * removed unless documented otherwise.
     */
    Node<T> removeChild(Predicate<? super Node<T>> pred);

    /**
     * Removes all children from this object that match the specified {@code predicate}
     *
     * @param pred the predicate to match
     * @return the removable nodes that matched the predicate or an empty list if none were removed
     * @throws NullPointerException if predicate is null
     * @throws UnsupportedOperationException if this node does not support the given modification
     */
    List<Node<T>> removeAllChildren(Predicate<? super Node<T>> pred);

    /** {@inheritDoc} */
    default Node<T> removeChild(T target) {
        return removeChild(e -> e.sameData(target));
    }

    /** {@inheritDoc} */
    default boolean removeChild(Node<T> target) {
        return removeChild(e -> e.equals(target)) != null;
    }

    /**
     * Removes from this object's parent the child located at the specified {@code index}.
     * The removed node is returned.
     *
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @throws UnsupportedOperationException if the underlying collection does not support removal
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     * @implSpec by default, this method includes the calling node as potential target of the operation
     */
    default Node<T> removeSibling(int index) {
        return getParentNode().removeChild(index);
    }

    /**
     * Removes from this object's parent the child that matches the specified {@code predicate}
     *
     * @param pred the predicate to match
     * @return the removed node that matched the predicate
     * @throws NullPointerException if predicate is null
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @implSpec (1) by default, this method includes the calling node as potential target of the operation.
     * (2) The first node to match the predicate should be removed unless documented otherwise.
     */
    default Node<T> removeSibling(Predicate<? super Node<T>> pred) {
        return getParentNode().removeSibling(pred);
    }

    /**
     * Removes from this object's parent all children that match the specified {@code predicate}
     *
     * @param pred the predicate to match
     * @return the removed node that matched the predicate
     * @throws NullPointerException if predicate is null
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @implSpec by default, this method includes the calling node as potential target of the operation.
     */
    default List<Node<T>> removeAllSibling(Predicate<? super Node<T>> pred) {
        return getParentNode().removeAllSibling(pred); //organize this method
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if target is null and null is not permitted
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @implSpec (1) by default, this method includes the calling node as potential target of the operation.
     * (2) The first node to match the target should be removed unless documented otherwise.
     */
    default Node<T> removeSibling(T target) {
        return removeSibling(e -> e.sameData(target));
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec (1) by default, this method includes the calling node as potential target of the operation.
     * (2) The first node to match the target should be removed unless documented otherwise.
     */
    default boolean removeSibling(Node<T> target) {
        return removeSibling(e -> e.equals(target)) != null;
    }

    /**
     * Removes from this object's parent all the children that contain the specified {@code data}.
     * The removed nodes are returned.
     *
     * @param target the data a sibling must contain to be removed
     * @return the removed siblings that contain the target data
     * @throws NullPointerException if target is null and null is not permitted
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @implSpec by default, this method includes the calling node as potential target of the operation.
     */
    default List<Node<T>> removeAllSibling(T target) {
        return removeAllSibling(e -> e.sameData(target));
    }

    /**
     * Removes from this object's parent all the children that contain the specified {@code data}.
     * The removed nodes are returned.
     *
     * @param target the node a sibling must contain to be removed
     * @return the removed siblings that equal the target node
     * @throws NullPointerException if target is null and null is not permitted
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @implSpec by default, this method includes the calling node as potential target of the operation.
     */
    default List<Node<T>> removeAllSibling(Node<T> target) {
        return removeAllSibling(e -> e.equals(target));
    }

    //pluck

    /**
     * Plucks this object from it's tree.
     * <p>
     * Definition: To "pluck" a node means it is removed from the tree and it's current children(if any) take it's previous place.
     * If there are multiple children, they will all move such that their "grand parent" is now their current parent.
     * As such, the plucked {@code node} loses all relations to it's current tree, but all the information
     * from it's descendants will remain connected to the tree but with a new parent
     *
     * @return the removed node or null if removal was not possible
     * @throws UnsupportedOperationException if this node does not support the given modification
     */
    Node<T> pluckNode();

    /**
     * Plucks this object's child {@code node} located at the specified {@code index}
     * <p>
     * Definition: To "pluck" a node means it is removed from the tree and it's current children(if any) take it's previous place.
     * If there are multiple children, they will all move such that their "grand parent" is now their current parent.
     * As such, the plucked {@code node} loses all relations to it's current tree, but all the information
     * from it's descendants will remain connected to the tree but with a new parent
     *
     * @param index the index of the child to be plucked
     * @return the node previously at the specified position
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     * @implSpec by default, the behavior of the pluck is dependant on {@link #pluckNode()}
     */
    default Node<T> pluckChild(int index) {
        Node<T> child = getChildNodes().get(index);
        return child.pluckNode();
    }

    /**
     * Plucks this object's child {@code node} that matches the specified {@code predicate}
     * <p>
     * Definition: To "pluck" a node means it is removed from the tree and it's current children(if any) take it's previous place.
     * If there are multiple children, they will all move such that their "grand parent" is now their current parent.
     * As such, the plucked {@code node} loses all relations to it's current tree, but all the information
     * from it's descendants will remain connected to the tree but with a new parent
     *
     * @param pred the predicate to match
     * @return the plucked node that matched the predicate
     * @implSpec by default, the behavior of the pluck is dependant on {@link #pluckNode()}
     * @throws NullPointerException if predicate is null
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @implSpec if multiple nodes match the predicate, the first to match should be
     * plucked unless documented otherwise.
     */
   default Node<T> pluckChild(Predicate<? super Node<T>> pred){
       Node<T> child = helperGet(pred, getChildNodes());
       return child.pluckNode();
   }

    /**
     * Plucks from this object's parent the child located at the specified {@code index}
     * <p>
     * Definition: To "pluck" a node means it is removed from the tree and it's current children(if any) take it's previous place.
     * If there are multiple children, they will all move such that their "grand parent" is now their current parent.
     * As such, the plucked {@code node} loses all relations to it's current tree, but all the information
     * from it's descendants will remain connected to the tree but with a new parent
     *
     * @param index the index of the sibling to be plucked
     * @return the node previously at the specified position
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     * @implSpec by default, this method includes the calling node as potential target of the operation.
     */
    default Node<T> pluckSibling(int index) {
        return getParentNode().pluckChild(index);
    }

    /**
     * Plucks from this object's parent the child that matches the specified {@code predicate}
     * <p>
     * Definition: To "pluck" a node means it is removed from the tree and it's current children(if any) take it's previous place.
     * If there are multiple children, they will all move such that their "grand parent" is now their current parent.
     * As such, the plucked {@code node} loses all relations to it's current tree, but all the information
     * from it's descendants will remain connected to the tree but with a new parent
     *
     * @param pred the predicate to match
     * @return the plucked node that matched the predicate
     * @throws NullPointerException if predicate is null
     * @throws UnsupportedOperationException if this node does not support the given modification
     * @implSpec (1) by default, this method includes the calling node as potential target of the operation.
     * (2) The first node to match the predicate should be removed unless documented otherwise.
     */
    default Node<T> pluckSibling(Predicate<? super Node<T>> pred) {
        return getParentNode().pluckChild(pred);
    }

    /** ({@inheritDoc} */
    void forEach(Consumer<? super Node<T>> action);

    /**
     * Applies the specified {@code consumer} action on the data contained by each node
     * in this object's tree. Actions are performed in the order defined by implementors.
     * Exceptions thrown by the action are relayed to the caller, and cause iteration to stop.
     *
     * The behavior of this method is unspecified if the action performs
     * side-effects that modify the underlying source of elements, unless an
     * overriding class has specified a concurrent modification policy.
     *
     * @throws NullPointerException if consumer is null
     * @implSpec application of this method is consistent with {@link #forEach(Consumer)}.
     * By default, this method converts it's specified {@code consumer} to be usable by said method.
     */
    default void forEachData(Consumer<? super T> cons) {
        Consumer<Node<T>> c = e -> cons.accept(e.getData());
        forEach(c);
    }

//getter

    /** {@inheritDoc} */
    @Override
    default List<T> getSiblingData() {
        return getParentNode().getChildData();
    }

    /** {@inheritDoc} */
    default List<Node<T>> getAllNodes() {
        List<Node<T>> list = new ArrayList<>();
        forEach(list::add);
        return list;
    }

    /** {@inheritDoc} */
    default List<T> getAllData() {
        List<T> list = new ArrayList<>();
        forEachData(list::add);
        return list;
    }

//find

    /**
     * Returns a node from this object's tree that matches the predicate.
     * If none do, {@code null} is returned.
     *
     * @param pred the predicate to match
     * @return a node of this object's tree that matched the predicate or null if none do
     * @throws NullPointerException if predicate is null
     * @implSpec if multiple nodes match the predicate, the first to match should be
     * returned unless documented otherwise.
     */
    Node<T> getNode(Predicate<? super Node<T>> pred);

    /**
     * Returns a node containing the specified {@code data} or null if not found
     *
     * @param data the data the returned node must contain
     * @return a node containing the specified data or null if not found
     * @throws NullPointerException if data is null and null is not permitted
     * @implSpec if multiple nodes match the data, the first to match should be
     * returned unless documented otherwise.
     */
    default Node<T> getNode(T data) {
        return getNode(e -> e.sameData(data));
    }

    //child

    /**
     * Returns this object's child that matches the specified {@code predicate}
     * 
     * @param pred the predicate to match
     * @return the child that matched the predicate, or null if none matched
     * @throws NullPointerException if predicate is null
     * @implSpec if multiple nodes match the predicate, the first to match should be
     * returned unless documented otherwise.
     */
    Node<T> getChild(Predicate<? super Node<T>> pred);

    /**
     * Returns all of this object's children that match the specified {@code predicate}
     * 
     * @param pred the predicate to match
     * @return all the children that match the predicate
     * @throws NullPointerException if predicate is null
     */
    List<Node<T>> getChildIf(Predicate<? super Node<T>> pred);
    
    /**
     * Returns this object's child that contains the specified {@code data}
     * 
     * @param data the data the sought after child contains
     * @return the child that contains the specified data or null if none do
     * @throws NullPointerException if data is null and null is not permitted
     * @implSpec if multiple nodes match the data, the first to match should be
     * returned unless documented otherwise.
     */
    default Node<T> getChild(T data) {
        return getChild(e -> e.sameData(data));
    }

    /**
     * Returns this objects child that equals the specified {@code target}
     * 
     * @param target the node the sought after child is equal to
     * @return the child that equals the target node
     * @throws NullPointerException if target is null and null is not permitted
     * @implSpec if multiple nodes match the target, the first to match should be
     * returned unless documented otherwise.
     */
    default Node<T> getChild(Node<T> target){
        return getChild(e-> e.equals(target));
    }

    /**
     * Returns all of this object's children that contains the specified data 
     * or an empty {@code list} if none do.
     *
     * @param data the data sought after children must contain
     * @return all the children that contain the specified data
     * @throws NullPointerException if data is null and null is not permitted
     */
    default List<Node<T>> getChildIf(T data) {
        return getChildIf(e -> e.sameData(data)); //rename this to target, and have target data in returns
    }

    /**
     * Returns all of this object's children that equal the specified {@code target}
     * 
     * @param target the node sought after children are equal to
     * @return the children that are equal to the target node
     * @throws NullPointerException if target is null and null is not permitted
     */
    default List<Node<T>> getChildIf(Node<T> target){
        return getChildIf(e-> e.equals(target));
    }

    //sibling

    /**
     * Returns from this object's parent the child that matches the specified {@code predicate}
     *
     * @param pred the predicate to match
     * @return the sibling that matched the predicate, or null if none matched
     * @throws NullPointerException if predicate is null
     * @implSpec (1) by default, this method includes the calling node as potential target of the operation.
     * (2) The first node to match the predicate should be removed unless documented otherwise.
     */
    default Node<T> getSibling(Predicate<? super Node<T>> pred) {
        return getParentNode().getChild(pred);
    }

    /**
     * Returns from this object's parent all the children that match the specified {@code predicate}
     *
     * @param pred the predicate to match
     * @return all the siblings that match the predicate
     * @throws NullPointerException if predicate is null
     * @implSpec by default, this method includes the calling node as potential target of the operation
     */
    default List<Node<T>> getSiblingIf(Predicate<? super Node<T>> pred) {
        return getParentNode().getChildIf(pred);
    }

    /**
     * Returns from this object's parent the child that contains the specified {@code data}
     *
     * @param data the data the sought after sibling contains
     * @return the sibling that contains the specified data or null if none do
     * @throws NullPointerException if data is null and null is not permitted
     * @implSpec (1) by default, this method includes the calling node as potential target of the operation.
     * (2) The first node to match the data should be removed unless documented otherwise.
     */
    default Node<T> getSibling(T data) {
        return getSibling(e -> e.sameData(data));
    }

    /**
     * Returns from this object's parent the child that equals the specified {@code target}
     *
     * @param target the node the sought after sibling is equal to
     * @return the sibling that equals the target node
     * @throws NullPointerException if target is null and null is not permitted
     * @implSpec (1) by default, this method includes the calling node as potential target of the operation.
     * (2) The first node to match the target should be removed unless documented otherwise.
     */
    default Node<T> getSibling(Node<T> target) {
        return getSibling(e -> e.equals(target));
    }

    /**
     * Returns from this object's parent all the children that contains the specified data
     * or an empty {@code list} if none do.
     *
     * @param data the data sought after siblings must contain
     * @return all the siblings that contain the specified data
     * @throws NullPointerException if data is null and null is not permitted
     * @implSpec (1) by default, this method includes the calling node as potential target of the operation.
     * (2) The first node to match the data should be removed unless documented otherwise.
     */
    default List<Node<T>> getSiblingIf(T data) {
        return getSiblingIf(e -> e.sameData(data));
    }

    /**
     * Returns from this object's parent all the children that equal the specified {@code target}
     *
     * @param target the node sought after siblings are equal to
     * @return the children that are equal to the target node
     * @throws NullPointerException if target is null and null is not permitted
     * @implSpec (1) by default, this method includes the calling node as potential target of the operation.
     * (2) The first node to match the target should be removed unless documented otherwise.
     */
    default List<Node<T>> getSiblingIf(Node<T> target) {
        return getSiblingIf(e -> e.equals(target));
    }

    //descending

    /**
     * Returns all of this object's descendant nodes that match the specified {@code pred}
     *
     * @param pred the predicate to match
     * @return the descendant nodes that matched the predicate
     * @throws NullPointerException if predicate is null
     */
    List<Node<T>> getDescendingNodeIf(Predicate<? super Node<T>> pred);

    /**
     * Returns all of this object's descendant data that match the specified {@code pred}
     *
     * @param pred the predicate to match
     * @return the descendant data that matched the predicate
     * @throws NullPointerException if predicate is null
     */
    List<T> getDescendingIf(Predicate<? super Node<T>> pred);

    /**
     * Returns all of this object's descendant nodes that contain the specifed {@code target} as data
     *
     * @param target the node sought after descendants are equal to
     * @return the descendant nodes that contain the target data
     * @throws NullPointerException if target is null and null is not permitted
     */
    default List<Node<T>> getDescendingNodeIf(T target) {
        return getDescendingNodeIf(e -> e.sameData(target));
    }

    /**
     * Returns all of this object's descendant nodes that equals the specified target
     *
     * @param target the node sought after descendants are equal to
     * @return all descendant nodes that equal the target node
     * @throws NullPointerException if target is null and null is not permitted
     */
    default List<Node<T>> getDescendingNodeIf(Node<T> target) {
        return getDescendingNodeIf(e -> e.equals(target));
    }

    /**
     * Returns all of this object's descendant data that contain the specified {@code target} as data
     *
     * @param target the data sought after descendants are equal to
     * @return the descendant data that equals the target data
     * @throws NullPointerException if target is null and null is not permitted
     */
    default List<T> getDescendingIf(T target) {
        return getDescendingIf(e -> e.sameData(target));
    }

    /**
     * Returns all of this object's descendant data who's nodes equal the specified {@code target}
     *
     * @param target the node sought after descendants are equal to
     * @return the descendant data who's nodes equal the target node
     * @throws NullPointerException if target is null and null is not permitted
     */
    default List<T> getDescendingIf(Node<T> target) {
        return getDescendingIf(e -> e.equals(target));
    }

    //all

    /**
     * Returns all nodes in this object's tree that match the specified {@code predicate}
     *
     * @param pred the predicate to match
     * @return all nodes in this object's tree that match the predicate
     * @throws NullPointerException if predicate is null
     */
    List<Node<T>> getNodeIf(Predicate<? super Node<T>> pred);

    /**
     * Returns the data from all nodes in this object's tree that match the specified {@code predicate}
     *
     * @param pred the predicate to match
     * @return the data in this object's tree that match the predicate
     * @throws NullPointerException if predicate is null
     */
    List<T> getDataIf(Predicate<? super Node<T>> pred);

    /**
     * Returns all nodes in this object's tree that contain the specified {@code target} as data
     *
     * @param data the data sought after nodes contain
     * @return all nodes in this object's tree that contain the target data
     * @throws NullPointerException if data is null and null is not permitted
     */
    default List<Node<T>> getNodeIf(T data) {
        return getNodeIf(e -> e.sameData(data));
    }

    /**
     * Returns all nodes in this object's tree that equal the specified {@code target}
     *
     * @param target the node sought after descendants are equal to
     * @return all nodes in this object's tree that equal the target node
     * @throws NullPointerException if target is null and null is not permitted
     */
    default List<Node<T>> getNodeIf(Node<T> target) {
        return getNodeIf(e -> e.equals(target));
    }

    /**
     * Returns the data from all nodes in this object's tree that contain the specified {@code target} as data
     * @param target the data get from tree
     * @return the data from nodes in this object's tree
     * @throws NullPointerException if target is null and null is not permitted
     */
    default List<T> getDataIf(T target) {
        return getDataIf(e -> e.sameData(target));
    }

    /**
     * Returns the data from all nodes in this object's tree that equal the specified {@code target}
     *
     * @param target the node to be equal to
     * @return the data from nodes in this object's tree that equal the target node
     * or any empty list if none found
     * @throws NullPointerException if target is null and null is not permitted
     */
    default List<T> getDataIf(Node<T> target) {
        return getDataIf(e -> e.equals(target));
    }

//not organized

    /**
     * Converts the specified {@link Predicate<T>} to a {@code Predicate<Node<T>>}. The conversion allows the
     * specified {@code predicate} to be usable by instances methods of this class that are {@link Node <T>}
     *
     * @param pred the predicate to convert
     * @param <T>  the type contained by the node
     * @return a predicate equivalent to the specified {@code pred} that can apply to {@link Node}s
     * @throws NullPointerException if predicate is null
     */
    static <T> Predicate<Node<T>> predicate(Predicate<? super T> pred) {
        return node -> pred.test(node.getData());
    }

    /**
     * "Unboxes" or remaps the specified {@code nodes} to a {@code list} consisting of the {@code data}
     * each {@code node} contained.
     *
     * @param nodes the collection to unbox
     * @param <T>   the type to unbox as
     * @return a list consisting of the data contained in the specified nodes
     * @see ListUtil#unbox(Collection, Function)
     * @throws NullPointerException if nodes is null
     */
    static <T> List<T> unbox(Collection<? extends Node<T>> nodes) {
        return ListUtil.unbox(nodes, Node::getData);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    default Iterator<Node<T>> iterator() {
        return NodeIterator.createFromHead(this);
    }

//helper

   private Node<T> helperGet(Predicate<? super Node<T>> pred, List<? extends Node<T>> list){
        return ListUtil.get(pred,list);
    }

    private  List<Node<T>> helperGetIf(Predicate<? super Node<T>> pred, List<? extends Node<T>> list){
        return ListUtil.getIf(pred,list);
    }

//inner class

    /**
     *  This iterator operates by converting all the nodes of a given tree
     *  into a list, and then creating an iterator for that list.
     *  The iterator produced does not support removing elements.
     *
     * @see #createFromCurrent(Node)
     * @see #createFromHead(Node)
     * @param <T> the type of data stored by tree nodes
     */
    class NodeIterator<T> implements Iterator<Node<T>> {

        private final Iterator<Node<T>> it;

        private NodeIterator(Iterator<Node<T>> it) {
            this.it = it;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public Node<T> next() {
            return it.next();
        }

        /**
         * Returns an {@link Iterator} that begins iteration at the specified {@code node}.
         *
         * @param node the node to begin at
         * @param <T> the type of data contained by the node
         * @return an iterator that begins iteration at the specified {@code node}.
         */
        static <T> Iterator<Node<T>> createFromCurrent(Node<T> node) {
            List<Node<T>> list = node.getDescendingNodes();
            return new NodeIterator<>(list.iterator());
        }

        /**
         * Returns an {@link Iterator} that begins iteration at the {@code tree head} of specified {@code node}.
         *
         * @param node the node to begin at
         * @param <T> the type of data contained by the node
         * @return an iterator that begins iteration at the {@code tree head} of specified {@code node}.
         */
        static <T> Iterator<Node<T>> createFromHead(Node<T> node) {
            List<Node<T>> list = node.getAllNodes();
            return new NodeIterator<>(list.iterator());
        }
    }
}
