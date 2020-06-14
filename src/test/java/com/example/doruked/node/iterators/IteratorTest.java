package com.example.doruked.node.iterators;

import com.example.doruked.Setup;
import com.example.doruked.Setup.TestNode;
import com.example.doruked.node.mynodes.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Contains common test conditions iterators may want to test
 *
 * @implSpec (1) this is not representative of all the conditions an
 * iterator wants to test. Such as, the specifics of iteration.
 * (2) More tests may be added as seen necessary
 */
public interface IteratorTest {

    //next

    void test_that_next_visits_every_node();

    void test_that_next_creates_a_visit_count_that_equals_the_tree_size();

    /**
     * @throws UnsupportedOperationException (1) if the iterator does not support iterating
     * from a non-head position (2) the default implementation of this method is not overridden
     */
    default void test_that_traversal_iterates_an_amount_equal_to_the_remaining_nodes_from_its_specified_start() {
        throw new UnsupportedOperationException("This method is not implemented yet");
    }

    //hasNext

    void test_hasNext_returns_false_when_there_is_not_a_next_node();

    void test_hasNext_is_true_at_a_count_equal_to_the_tree_size();


//inner class

    /**
     * Implements the tests within {@link IteratorTest} in exchange
     * for implementing some simpler methods
     *
     * @implSpec the required methods to implement may grow if {@code IteratorTest}
     * acquires more methods.
     */
    interface IDefault<TData, TIterator extends NodeIterator<Node<TData>>> extends IteratorTest {


        TestNode<TData> getHead();

        NodeIterator<Node<TData>> getIterator();

        Setup<TestNode<TData>, TData> treeGenerator();

        /**
         * creates an instance of the tested iterator that starts at the specified {@code initial}
         */
        TIterator createIterator(Node<TData> initial);

        void setIterator(TIterator iterator);

        @Override
        default void test_that_next_creates_a_visit_count_that_equals_the_tree_size() {
            AtomicInteger count = new AtomicInteger();
            Supplier<Boolean> condition = notExceedSize(count, treeSize());

            helperTest_iteration_of_all_members_by_condition(count, condition);
        }

        @Override
        default void test_hasNext_is_true_at_a_count_equal_to_the_tree_size() {
            AtomicInteger counter = new AtomicInteger();
            Supplier<Boolean> condition = () -> getIterator().hasNext();

            helperTest_iteration_of_all_members_by_condition(counter, condition);
        }

        /**
         * @implNote testing strategy relies on each node containing unique data
         */
        @Override
        default void test_that_next_visits_every_node() {
            AtomicInteger count = new AtomicInteger();
            Set<TData> visited = new HashSet<>();
            List<Node<TData>> failed = new ArrayList<>();

            Supplier<Boolean> condition = notExceedSize(count, treeSize());

            Consumer<Node<TData>> verify = next -> {
                TData id = next.getData();
                if (!visited.add(id)) failed.add(next);
            };

            helper_iterate_if_condition(count, condition, verify);

            assertEquals(0, failed.size());
        }

        @Override
        default void test_hasNext_returns_false_when_there_is_not_a_next_node() {
            getHead().getChildNodes().clear();
            getHead().setParent(null);

            NodeIterator<Node<TData>> iterator = createIterator(getHead());
            iterator.next();

            assertFalse(iterator.hasNext());
        }

        //helper


        private int treeSize() {
            return treeGenerator().getTree().size();
        }

        private void helperTest_iteration_of_all_members_by_condition(AtomicInteger counter, Supplier<Boolean> condition) {
            //setup
            int size = treeGenerator().getTree().size();
            Consumer<Node<TData>> ignore = e -> {
            };

            //test
            helper_iterate_if_condition(counter, condition, ignore);
            assertEquals(size, counter.get());
        }

        private void helper_iterate_if_condition(AtomicInteger count, Supplier<Boolean> condition, Consumer<Node<TData>> examineNode) {
            if (getIterator() == null) setIterator(createIterator(getHead()));
            try {
                while (condition.get()) {
                    Node<TData> next = getIterator().next();
                    examineNode.accept(next);
                    count.incrementAndGet();
                }
            } catch (NoSuchElementException e) {
                //iterator traversed all nodes. expected exception
            }
        }

        /**
         * @implSpec allows count to be bigger than size to an extent, so it can be
         * checked later whether more was counted than it should
         */
        private Supplier<Boolean> notExceedSize(AtomicInteger count, int size) {
            int tooBig = size + 5;
            return () -> count.get() < tooBig;
        }

    }

