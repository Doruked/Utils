package com.example.doruked.Node.MyNodes;

import com.example.doruked.ListUtil;
import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A fairly basic implementation of {@link AbstractNode}.
 * <p>
 * Published Data:
 * (1)this class does not publish it's own children, but rather a copy (see: {@link #getChildNodes()}.
 * (2) It "may" publish it's siblings. This depends on the parent's implementation, because this class just returns
 * the info from it's parent (see: {@link #getSiblingNodes()}.
 * (3) does publish it's parent node. which is expected
 * <p>
 * Safety Notes:
 * it is best to generally keep this node in trees that only contain it's type.
 * This is because other nodes may have a different protocols for state management when modifying nodes.
 * Operations like {@code adding} and {@code removing} are a cooperative effort to maintain proper
 * state across different nodes.
 *
 * @param <T> the type of data contained by the node
 */
//allows duplicates
@NotThreadSafe
public class MyNode<T> extends AbstractNode<T> {

    private T data;
    private Node<T> parent;
    private List<Node<T>> children;

    public MyNode(T data, Node<T> parent, List<Node<T>> children) {
        this.data = data;
        this.parent = parent;
        this.children = children;
    }

    public MyNode(T data) {
        this(data, null, new ArrayList<>());
    }

    public MyNode(T data, Node<T> parent) {
        this(data, parent, new ArrayList<>());
    }

    public MyNode(T data, List<Node<T>> children) {
        this(data, null, children);
    }

//public operations

    /**
     * {@inheritDoc}
     *
     * @implSpec (1) this method is used other methods of this class, and will affect them if overridden.
     * (2) The logic of the method depends assumptions of how this class works. Particularly,
     * when nodes are added, a copy of the node is added not the original. That copy
     * already has it's parent set to the correct value before being added. As such,
     * this method will only add children if the specified {@code node} has a {@code null} parent
     * @see MyNode#helperAddChild(Node, int)
     */
    @Override
    public void setParentAs(Node<T> node) {
        if (parent != null) {
            int myIndex = ListUtil.getReferenceIndex(parent.getChildNodes(), this);
            if (myIndex > -1) parent.removeChild(myIndex);
        }
        parent = node;
        if (parent != null) parent.addChild(this);
    }

    @Override
    public void setData(T t) {
        data = t;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public Node<T> getParentNode() {
        return parent;
    }

    @Override
    public List<Node<T>> getChildNodes() {
        return List.copyOf(children);
    }


//protected

    @Override
    protected List<Node<T>> hGetChildNodes() {
        return children;
    }
    @Override
    protected Node<T> create(T data) {
        return new MyNode<>(data);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index > size()})
     * @return the added node that is a copied from the specified {@code child}
     * @implSpec adds a copy of the specified {@code child}, but with an updated parent.
     * Presently, the nodes share the same children reference but that may change.
     * By default this defines how all adds occur in this class
     */
    @Override
    protected Node<T> helperAddChild(Node<T> child, int index) {
        // if specified child keeps children nodes, they can still affect this class
        Objects.requireNonNull(child);
        Node<T> copy = new MyNode<>(child.getData(), this, child.getChildNodes());

        children.add(index, copy);
        return copy;
    }

}
