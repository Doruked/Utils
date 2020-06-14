package com.example.doruked.node.iterators;

import com.example.doruked.Setup;
import com.example.doruked.Setup.TestNode;
import com.example.doruked.node.iterators.SweepIterator.RemoveOption;
import com.example.doruked.node.mynodes.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SweepIteratorTest implements  IteratorTest{

    private static SweepIterator<Integer> iterator;
    private static TestNode<Integer> head;
    private static Setup.Int treeGenerator;
    private static Default<Integer, SweepIterator<Integer>> defaults;

    @Before
    public void setUp() throws Exception {
        treeGenerator = new Setup.Int();
        treeGenerator.createTree();
        head = treeGenerator.getHead();
        defaults = new Default<>(iterator, treeGenerator, SweepIterator::fromCurrent);
    }

    @After
    public void tearDown(){
        iterator = null; //prevent iterator re-use
    }


    @Test
    public void test_that_next_goes_to_next_sibling_as_first_option() {
        int i = 0;
        List<Node<Integer>> siblings = head.getChildNodes();

        Node<Integer> initial = siblings.get(i);
        Node<Integer> expected = siblings.get(i + 1);

        verifyExpected(initial, expected);
    }

    @Test
    public void test_that_next_goes_to_a_siblings_first_child_as_second_option() {
        //set initial to last child
        List<Node<Integer>> siblings = head.getChildNodes();
        Node<Integer> initial = siblings.get(siblings.size() - 1);

        //ensure some sibling has a child
        Node<Integer> firstChild = null;
        boolean hasChild = false;
        for (Node<Integer> e : siblings) {
            if (e.getChildNodes().size() > 0) {
                hasChild = true;
                firstChild = e.getChildNodes().get(0);
                break;
            }
        }
        if (!hasChild) {
            firstChild = new TestNode<>(-1, initial.getParentNode(), null);
            siblings.get(0).addChild(firstChild);
        }

        //test
        verifyExpected(initial, firstChild);
    }

    @Test
    public void test_that_next_goes_to_a_parents_sibling_child_as_third_option() {
        //set initial to head's last child
        List<Node<Integer>> children = head.getChildNodes();
        Node<Integer> initial = children.get(children.size() - 1);

        //set initial to it's own last child (i.e. head's grandChild) /last doesn't matter/ any will do just need to remove others
        List<Node<Integer>> grandChildren = initial.getChildNodes();
        initial = grandChildren.get(grandChildren.size() -1);

        //ensure initial has no children
        initial.getChildNodes().clear();

        //ensure initial has no siblings
        initial.getSiblingNodes().clear();
        initial.getSiblingNodes().add(initial);

        //ensure head has a sibling
        Node<Integer> parent = head.getParentNode();
        if (parent == null) {
            parent = new TestNode<>(-1, null, new ArrayList<>());
            parent.getChildNodes().add(head);
            head.setParent(parent);

        }
        List<Node<Integer>> siblings = parent.getChildNodes();
        if (siblings.size() < 2) {
            TestNode<Integer> node = new TestNode<>(-1, parent, new ArrayList<>());
            node.getChildNodes().add(new TestNode<>(-1, node, null));
            siblings.add(node);
        }

        //ensure head's sibling has a child
        Node<Integer> expected = addChildIfAbsent(head);

        //test
        verifyExpected(initial, expected);
    }

    @Test
    @Override
    public void test_that_next_creates_a_visit_count_that_equals_the_tree_size() {
        defaults.test_that_next_creates_a_visit_count_that_equals_the_tree_size();
    }

    @Test
    @Override
    public void test_hasNext_is_true_at_a_count_equal_to_the_tree_size() {
        defaults.test_hasNext_is_true_at_a_count_equal_to_the_tree_size();
    }

    @Test
    @Override
    public void test_that_next_visits_every_node() {
        defaults.test_that_next_visits_every_node();
    }

    @Test
    @Override
    public void test_that_traversal_iterates_an_amount_equal_to_the_remaining_nodes_from_its_specified_start() {
        List<Node<Integer>> layerOne = head.getChildNodes();
        Node<Integer> initial = layerOne.get(layerOne.size() - 1);

        int skip = layerOne.size() + 1 - 1; // +1 because a child will be starting location, -1 because skips head
        int treeSize = treeSize();
        int expected = treeSize - skip;

        defaults.helperTest_that_traversal_iterates_an_amount_equal_to_the_remaining_nodes_from_its_specified_start
        (initial, expected);
    }

    @Test
    @Override
    public void test_hasNext_returns_false_when_there_is_not_a_next_node() {
        defaults.test_hasNext_returns_false_when_there_is_not_a_next_node();
    }

    //helpers

    private int treeSize(){
       return treeGenerator.getTree().size();
    }

    private Node<Integer> initTree(Node<Integer> initial) {
        iterator = SweepIterator.fromCurrent(initial, RemoveOption.NORMAL);
        iterator.next();
        return iterator.next();
    }

    private void verifyExpected(Node<Integer> initial, Node<Integer> expected) {
        Node<Integer> next = initTree(initial);
        assertEquals(next, expected);
    }

    private Node<Integer> addChildIfAbsent(Node<Integer> head) {
        List<Node<Integer>> siblings = new ArrayList<>(head.getSiblingNodes());
        siblings.remove(head);
        return addChildIfAbsent(siblings);
    }

    private Node<Integer> addChildIfAbsent(List<Node<Integer>> parents){
        Node<Integer> firstChild = null;
        boolean hasChild = false;
        for (Node<Integer> e : parents) {
            if (e.getChildNodes().size() > 0) {
                hasChild = true;
                firstChild = e.getChildNodes().get(0);
                break;
            }
        }
        if (!hasChild) {
            Node<Integer> node = parents.get(0);
            firstChild = new TestNode<>(-1, node, null);
            node.getChildNodes().add(firstChild);
        }

        return firstChild;
    }

}