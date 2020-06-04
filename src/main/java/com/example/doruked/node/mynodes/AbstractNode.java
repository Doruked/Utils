package com.example.doruked.node.mynodes;

import com.example.doruked.ListUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
//overrding a method using a "helper" means that method may no longer be affected by changes to that helper method
//not competely safe to publish, nodes as they can be removed leaving the tree in an inconsistent state
//can refactor them to be "views"

/**
 *
 *------Is Draft-----------
 *
 * Modify Nodes and their Tree:
 *
 * Modifications can be broken down into the interaction between an two nodes
 * check whether you should update the state of the another node
 * A raw call to update a node, inside another method who's object may be the target of
 * that update is prone to lead to a recursive call if you are unfamiliar with
 * how each node will be/is implemented.
 *
 * In short, if a reasonable check can be made, do so before calling for an update.
 *
 * Methods that modify the state of this object's tree occur in two components.
 * (1) An updating of the callers(i.e. this object) state
 * (2) A request for another node to update it's state
 *
 * For example, suppose you want to remove your parent. The calling object
 *
 * (2) Is a good faith effort to properly maintain the state of this object's tree.
 * Whether the other node behaves properly is not easy to guarantee, if you are
 * interacting with an unknown implementation.
 *
 * This good faith effort does result in potential duplicate actions, that may be trivial.
 * So for example, when a node is requested to update it's state,
 *
 * If you are in c, you may re-implement some of the methods that modify in this
 * class to optimize behavior.
 *
 *
 * This class makes many of the methods in {@link Node} default, in exchange for having extendors of this
 * class provide an implementation for some much easier abstract methods.
 *
 * In implementing these methods, this class maintains that
 *
 * A structure is provided for overriding methods and making the effects of doing so not have side effects.
 * This class makes use of {@code protected} helper methods to ensure that overriding a given {@code public}
 * method does affect the behavior of any another.
 *
 * @implNote this interface implements all of the non-trival methods that occur in {@link Node}.
 * As an extendor of this interface, you are choosing how you would configure a nodes state. And further,
 * you may override target methods, while leaving the rest. For example, if a different iteration strategy was desired,
 * you could override {@link #helperIterateDescendants(Node, Predicate)}. And in doing so, you change how
 * multi-layered traversal occurs for all methods without needed to change those specific methods.
 *
 * Why? Because your modifications may be overwritten
 * If you update your state to it's new value, then request an another object to update it's state,
 * if method may feasibly attempt to alter your state, then your modifications may be overwritten!
 *
 * @param <T> the type of data contained by the node
 */
public abstract class AbstractNode<T> implements Node<T> {

//modify

    /** {@inheritDoc} */
    @Override
    public void setParentAs(T data) {
        setParentAs(create(data));
    }

    /** {@inheritDoc} */
    @Override
    public void removeParent() {
        helperRemove(this); //removes self from parent's tree
    }

    /** {@inheritDoc} */
    @Override
    public Node<T> addChild(T data) {
        return helperAddChild(create(data), defaultAddIndex());
    }

    /** {@inheritDoc} */
    @Override
    public Node<T> addChild(Node<T> node) {
        return helperAddChild(node, defaultAddIndex());
    }

    /** {@inheritDoc} */
    @Override
    public Node<T> addChild(T data, int index) {
       return helperAddChild(create(data), index);
    }

    @Override
    public Node<T> addChild(Node<T> node, int index) {
       return helperAddChild(node, index);
    }

    /** {@inheritDoc} */
    @Override
    public Node<T> removeChild(int index) {
        Node<T> target = hGetChildNodes().remove(index);
        target.setParentAs((Node<T>) null);
        return target;
    }

    /** {@inheritDoc} */
    @Override
    public Node<T> removeChild(Predicate<? super Node<T>> pred) {
        Node<T> target = ListUtil.get(pred, hGetChildNodes());
        if(target == null) return null;
        return helperRemove(target);
    }

    /** {@inheritDoc} */
    @Override
    public List<Node<T>> removeAllChildren(Predicate<? super Node<T>> pred) {
        List<Node<T>> targets = ListUtil.getIf(pred, hGetChildNodes());
        for(Node<T> n : targets){
            helperRemove(n);
        }
        return targets;
    }