    /**
     * Implements most of the methods belonging to {@link IteratorTest}. For methods that cannot be
     * implemented(due to requiring iterator specific behavior), a helper method will be added if possible.
     *
     * @implSpec The intended use is for specific iterator tests to store and delegate to
     * an instance of this class.
     *
     * @param <TData> the data contained by iterated nodes
     * @param <TIterator> the type of iterator to test
     */
    class Default<TData, TIterator extends NodeIterator<Node<TData>>> implements IteratorTest {

        private TIterator iterator;
        private Setup<TestNode<TData>, TData> treeGenerator;
        private TestNode<TData> head;
        private Function<Node<TData>, TIterator> createIterator;

        public Default(TIterator iterator, Setup<TestNode<TData>, TData> treeGenerator, Function<Node<TData>, TIterator> createIterator) {
            this.iterator = iterator;
            this.treeGenerator = treeGenerator;
            this.head = treeGenerator.getHead();
            this.createIterator = createIterator;
        }

        @Override
        public void test_that_next_creates_a_visit_count_that_equals_the_tree_size() {
            AtomicInteger count = new AtomicInteger();
            Supplier<Boolean> condition = notExceedSize(count, treeSize());

            helperTest_iteration_of_all_members_by_condition(count, condition);
        }

        @Override
        public void test_hasNext_is_true_at_a_count_equal_to_the_tree_size() {
            AtomicInteger counter = new AtomicInteger();
            Supplier<Boolean> condition = () -> iterator.hasNext();

            helperTest_iteration_of_all_members_by_condition(counter, condition);
        }

        /**
         * @implNote testing strategy relies on each node containing unique data
         */
        @Override
        public void test_that_next_visits_every_node() {
            AtomicInteger count = new AtomicInteger();
            Set<TData> visited = new HashSet<>();
            List<Node<TData>> failed = new ArrayList<>();

            Supplier<Boolean> condition = notExceedSize(count, treeSize());

            Consumer<Node<TData>> verify = next -> {
                TData id = next.getData();
                if (!visited.add(id)) failed.add(next);
            };

            helper_iterate_if_condition(count, condition, verify);

            assertEquals(0, failed.size());
        }


        @Override
        public void test_hasNext_returns_false_when_there_is_not_a_next_node() {
            TestNode<TData> single = new TestNode<>(null, null, null);
            NodeIterator<Node<TData>> iterator = createIterator.apply(single);
            iterator.next();

            assertFalse(iterator.hasNext());
        }


        //helper

        /**
         * Helps implement
         * {@link IteratorTest#test_that_traversal_iterates_an_amount_equal_to_the_remaining_nodes_from_its_specified_start()}
         *
         * @param initial  the node to start iteration from
         * @param expected the expected number of nodes that will be traversed
         */
        public void
        helperTest_that_traversal_iterates_an_amount_equal_to_the_remaining_nodes_from_its_specified_start
        (Node<TData> initial, int expected)
        {
            iterator = createIterator.apply(initial);
            AtomicInteger count = new AtomicInteger();
            Supplier<Boolean> condition = notExceedSize(count, treeSize());
            Consumer<Node<TData>> ignore = e -> { };

            helper_iterate_if_condition(count, condition, ignore);
            assertEquals(expected, count.get());
        }

        private int treeSize() {
            return treeGenerator.getTree().size();
        }

        void helperTest_iteration_of_all_members_by_condition(AtomicInteger counter, Supplier<Boolean> condition) {
            //setup
            int size = treeGenerator.getTree().size();
            Consumer<Node<TData>> ignore = e -> {
            };

            //test
            helper_iterate_if_condition(counter, condition, ignore);
            assertEquals(size, counter.get());
        }

        private void helper_iterate_if_condition(AtomicInteger count, Supplier<Boolean> condition, Consumer<Node<TData>> examineNode) {
            if (iterator == null) iterator = createIterator.apply(head);
            try {
                while (condition.get()) {
                    Node<TData> next = iterator.next();
                    examineNode.accept(next);
                    count.incrementAndGet();
                }
            } catch (NoSuchElementException e) {
                //iterator traversed all nodes. expected exception
            }
        }

        /**
         * @implSpec allows count to be bigger than size to an extent, so it can be
         * checked later whether more was counted than it should
         */
        private Supplier<Boolean> notExceedSize(AtomicInteger count, int size) {
            int tooBig = size + 5;
            return () -> count.get() < tooBig;
        }

    }

}
