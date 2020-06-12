package com.example.doruked.node.iterators;

import com.example.doruked.node.mynodes.Node;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This iterator will check all nodes in a given layer before diving to another node.
 * This means if a node is checked, then all of it's siblings will be checked before any other node is.
 * For each node that is checked, their descendants are not guaranteed to be checked in a time reasonably
 * soon to one another. For example, in this current implementation, the last child of a starting location
 * will have it's descendants checked last, regardless of the size of the tree.
 * In code form, this call is said to be checked last {@code head.getChild(
 * ).childNodes()},
 * where head is the starting location.
 *
 * @param <T> the data contained by nodes
 * @implSpec at present, this class does not safe guard against concurrent modification,
 * nor are the side effects known/detailed. In the future, this class may change to make operations safer.
 */
public class SweepIterator<T> implements NodeIterator<Node<T>> {

    private AtomicBoolean startedFromHead = null; //atomic just so it can start off null
    private volatile Node<T> current;
    private volatile Node<T> lastReturned = null;
    private final SweepIterator.RemoveOption option;
    private boolean modified = false;

    private SweepIterator(Node<T> first, SweepIterator.RemoveOption option) {
        this.current = first;
        this.option = option;
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if the current position is null
     */
    @Override
    public Node<T> next() {
        if(current == null) throw new NoSuchElementException();
        Node<T> next = null;


        //Priority #1: go to next sibling
        List<Node<T>> siblings = current.getSiblingNodes();
        int curIndex = siblings.indexOf(current);

        if(curIndex < siblings.size() -1){
            next = setNext(siblings.get(curIndex + 1)); //can improve will later. can do in 2 lines. also better use sib index
            return next;
        }

        //Priority #2: go to a sibling's child
        List<Node<T>> search = null;
        for (Node<T> n : siblings) {
            search = n.getChildNodes();

            if ((search != null) && search.size() > 0) {
                next = setNext(search.get(0));
                return next;
            }
        }

        //Priority #3: go to a parent's sibling's child (i.e. backtrack then progress)
        Node<T> parent = current.getParentNode();
        while (parent != null) {
            siblings = parent.getSiblingNodes();
            if (siblings != null) {

                int parentIndex = siblings.indexOf(parent);

                for (int i = parentIndex + 1; i < siblings.size(); i++) {
                    search = siblings.get(i).getChildNodes();
                    if (search != null && search.size() > 0) {
                        next =  setNext(search.get(0));
                        return next;
                    }
                }
            }
            parent = parent.getParentNode();
        }

        //Priority #4: At TreeHead, determine if starting or ending
        if (startedFromHead() && (lastReturned == null)) {//i.e. has not moved from head

            search = current.getChildNodes();

            if (search != null && search.size() > 0) {
                next = setNext(search.get(0));
                return next;
            }//else = tree only contains 1 node...no next element
        }
        return setNext(null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void remove() {
        if (lastReturned == null || modified) throw new IllegalStateException();
            if (option == RemoveOption.NORMAL) lastReturned.removeParent();
            if (option == RemoveOption.PLUCK) lastReturned.pluckNode();
            modified = true;
    }

    @Override
    public void clearData() {
        if (lastReturned == null) throw new IllegalStateException();
        lastReturned.setData((T) null);
    }

//helpers

    private boolean notEmpty(List<?> list){
        return (list != null) && list.size() > 0;
    }

    private Node<T> setNext(Node<T> next) {
        lastReturned = current;
        current = next;
        modified = false;
        return lastReturned;
    }

    private void setStartedFromHead() { // not good because if you do previous and return to head you can get a false result
        if (startedFromHead == null) {
            startedFromHead = new AtomicBoolean();
            boolean value;

            value = (current.getParentNode() == null);
            startedFromHead.set(value);
        } //else do nothing, only set this once per iterator
    }

    private boolean startedFromHead(){
        return startedFromHead.get();
    }

//static

    /**
     * @param start the node to move from
     * @return the node previous of the specified {@code start} or null if non-existent
     * @implNote this method relies on knowing how {@link #next()} traverses. If it's behavior changes,
     * this method will be inaccurate.
     *
     * Of note, if {@code start} equals the tree head, a call of this method will return the tree head.
     */
    public static <T> Node<T> previousNode(Node<T> start) {
        Node<T> parent = start.getParentNode();
        if (parent == null) return null;

        //Option #1: return older sibling
        List<Node<T>> sibs = start.getSiblingNodes();
        int index = sibs.indexOf(start);
        if (index > 0) return sibs.get(index - 1);

            //Option #2: return youngest parent or parent sibling
        else {
            sibs = parent.getSiblingNodes();
            return sibs.get(sibs.size() -1);
        }
    }

    /**
     * Creates an iterator that starts at the specified {@code first}
     *
     * @param first the node to start from/be returned first
     * @param option how the iterator should perform removes
     * @param <T> the data contained by nodes
     * @return a new node iterator
     */
    public static <T> SweepIterator<T> fromCurrent(Node<T> first, SweepIterator.RemoveOption option){
        SweepIterator<T> iterator = new SweepIterator<>(first, option);
        iterator.setStartedFromHead();
        return iterator;
    }

    /**
     * Creates an iterator that will start at the {@code head} of the tree that the
     * specified {@code treeMember} belongs to.
     *
     * @param treeMember the node belonging to the tree that will be iterated from
     * @param option how the iterator should perform removes
     * @param <T> the data contained by nodes
     * @return a new node iterator
     */
    public static <T> SweepIterator<T> fromHead(Node<T> treeMember, SweepIterator.RemoveOption option){
        SweepIterator<T> iterator = new SweepIterator<>(treeMember.getTreeHeadNode(), option);
        iterator.setStartedFromHead();
        return iterator;
    }

    /**
     * Creates an iterator that will start at the {@code head} of the tree that the
     * specified {@code treeMember} belongs to.
     *
     * @param treeMember the node belonging to the tree that will be iterated from
     * @param <T> the data contained by nodes
     * @return a new node iterator
     */
    public static <T> SweepIterator<T> fromHead(Node<T> treeMember){
        return fromHead(treeMember, SweepIterator.RemoveOption.NORMAL);
    }

    /**
     * Creates an iterator that starts at the specified {@code first}
     *
     * @param first the node to start from/be returned first
     * @param <T> the data contained by nodes
     * @return a new node iterator
     */
    public static <T> SweepIterator<T> fromCurrent(Node<T> first) {
        return fromCurrent(first, SweepIterator.RemoveOption.NORMAL);
    }

//inner class

    public enum RemoveOption {

        /** removes the node relation to tree (this includes it's descendants) */
        NORMAL,

        /** @see Node#pluckNode()  */
        PLUCK
    }
}