package com.example.doruked.node.Iterators;

import com.example.doruked.ListUtil;
import com.example.doruked.Setup;
import com.example.doruked.Setup.TestNode;
import com.example.doruked.node.mynodes.Node;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @implNote
 * The operation {@code next} returns it's previous calculation and stores the current one for later use.
 * As such, tests not only require that the expected node was located in a specific scenario,
 * but that the method exited without throwing an {@code exception} in a subsequent call.
 */
public class DiveIteratorTest {

    private static TestNode<Integer> head;
    private static List<TestNode<Integer>> tree;
    private static Setup<TestNode<Integer>, Integer> treeGenerator;
    private static DiveIterator<Node<Integer>> iterator;
    private static Node<Integer> use;

    @BeforeClass
    public static void setUp() throws Exception {
        treeGenerator = new Setup.Int();
        treeGenerator.createTree();

        head = treeGenerator.getHead();
        use = head.getChild(0);
        tree = treeGenerator.getTree();
    }

    @Test
    public void test_next_returns_child_0_as_first_option() {
        Node<Integer> initial = head;
        Node<Integer> expected = initial.getChildNodes().get(0);

        verifyExpected(initial, expected);
    }

    @Test
    public void test_next_returns_next_sibling_as_second_option() {
        //step 1: set initial to childless node
        Node<Integer> initial = findChildlessNode(head);

        //step 2: ensure initial has next sibling
        Node<Integer> expected = getNextOrAdd(initial);

        //test
        verifyExpected(initial, expected);
    }

    @Test
    public void test_next_returns_parents_next_sibling_as_third_option() {
        //step 1: set initial childless node
        Node<Integer> initial = findChildlessNode(head);

        //step 2: ensure initial does NOT have next sibling
        trimTo(initial);

        //step 3: ensure parent has next sibling
        Node<Integer> parent = initial.getParentNode();
        Node<Integer> expected = getNextOrAdd(parent);

        //test
        verifyExpected(initial, expected);
    }

    @Test
    public void test_next_returns_grandparents_or_higher_next_sibling_as_fourth_option() {
        //step 1: set initial childless node
        Node<Integer> initial = findChildlessNode(head);

        //step 2: ensure initial does NOT have next sibling
        trimTo(initial);

        //step 3: ensure parent does NOT have next sibling
        Node<Integer> parent = initial.getParentNode();
        trimTo(parent);

        //step 4: ensure grandparent has next sibling
        Node<Integer> grandParent = parent.getParentNode();
        Node<Integer> expected = getNextOrAdd(grandParent);

        //test
        verifyExpected(initial, expected);
    }

    @Test
    public void test_hasNext_returns_false_when_next_does_not_exist() {
        TestNode<Integer> single = new TestNode<>(0, null, new ArrayList<>());
        iterator = new DiveIterator<>(single);
        iterator.next();

        assertFalse(iterator.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_that_clearData_is_Unsupported() {
        initIterator(head);
        iterator.clearData();
    }


//helpers

    private static Node<Integer> initIterator(Node<Integer> initial) {
        iterator = new DiveIterator<>(initial);
        iterator.next();

        return iterator.next();
    }

    private <T> Node<T> findChildlessNode(Node<T> head) {
        Node<T> initial = head;
        List<Node<T>> children = initial.getChildNodes();
        while (children.size() > 0 && children.get(0) != null) {
            initial = children.get(0);
            children = initial.getChildNodes();
        }
        return initial;
    }

    private <T> void trimTo(Node<T> reference) {
        List<Node<T>> siblings = reference.getSiblingNodes();
        ListUtil.trimToReference(siblings, reference);
    }

    private Node<Integer> getNextOrAdd(Node<Integer> node) {
        int myIndex = indexOfReference(node);
        List<Node<Integer>> siblings = node.getSiblingNodes();
        if (myIndex >= siblings.size() - 1) {
            TestNode<Integer> added = new TestNode<>(-1, node.getParentNode(), null);
            siblings.add(added);
            return added;
        }
        return siblings.get(myIndex + 1);
    }

    private <T> int indexOfReference(Node<T> reference) {
        return ListUtil.getReferenceIndex(reference.getSiblingNodes(), reference);
    }

    private  void verifyExpected(Node<Integer> initial, Node<Integer> expected){
        Node<Integer> next = initIterator(initial);
        assertEquals(expected, next);
    }

}