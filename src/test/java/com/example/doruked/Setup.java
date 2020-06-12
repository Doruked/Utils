package com.example.doruked;

import com.example.doruked.node.mynodes.AbstractNode;
import com.example.doruked.node.mynodes.Node;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * This class helps create a testable node tree. It's main purpose is to
 * initialize a tree who'se state is correctly setup for each node.
 *
 * @param <TNode> the type of nodes the tree will contain
 * @param <TData> the type of data contained by the nodes
 * @see Setup.AbInt
 * @see Setup.Int
 */
public abstract class Setup<TNode extends Node<TData>, TData> {

    private TNode head;
    private List<TNode> tree;
    private TNode use;

//public operations

    @SuppressWarnings("unchecked")
    public void createTree() throws Exception {
        //init basic tree
        head = createNode();
        tree = new ArrayList<>();
        tree.add(head);

        List<TNode> layerOne = populate(3); //add once to head
        List<TNode> layerTwo; //add to each layerOne
        List<TNode> layerThree; //add to each layerTwo

        //add to head
        initChildren(head, layerOne);

        //add to layerOne
        for (TNode node : layerOne) {

            layerTwo = populate(2);
            initChildren(node, layerTwo);

            //add to layerTwo
            for (TNode node2 : layerTwo) {
                layerThree = populate(1);
                initChildren(node2, layerThree);
            }
        }
        use = (TNode) head.getChildNodes().get(0);
    }

    public TNode getHead() {
        return head;
    }

    public List<TNode> getTree() {
        return tree;
    }

//abstract


    protected abstract TNode createNode();


//helpers

    private void initChildren(TNode parent, List<TNode> children) {
        setChildren(parent, children);
        for (TNode n : children) {
            setParent(n, parent);
        }
        tree.addAll(children);
    }

    private List<TNode> populate(int amount) {
        List<TNode> layer = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            layer.add(createNode());
        }
        return layer;
    }

    private List<TNode> populate(int amount, TNode parent) {
        List<TNode> layer = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            layer.add(createNode());
        }
        return layer;
    }

    private void addChild(TNode parent, TNode child, int index) {
        List<Node<TData>> list = parent.getChildNodes();
        list.add(index, child);
        setParent(child,parent);
    }

    private void removeChild(TNode node, TNode child) {
        List<Node<TData>> list = node.getChildNodes();
        list.remove(child);
        setParent(node, null);
    }


    private void mockParent(TNode child, TNode parent) {
        when(child.getParentNode()).thenReturn(parent);
    }

    protected abstract void setParent(TNode child, TNode parent);

    protected abstract void setChildren(TNode parent, List<TNode> children);

//inner class

    /**
     * Sets up a tree with nodes that contain unique integers as data
     *
     * @implNote This class is abstract to allow implementors to choose what type of
     * nodes populate the tree.
     * @see Int
     */
    public static abstract class AbInt<TNode extends Node<Integer>> extends Setup<TNode, Integer> {

        private static int id;

        @Override
        public void createTree() throws Exception {
            id = 0; //do before node creation
            super.createTree();
        }
    }

    /**
     * Sets up a tree with nodes that contain unique integers as data
     */
    public static class Int extends AbInt<TestNode<Integer>> {

        private static int id;

        @Override
        protected void setParent(TestNode<Integer> child, TestNode<Integer> parent) {
            child.parent = parent;
        }

        @Override
        protected TestNode<Integer> createNode() {
            return new TestNode<>(id++, null, new ArrayList<>());
        }

        @Override
        protected void setChildren(TestNode<Integer> parent, List<TestNode<Integer>> children) {
            parent.children =  new ArrayList<>(children);
        }
    }

    public static class TestNode<T> extends AbstractNode<T> {

        private T data;
        private Node<T> parent;
        private List<Node<T>> children;

        public TestNode(T data, Node<T> parent, List<Node<T>> children) {
            this.data = data;
            this.parent = parent;
            this.children = children;
        }

        @Override
        protected TestNode<T> create(T data) {
            return new TestNode<>(data, null, new ArrayList<>());
        }

        @Override
        protected Node<T> helperAddChild(Node<T> child, int index) {
          throw new UnsupportedOperationException("This method is not implemented yet");
        }

        @Override
        protected List<Node<T>> hGetChildNodes() {
            return children;
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

        /**
         * {@inheritDoc}
         * @implSpec returns the backing collection to help testing
         * Of note, modifying the collection directly may lead to inconsistencies in tree state.
         */
        @Override
        public List<Node<T>> getChildNodes() {
            return children;
        }

        public void setParent(Node<T> parent){
            this.parent = parent;
        }

//unsupported

        /**
         * @implSpec this is a potentially unsafe operation as it requires logic to manage
         * the state of the child and parent. As such, not implemented to keep this class simple.
         * Implementing this method is necessary to support nodes being modifiable
         */
        @Override
        public void setParentAs(Node<T> node) {
            throw new UnsupportedOperationException("This method is not implemented yet");
        }
    }


}
