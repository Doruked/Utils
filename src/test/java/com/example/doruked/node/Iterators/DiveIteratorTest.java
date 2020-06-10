package com.example.doruked.node.Iterators;

import com.example.doruked.ListUtil;
import com.example.doruked.Setup;
import com.example.doruked.Setup.TestNode;
import com.example.doruked.node.mynodes.Node;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
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
        Node<Integer> next = initIterator(initial);

        assertEquals(initial.getChildNodes().get(0), next);
    }

    @Test
    public void test_next_returns_next_sibling_as_second_option() {
        //step 1: set initial to childless node
        Node<Integer> initial = findChildlessNode(head);

        //step 2: ensure initial has next sibling
        Node<Integer> expected = getNextOrAdd(initial);

        //test
        Node<Integer> next = initIterator(initial);
        assertEquals(expected, next);
    }

    @Test
    public void test_next_returns_parents_next_sibling_as_third_option() {
        //step 1: set initial childless node
        Node<Integer> initial = findChildlessNode(head);

        //step 2: ensure initial has next sibling
        trimTo(initial);

        //step 3: ensure parent has next sibling
        Node<Integer> parent = initial.getParentNode();
        Node<Integer> expected = getNextOrAdd(parent);

        //test
        Node<Integer> next = initIterator(initial);
        assertEquals(expected, next);
    }

    @Test
    public void test_next_returns_grandparents_or_higher_next_sibling_as_fourth_option() {
        //step 1: set initial childless node
        Node<Integer> initial = findChildlessNode(head);

        //step 2: ensure initial is the last child
        trimTo(initial);

        //step 3: ensure parent does NOT have next sibling
        Node<Integer> parent = initial.getParentNode();
        trimTo(parent);

        //step 4: ensure grandparent has next sibling
        Node<Integer> grandParent = parent.getParentNode();
        Node<Integer> expected = getNextOrAdd(grandParent);

        //test
        Node<Integer> next = initIterator(initial);
        assertEquals(expected, next);
    }

    @Test
    public void hasNext() {
    }

    @Test
    public void clearData() {
    }


//helpers

    private static Node<Integer> initIterator(Node<Integer> initial) {
        iterator = new DiveIterator<>(initial);
        iterator.next();

        return iterator.next();
    }

    /**
     * @deprecated redundant with {@link ListUtil#trimToReference(List, Object)}
     */
    @Deprecated
    private <T> List<T> trimToReference(List<T> list, T reference) {
        if (list.size() < 1) return Collections.emptyList();
        List<T> removed = new ArrayList<>();
        int i = ListUtil.getReferenceIndex(list, reference);
        while (i != list.size() - 1) {
            T e = list.remove(list.size() - 1);
            removed.add(e);
        }
        return removed;
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

    /**
     * @deprecated redundant with {@link ListUtil#trimTo(List, Object)}
     */
    @Deprecated
    private <T> List<T> trim(List<T> list, int bound) {
        List<T> removed = new ArrayList<>();
        while (bound != list.size() - 1) {
            T e = list.remove(list.size() - 1);
            removed.add(e);
        }
        return removed;
    }

    /**
     * @deprecated redundant with {@link #trimTo(Node)}
     */
    @Deprecated
    private <T> void ensureNodeIsLastChild(Node<T> initial) {
        int i = indexOfReference(initial);
        List<Node<T>> siblings = initial.getSiblingNodes();
        while (i != siblings.size() - 1) {
            siblings.remove(siblings.size() - 1);
        }
    }

    private <T> void trimTo(Node<T> initial) {
        int i = indexOfReference(initial);
        List<Node<T>> siblings = initial.getSiblingNodes();
        while (i != siblings.size() - 1) {
            siblings.remove(siblings.size() - 1);
        }
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

}