    /**
     * {@inheritDoc}
     * @implSpec if {@link #hGetChildNodes()} does not provide the backing list of children
     * the method doesn't work. But also, if it returns an unmodifiable list, the method recurses infinitely.
     * Both errors represent improper implementation of the method.
     */
    @Override
    public Node<T> pluckNode() {
        Node<T> myParent = getParentNode();
        List<Node<T>> children = hGetChildNodes();
        helperRemove(this);

        while(children.size() > 0){
            Node<T> c = children.get(0);
            if(c == null) break;
            c.setParentAs(myParent);
        }
        return this;
    }


    /** {@inheritDoc} */
    public void forEach(Consumer<? super Node<T>> cons) {
        Node<T> head = getTreeHeadNode();
        cons.accept(head);
        helperForEachDescendant(head, cons);
    }

//getter

    /**
     * {@inheritDoc}
     * @throws ClassCastException if {@code this} is not a child of {@link T}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public Node<T> getTreeHeadNode() {
        Node<T> treeHead = this;
        Node<T> head = this;
        while ((head = head.getParentNode()) != null) {
            if(treeHead == head) throw new IllegalStateException("Node(" + this + ") Kcontains itself as parent");
            treeHead = head;
        }
        return treeHead;
    }

    /** {@inheritDoc} */
    @Override
    public List<T> getChildData() {
        List<T> list = new ArrayList<>();
        helperForEachDescendant(this, e-> list.add(e.getData()));
        return list;
    }

//find

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     *
     * @implSpec by default, to understand how a search occurs, may require understanding
     * two methods depending on if either are overridden (1){@link #helperIterateDescendants(Node, Predicate)}
     * or (2){@link #helperIterateAll(Predicate)}. I suggest viewing the first if neither is overridden.
     */
    @Override
    public Node<T> getNode(Predicate<? super Node<T>> pred) {
        AtomicReference<Node<T>> result = new AtomicReference<>(null);
        helperIterateAll(node -> {
            if (pred.test(node)) {
                result.set(node);
                return true;
            }
            return false;
        });
        return result.get();
    }

    /** {@inheritDoc} */
    @Override
    public List<Node<T>> getNodeIf(Predicate<? super Node<T>> pred) {
        return helperGetIf((node, list) -> {
            if (pred.test(node))
                list.add(node);
        });
    }

    /** {@inheritDoc} */
    public Node<T> getChild(Predicate<? super Node<T>> pred) { //requires being added
       return helperGet(pred, getChildNodes());
    }

    /** {@inheritDoc} */
    @Override
    public List<Node<T>> getChildIf(Predicate<? super Node<T>> pred) {
        return helperGetIf(pred, getChildNodes());
    }

    //descending

    /** {@inheritDoc} */
    @Override
    public List<Node<T>> getDescendingNodes() {
        return helperGetDescending(this, Function.identity());
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws ClassCastException if {@code this} is not a child of {@link Node <T>}
     */
    @Override
    public List<T> getDescendingData() {
        return helperGetDescending(this, Node::getData);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws ClassCastException if {@code this} is not a child of {@link Node <T>}
     */
    @Override
    public List<Node<T>> getDescendingNodeIf(Predicate<? super Node<T>> pred) {
        return helperGetDescendingIf(this, Function.identity(), pred);
    }

    /** {@inheritDoc} */
    @Override
    public List<T> getDescendingIf(Predicate<? super Node<T>> pred) {
        return helperGetDescendingIf(this, Node::getData, pred);
    }

    /** {@inheritDoc} */
    @Override
    public List<T> getDataIf(Predicate<? super Node<T>> pred) {
        return helperGetIf((node, list) -> {
            if (pred.test(node)) {
                list.add(node.getData());
            }
        });
    }


//abstract

    /** can't locate if there are mutliple and it's noneset based? */
    protected abstract Node<T> create(T data);

    /**
     * Adds a {@code node} containing the specified {@code data} to the specified {@code parent}
     *
     * @param child  the child to add
     * @param index  the index to insert at
     * @implSpec this method should fulfill the role adding the child in a way that properly maintains
     * the state of the tree and it's nodes. This may require updating the parent and the calling node
     * @throws IndexOutOfBoundsException if the index is too big or small to be accepted
     * by this objects collection of children
     */
    protected abstract Node<T> helperAddChild(Node<T> child, int index);

    /**
     * Returns a list that when modified, affects the contained children in this class
     * This method exists to allow like {@link #getChildNodes()} to return immutable
     * instances or copies.
     *
     * @return the list backing this objects children
     */
    protected abstract List<Node<T>> hGetChildNodes();

//helpers

    //general

    /**
     * This method determines the default {@code index} an accepted child will be inserted at.
     *
     * @return the default index for insertion
     * @implNote the default implementation appends added nodes
     */
    protected int defaultAddIndex(){
        return getChildNodes().size();
    }

    //modify

    /**
     * Performs the necessary actions it takes to sever a nodes relationship to the current tree.
     * The specified {@code target} is the {@code node} to be removed
     *
     * @param target the node to remove
     * @return the removed node (for use in other methods)
     * @implSpec by default, this method is used in all operations removes a node from the tree.
     * This method is responsible for making sure that it leaves the tree in an appropriate state.
     * This may involve updating the state of multiple nodes.
     */
    protected Node<T> helperRemove(Node<T> target){
        target.setParentAs((Node<T>) null);
        return target;
    }

    //find

    /**
     * Returns the first element in the specified {@code nodes} to match the specified {@code predicate}
     *
     * @param pred the predicate to match
     * @param nodes the list to search
     * @return the first node to match the predicate or null if none match
     */
    protected Node<T> helperGet(Predicate<? super Node<T>> pred, List<? extends Node<T>> nodes){
        return ListUtil.get(pred, nodes);
    }

    /**
     * Returns the list of elements from the specified {@code nodes} that matched
     * the specified {@code predicate}
     *
     * @param pred  the predicate to match
     * @param nodes the nodes to search
     * @return the list of nodes that matched the predicate
     * @see ListUtil#getIf(Predicate, Collection)
     */
    protected List<Node<T>> helperGetIf(Predicate<? super Node<T>> pred, List<? extends Node<T>> nodes){
        return ListUtil.getIf(pred, nodes);
    }

    /**
     * Applies the specified {@code biConsumer} to each node in this object's tree and a local {@link List}.
     * After iteration completes, the local {@code list} is returned, containing any mutations that are the result
     * of applying the {@code biconsumer}
     *
     * @param cons the consumer to use
     * @param <U> the type to be stored
     * @return a list that is the result of apply the biconsumer to the list and each node of this objects tree
     */
    protected <U> List<U> helperGetIf(BiConsumer<? super Node<T>, List<U>> cons){
        List<U> filtered = new ArrayList<>();
        forEach(e-> cons.accept(e, filtered));
        return filtered;
    }

    //find descending

    /**
     * This method collects the descendants of the specified {@code head} and stores them according to
     * applying the specified {@code converter} to each descendant.
     *
     * @param head the node to get descendants from
     * @param converter determines whether Node<T> or T is stored in the returned list
     * @param <R> the type to store nodes as
     * @return a list of descendants that have been converted by the converter
     * @throws NullPointerException if a head or converter is null
     */
    protected <R> List<R> helperGetDescending(Node<T> head, Function<? super Node<T>, ? extends R> converter) {
        List<R> list = new ArrayList<>();
        helperForEachDescendant(head, e -> list.add(converter.apply(e)));
        return list;
    }

    /**
     * This method filters the descendants of the specified {@code head} by the specified {@code predicate},
     * and stores matching {@code nodes} according to applying the specified {@code converter} to them.
     *
     * @param head the node to get descendants from
     * @param converter determines whether Node<T> or T is stored in the returned list
     * @param pred the predicate to apply to descendants
     * @param <R> the type to store nodes as the type to store nodes as
     * @return the list of descendants that matched the predicate, and were converted by the converter
     * @throws NullPointerException if a null argument is received
     */
    protected <R> List<R> helperGetDescendingIf(Node<T> head, Function<? super Node<T>, ? extends R> converter, Predicate<? super Node<T>> pred) {
        List<R> list = new ArrayList<>();
        helperForEachDescendant(head, e -> {
            if (pred.test(e))
            list.add(converter.apply(e));
        });
        return list;
    }

    //iteration control - anything involving more than 1 layer of the tree

    /**
     * Iterates through each descendant of the specified {@code parent} and applies the specified {@code consumer}
     *
     * @param cons the consumer to apply
     * @param parent the node to get descendants from
     * @implSpec by default, only used by other helpers
     */
    protected void helperForEachDescendant(Node<T> parent, Consumer<? super Node<T>> cons) {
        Predicate<Node<T>> forEach = (node) -> {
            cons.accept(node);
            return false; //false constant causes helperIterateDescendants to check all nodes
        };
        helperIterateDescendants(parent, forEach);
    }

    /**
     * Iterates through each node of the this object's tree and applies the specified {@code predicate}
     * on each {@code node}. If the predicate is matched, iteration stops and the method  returns {@code true}
     *
     * @param pred the predicate that if true, causes iteration to stop
     * @return true if a node matched the predicate, false if not and all
     * nodes have been traverse
     * @see #helperIterateDescendants(Node, Predicate)
     * @implSpec by default, only used by other helpers
     */
    protected boolean helperIterateAll(Predicate<? super Node<T>> pred){
        Node<T> head =  getTreeHeadNode();
        if(pred.test(head)) return true;
        else return helperIterateDescendants(head, pred);
    }

    /**
     * Iterates through each descendant of the specified {@code parent} and applies the specified {@code predicate}
     * on each {@code node}. If the predicate is matched, iteration stops and the method  returns {@code true}
     *
     * @param pred the predicate to apply to each node. If return true, will causes iteration to stop
     * @return true if a descendant matched the predicate, false if not and all
     * descendants have been traversed
     * @implNote Duplicate Nodes: If multiple object's share the same specified {@code target}, then the first object received
     * will be returned. Which object is retrieved first is an implementation detail, and doesn't promise to remain
     * consistent in the face of search pattern changes.
     * <p>
     * Search Pattern: This method iteratively checks all nodes for a match. The "head" of the search begins at the
     * specified {@code parent}. The method checks the head's immediate children, then sets the head to be at it's first child
     * This continues as the method goes deeper, until a dead end is reached. At this point the method has two
     * recovery behaviors. (1) Go to a side node (set the heads to it's next {@code non-null} sibling node). If that fails
     * (2) Reset head to parent, then set the head the next {@code non-null} sibling node.
     * <p>
     * If either of these behaviors are successful, the method continues to search deeper. When these recovery
     * can no longer produce a {@code non-null} result, the method fails and returns {@code false
     * <p>
     * Consistency: This method doesn't aim to be particularly performant. However, this method may change in the
     * future for faster means of acquiring nodes. It's not a priority though.
     * @implSpec by default, only used by other helpers. Override this method to change how iteration occurs for
     * any method that potentially requires traversing more than one layer of the {@code tree}. This method embodies
     * the logic of how this class traverses.
     * <p>
     * If iteration doesn't stop when the predicate returns true, that may impact other methods relying on the behavior.
     * However, a given predicate doesn't need to attempt to return true.
     */
    protected boolean helperIterateDescendants(Node<T> parent, Predicate<? super Node<T>> pred) { //used in multiple places
        Node<T> node = parent;

        while (true) {
            //1.) define search target
            List<Node<T>> search = node.getChildNodes();

            //1.A?) manage backtracking
            int backTrackIndex = 0;
            while (search == null || search.size() < 1) {
                Node<T> backTrack = node.getParentNode();
                if (backTrack == null) return false;
                search = backTrack.getChildNodes();
                backTrackIndex++;
                if (backTrackIndex < search.size()) {

                    Node<T> sidePath = search.get(backTrackIndex);
                    if (sidePath != null) {
                        node = sidePath;
                        search = node.getChildNodes();
                    }
                } else {//no side paths found
                   //reset tracking
                    backTrackIndex = 0;

                    //go to my parents next sibling
                    while (true) {
                        Node<T> btParent = backTrack.getParentNode();
                        if (btParent == null) return false;
                        List<Node<T>> btSiblings = btParent.getChildNodes();
                        int nextIndex = 1 + ListUtil.getReferenceIndex(btSiblings, backTrack);
                        if (nextIndex < btSiblings.size()) {

                            node = btSiblings.get(nextIndex);
                            search = node.getChildNodes();
                            break;
                        } else {//try search from higher
                            backTrack = backTrack.getParentNode();
                            if (backTrack == null) return false;
                        }
                    }
                }
            }

            //2.) check nodes
            for (Node<T> child : search) {
                if (pred.test(child)){
                    return true;
                }
            }

            //3.) delve deeper
            Node<T> deeper = search.get(0); //only delves at "0" and relies on backtracking to correct targets
            if (deeper != null) {
                node = deeper;
            }
        }
    }

}
