package com.example.doruked.Node.MyNodes;

import com.example.doruked.ListUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class AbstractNodeTest {

    private static Dummy<Integer> head;
    private static List<Dummy<Integer>> tree;
    private static int id;
    private static Dummy<Integer> use;
    private static final Dummy<Integer> NULL = null;
    private static final boolean CONTINUE = false; //@implNot - methods that iterate layers continue when passed "false"
    private static final boolean STOP = true; //should be inverse of field "CONTINUE"

//setup

    @Before
    public  void setUp() throws Exception {
        //init basic tree
        id = 0; //do before node creation
        head = createNode();
        tree = new ArrayList<>();
        tree.add(head);

        List<Dummy<Integer>> layerOne = populate(3); //add once to head
        List<Dummy<Integer>> layerTwo; //add to each layerOne
        List<Dummy<Integer>> layerThree; //add to each layerTwo

        //add to head
        initChildren(head, layerOne);

        //add to layerOne
        for (Dummy<Integer> node : layerOne) {

            layerTwo = populate(2);
            initChildren(node, layerTwo);

            //add to layerTwo
            for (Dummy<Integer> node2 : layerTwo) {
                layerThree = populate(1);
                initChildren(node2, layerThree);
            }
        }
        use = (Dummy<Integer>) head.getChildNodes().get(0);
    }

    private static void initChildren(Dummy<Integer> node, List<Dummy<Integer>> list){
        node.children = new ArrayList<>(list);
        for (Node<Integer> n : list) {
            setParent((Dummy<Integer>)n, node);
        }
        tree.addAll(list);
    }

    private static List<Dummy<Integer>> populate(int amount){
        List<Dummy<Integer>> layer = new ArrayList<>();
        for(int i = 0; i < amount; i++){
            layer.add(createNode());
        }
        return layer;
    }

    private static Dummy<Integer> createNode(){
        return new Dummy<>(id++, null, new ArrayList<>());
    }

    private static void setParent(Dummy<Integer> child, Dummy<Integer> parent){
        child.parent = parent;
    }

    private static void addChild(Dummy<Integer> parent, Dummy<Integer> child, int index) {
        List<Node<Integer>> list = parent.getChildNodes();
        list.add(index, child);
        setParent(child,parent);
    }

    private static void removeChild(Dummy<Integer> node, Dummy<Integer> child) {
        List<Node<Integer>> list = node.getChildNodes();
        list.remove(child);
        setParent(node, null);
    }

//-----------Tests-------------------------------


//SetParentAs

    @Test
    public void test_parent_is_set_null(){
        use.setParentAs(NULL);
        assertNull(use.getParentNode());
    }

    @Test //the tested method is not currently implemented by AbstractNode. rather by the dummy
    public void test_that_old_parent_loses_child_when_child_changes_parents(){
        Node<Integer> parent = use.getParentNode();
        use.setParentAs(NULL);

        List<Node<Integer>> matches = new ArrayList<>();
        for(Node<Integer> n: parent.getChildNodes()){
            if(n.sameData(use))
                matches.add(n);
        }
        assertEquals(0, matches.size());
    }

    @Test
    //does not test if it receives the correct node. only that it recieves a node
    public void test_new_parent_gets_child_when_node_sets_it_as_parent(){
        Dummy<Integer> newParent = createNode();
        int childSize = newParent.getChildNodes().size();
        use.setParentAs(newParent);

        assertEquals(childSize + 1, newParent.getChildNodes().size());
    }

    @Test
    public void test_that_lost_child_equals_the_calling_node(){
        Node<Integer> parentNode = use.getParentNode();
        List<Node<Integer>> had = ListUtil.getIf(e-> e.sameData(use), parentNode.getChildNodes());

        use.setParentAs(createNode());

        List<Node<Integer>> altered = ListUtil.getIf(e-> e.sameData(use), parentNode.getChildNodes());
        assertEquals(had.size() -1, altered.size());
    }

    @Test
    public void test_that_only_one_child_is_lost(){
        helper_test_that_only_one_child_is_lost(use);
    }

    @Test
    public void test_that_only_one_child_is_lost_when_containing_duplicates(){
        Dummy<Integer> duplicate = createNode();
        Node<Integer> parentNode = use.getParentNode();

        //add duplicates
        parentNode.addChild(duplicate);
        parentNode.addChild(duplicate);
        Node<Integer> check = parentNode.addChild(duplicate);

        helper_test_that_only_one_child_is_lost(check);
    }

    private void helper_test_that_only_one_child_is_lost(Node<Integer> target){
        Node<Integer> parentNode = target.getParentNode();
        List<Node<Integer>> had = ListUtil.getIf(e-> e.sameData(target), parentNode.getChildNodes());

        target.setParentAs(createNode());

        List<Node<Integer>> altered = ListUtil.getIf(e-> e.sameData(target), parentNode.getChildNodes());
        assertEquals(had.size() -1, altered.size());
    }

    @Test
    public void test_parent_children_are_same_size_when_parent_is_set_as_current_parent() {
        Node<Integer> parent = use.getParentNode();
        List<Node<Integer>> had = new ArrayList<>(parent.getChildNodes());

        use.setParentAs(parent);
        parent = use.getParentNode();

        assertEquals(had.size(), parent.getChildNodes().size());
    }


//removeParent

    @Test
    public void test_that_removed_parent_is_null(){
        use.removeParent();
        assertNull(use.getParentNode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_that_removeParent_delegates_to_setParentAs_accepting_null() {
        Dummy<Integer> spy = Mockito.spy(createNode());
        spy.removeParent();
        verify(spy, times(1)).setParentAs(NULL);
    }

//Add child

    @Test
    public void test_that_parents_state_is_updated_when_node_is_accepted(){
        List<Node<Integer>> children = use.getChildNodes();
        int preSize = children.size();

        use.addChild(createNode());

        int postSize = children.size();
        assertEquals(postSize, preSize + 1 );
    }

    @Test
    public void test_that_parents_state_is_updated_when_data_is_accepted(){
        List<Node<Integer>> children = use.getChildNodes();
        int preSize = children.size();

        use.addChild(2);

        int postSize = children.size();
        assertEquals(postSize, preSize + 1 );
    }

    @Test
    public void test_that_child_state_is_updated_when_node_is_accepted() {
        Node<Integer> child = use.addChild(createNode());
        assertEquals(use, child.getParentNode());
    }

    @Test
    public void test_that_child_state_is_updated_when_data_is_accepted(){
        Node<Integer> child = use.addChild(2);
        assertEquals(use, child.getParentNode());
    }

    @Test
    public void test_that_node_is_added_at_index(){
       helperTest_that_added_at_index((use, index) -> use.addChild(createNode(), index));
    }

    @Test
    public void test_that_data_is_added_at_index() {
        helperTest_that_added_at_index((use, index) -> use.addChild(2, index) );
    }

    private static void helperTest_that_added_at_index(BiFunction<Node<Integer>, Integer, Node<Integer>> creator){
        List<Node<Integer>> children = use.getChildNodes();

        //increasing size so can add at specific index
        Dummy<Integer> dud = createNode();
        children.add(dud);
        children.add(dud);
        children.add(dud);

        //adding target child
        int index = 1;
        Node<Integer> child = creator.apply(use, 1);

        //check children for child
        assertEquals(child, children.get(index));
    }

    @Test
    public void test_that_added_node_appends(){
        helperTest_that_added_is_appended(use-> use.addChild(2));
    }

    @Test
    public void test_that_added_data_appends() {
        helperTest_that_added_is_appended(use-> use.addChild(createNode()));
    }

    private static void helperTest_that_added_is_appended(UnaryOperator<Node<Integer>> creator){
        List<Node<Integer>> children = use.getChildNodes();
        Node<Integer> child = creator.apply(use);
        assertEquals(child, children.get(children.size()-1));
    }

//Remove Child

    @Test
    public void test_removed_child_was_at_specified_index() {
        List<Node<Integer>> children = use.getChildNodes();
        children.add(createNode());
        children.add(createNode());

        int index = children.size() - 2;
        Node<Integer> target = children.get(index);
        Node<Integer> removed = use.removeChild(index);

        assertEquals(target, removed);
    }

    @Test
    public void test_removed_child_matches_predicate() {
        Dummy<Integer> target = createNode();
        use.getChildNodes().add(target);

        Node<Integer> removed = use.removeChild(e -> e.equals(target));
        assertEquals(target, removed);

    }

    @Test
    //relies on fact that each node has an incremental id
    public void test_removed_child_was_first_to_match_predicate(){
        List<Node<Integer>> children = use.getChildNodes();
        Dummy<Integer> target = createNode();
        children.add(target);
        children.add(createNode());
        children.add(createNode());

        Node<Integer> removed = use.removeChild(e -> e.getData() >= target.getData());
        assertEquals(target, removed);
    }

    @Test
    public void test_return_matches_the_removed_child(){
        Node<Integer> target = use.getChildNodes().get(0);
        Node<Integer> removed = use.removeChild(0);

        assertSame(target, removed);
    }

    @Test
    public void test_removed_child_at_index_updates_its_state(){
        int index = 0;
        Node<Integer> target = use.getChildNodes().get(index);
        use.removeChild(index);

        assertEquals(NULL, target.getParentNode());
    }

    @Test
    public void test_removed_child_that_matched_predicate_updates_its_state(){
        Node<Integer> target = use.getChildNodes().get(0);
        Node<Integer> removed = use.removeChild(e -> e.sameData(target));

        assertEquals(NULL, removed.getParentNode());
    }

//Remove All Children

    @Test
    public void test_removed_children_all_match_predicate() {
        List<Node<Integer>> children = use.getChildNodes();
        Dummy<Integer> duplicate = createNode();

        //add nodes to remove
        children.add(duplicate);
        children.add(duplicate);
        children.add(duplicate);

        //remove nodes
        List<Node<Integer>> removed = use.removeAllChildren(e -> e.equals(duplicate));

        //verify removed
        List<Node<?>> failed = getIf(removed, e-> !e.equals(duplicate));
        assertEquals(0, failed.size());
    }

    @Test
    public void test_removed_children_all_update_state(){
        List<Node<Integer>> removed = head.removeAllChildren(e -> true);

        List<Node<Integer>> failed = getIf(removed, e -> head.equals(e.getParentNode()));
        assertEquals(0, failed.size());
    }

    @Test
    public void test_removed_children_are_no_longer_stored() {
        head.removeAllChildren(e -> true);
        assertEquals(0, head.getChildNodes().size());
    }

//Pluck Node

    @Test
    public void test_plucked_node_loses_parent(){
        Node<Integer> plucked = use.pluckNode();
        assertNull(plucked.getParentNode());
    }

    @Test
    public void test_plucked_node_loses_children(){
        Node<Integer> plucked = use.pluckNode();
        assertEquals(0, plucked.getChildNodes().size());
    }

    @Test
    public void test_plucked_node_replaced_by_its_children() {
        setupThenAssertPluckNodeTest(use, (parent, children) -> {
                    List<Node<Integer>> moved = new ArrayList<>();

                    for(Node<Integer> c: children){
                        if(parent.getChildNodes().contains(c)){
                            moved.add(c);
                        }
                        assertEquals(moved.size(), children.size());
                    }
                }
        );
    }

    @Test
    public void test_children_update_state_after_parent_is_plucked() {
        setupThenAssertPluckNodeTest(use, (parent, children) -> {
        //assertion
        List<Node<Integer>> failed = getIf(children, e -> e.getParentNode().equals(parent));
        assertEquals(0, failed.size());
        });
    }

    @Test
    public void test_plucked_node_updates_has_no_parent(){
        setupThenAssertPluckNodeTest(use, (parent, children) ->
        assertEquals(NULL, use.getParentNode()));
    }

    @Test
    public void test_plucked_node_updates_has_no_children() {
        setupThenAssertPluckNodeTest(use, (ignore1, ignore2) ->
        assertEquals(0, use.getChildNodes().size()));
    }

    private void setupThenAssertPluckNodeTest(Dummy<Integer> target, BiConsumer<Node<Integer>, List<Node<Integer>>> assertion) {
        Node<Integer> oldParent = target.getParentNode();
        List<Node<Integer>> oldChildren = target.getChildNodes();
        target.pluckNode();

        assertion.accept(oldParent, oldChildren);
    }

//ForEach

    @Test
    public void test_forEach_traveses_all_nodes() {
        AtomicInteger count = new AtomicInteger();

        use.forEach(e-> count.incrementAndGet());
        assertEquals(tree.size(), count.get());
    }

    @Test
    public void test_forEach_delegates_to_helperForEachDescendant(){
        Dummy<Integer> spy = spy(createNode());
        spy.forEach((e)-> {});
        verify(spy, times(1)).helperForEachDescendant(any(), any());
    }

//getTreeHeadNode

    @Test
    public void test_siblings_have_the_same_tree_head() {
        List<Node<Integer>> siblings = use.getSiblingNodes();
        getIfThenAssertEmpty(siblings, e -> !e.getTreeHeadNode().equals(head));
    }

    @Test
    public void test_children_have_the_same_tree_head(){
        List<Node<Integer>> children = use.getChildNodes();
        getIfThenAssertEmpty(children, e -> !e.getTreeHeadNode().equals(head));
    }

    @Test
    public void test_tree_head_does_not_have_parent(){
        Node<Integer> head = use.getTreeHeadNode();
        assertEquals(NULL, head.getParentNode());
    }

//GetNode

    @Test
    public void test_return_matches_predicate(){
        Node<Integer> target = use.addChild(createNode());
        Node<Integer> got = use.getNode(e -> e.equals(target));

        assertEquals(target, got);
    }

    @Test
    public void test_return_is_first_to_match_predicate(){
        List<Node<Integer>> children = use.getChildNodes();
        Dummy<Integer> target = createNode();

        //setting up multiple predicate matches
        children.add(target);
        children.add(createNode());
        children.add(createNode());

        //Verify retrieved node
        Node<Integer> got = use.getNode(e -> e.getData() >= target.getData());
        assertEquals(target, got);
    }

    @Test
    public void test_predicate_is_used_when_matches_exist(){
        Dummy<Integer> target = createNode();
        use.getChildNodes().add(target);

        Node<Integer> got = use.getNode(e -> e.equals(target));
        assertEquals(got, target);
    }

    @Test
    public void test_null_is_returned_if_predicate_does_not_match(){
        Dummy<Integer> dud = createNode();
        Node<Integer> got = use.getNode(e -> e.equals(dud));
        assertEquals(NULL, got);
    }

//GetNodeIf

    @Test
    public void test_all_returned_nodes_match_predicate(){
        List<Node<Integer>> children = use.getChildNodes();
        Dummy<Integer> target = createNode();

        //setting up multiple predicate matches
        children.add(target);
        children.add(createNode());
        children.add(createNode());

        //Verify retrieved node
        Predicate<Node<Integer>> pred = e -> e.getData() >= target.getData();
        List<Node<Integer>> matches = use.getNodeIf(pred);
        List<Node<Integer>> verify = ListUtil.getIf(pred, matches);
        assertEquals(matches.size(), verify.size());
    }

    @Test
    public void test_getNodeIf_returns_empty_list_if_none_match_predicate(){
        Dummy<Integer> dud = createNode();
        List<Node<Integer>> got = use.getNodeIf(e -> e.equals(dud));
        assertEquals(0, got.size());
    }

//getDescending

    @Test
    public void test_that_getDescending_delegates(){
        Dummy<Integer> spy = spy(createNode());

        spy.getDescendingNodes();
        verify(spy, times(1)).helperGetDescending(any(), any());
    }

    @Test
    public void test_that_getDescending_collects_nodes() {
        if (use.getChildNodes().size() < 1) fail("Not enough descendants to check");
        else assertTrue(use.getDescendingNodes().size() > 1);
    }

//getDescendingIf

    @Test
    public void test_that_descendants_match_the_predicate(){
        Dummy<Integer> target = createNode();
        use.getChildNodes().add(target);

        Predicate<Node<Integer>> pred = e-> e.equals(target);
        List<Node<Integer>> got = use.getDescendingNodeIf(pred);
        getIfThenAssertEmpty(got, pred.negate());
    }

    @Test
    public void test_that_all_descendants_who_match_are_retrieved(){
        int amount = 3;
        Dummy<Integer> target = createNode();
        ListUtil.addAmount(use.getChildNodes(), amount, target);

        List<Node<Integer>> got = use.getDescendingNodeIf(e -> e.equals(target));
        assertEquals(amount, got.size());
    }

    @Test
    public void test_getDescendants_wont_match_self_with_predicate(){
        List<Node<Integer>> got = use.getDescendingNodeIf(e -> e == use);
        assertEquals(0, got.size());
    }

    @Test
    public void test_getDescendants_does_not_include_self(){
        List<Node<Integer>> got = use.getDescendingNodeIf(e -> true);
        getIfThenAssertEmpty(got, e-> e == use);
    }

    @Test
    public void test_predicate_is_applied_to_multiple_layers(){
        Dummy<Integer> target = createNode();

        //adding target to select layers
        Node<Integer> parent = use.getParentNode();
        if(parent != null){
            parent.getChildNodes().add(target);
        }
        List<Node<Integer>> children = use.getChildNodes();
        children.add(target);

        Node<Integer> child = children.get(0);
        child.getChildNodes().add(target);

        //verify retrieval
        List<Node<Integer>> got = use.getDescendingNodeIf(e -> e.equals(target));
        assertTrue(got.size() > 1);
    }

//GetDataIf

    @Test
    public void test_getDataIf_delegates_to_helperGetIf(){
        AbstractNode<Integer> mock = spy(createNode());

        mock.getDataIf(NULL);
        verify(mock, times(1)).helperGetIf(any());
    }


//--------------------helpers----------------------------------


//helperRemove

    @Test
    public void test_that_helperRemove_delegates_to_setParentAs_null() {
        helperDelegationTest(Dummy::removeParent, e -> e.setParentAs(NULL));
    }

//helperGetIf

    @Test
    public void test_delegation_to_forEach(){
        BiConsumer<Node<Integer>, List<Node<Integer>>> ignore = (e1, e2)-> {};

        helperDelegationTest(e-> e.helperGetIf(ignore),
                             e-> e.forEach(any()));
    }


//helperGetDescending

    @Test
    public void test_delegation_to_helperForEachDescendant(){
        Node<Integer> dummyValue1 = createNode();
        Function<Node<Integer>, Object> dummyValue2 = (e)-> new Object();
        helperDelegationTest(e-> e.helperGetDescending(dummyValue1, dummyValue2),
                             e-> e.helperForEachDescendant(any(), any()));
    }


//helperGetDescendingIf

    @Test
    public void test_delegates_to_helperForEachDescendant(){
        Node<Integer> dummyValue1 = createNode();
        Function<Node<Integer>, Object> dummyValue2 = (e)-> new Object();
        Predicate<Node<Integer>> dummyValue3 = (e)-> false;

        helperDelegationTest(e -> e.helperGetDescendingIf(dummyValue1, dummyValue2, dummyValue3),
                             e -> e.helperForEachDescendant(any(), any()));
    }

//helperForEachDescendant

    @Test
    public void test_consumer_is_applied(){
        AtomicInteger count = new AtomicInteger();
        Dummy<Integer> head = this.head;
        if(head.getChildNodes().size() < 1) fail("No descendants found to iterate");

        head.helperForEachDescendant(head, e-> count.incrementAndGet());
        assertTrue(count.get() > 0);
    }

    @Test
    public void test_delegates_to_helperIterateDescendants(){
        helperDelegationTest(e -> e.helperForEachDescendant(use, j -> {}), //no significance put in params
                             e -> e.helperIterateDescendants(any(), any()));
    }

    @Test
    public void test_can_start_from_head(){
        Node<Integer> target = createNode();
        head.getChildNodes().add(0,target);

        AtomicBoolean passed = new AtomicBoolean();
        use.helperForEachDescendant(head, e-> {
            if (e == target) {
                passed.set(true);
            }
            assertTrue(passed.get());
        });
    }

    @Test
    public void test_can_start_not_from_head(){
        //setup
        Dummy<Integer> use = createNode();
        Dummy<Integer> child = createNode();
        addChild(use,child,0);

        head.getChildNodes().add(0,use);

        //verify
        AtomicBoolean passed = new AtomicBoolean();
        head.helperForEachDescendant(use, e->{
            if(e == child){
                passed.set(true);
            }
            assertTrue(passed.get());
        });
    }

//helperIterateAll

    @Test
    public void test_starts_from_tree_head() {
        //must use atomic ref since Mockito.asserts return void and will not stop iteration when true
        AtomicReference<Node<Integer>> firstElement = new AtomicReference<>();

        use.helperIterateAll(e -> {
            firstElement.set(e);
            return STOP;
        });

        assertSame(firstElement.get(), head);
    }

    @Test
    public void test_stops_if_tree_head_matches_predicate() {
        AtomicInteger count = new AtomicInteger();

        use.helperIterateAll(e -> {
            count.incrementAndGet();
            return e == head;
        });
        assertEquals(1, count.get());
    }

    @Test
    public void test_delegates_to_helperIterateDescendats() {
        helperDelegationTest(e-> e.helperIterateAll(j-> false), //must be false or else will stop before delegating
                             e-> e.helperIterateDescendants(any(), any()));
    }


//helperIterateDescendants

    @Test
    public void test_starts_from_specified_parent() {
       AtomicReference<Node<Integer>> firstElement  = new AtomicReference<>();
        use.helperIterateDescendants(use, e -> {
                    firstElement.set(e);
                    return true;
                }
        );
        assertSame(firstElement.get(), use.getChildNodes().get(0));
    }

    @Test
    @SuppressWarnings("PointlessBooleanExpression") //true but not as clear
    public void test_traversal_of_all_descendants(){
        //setup
        Dummy<Integer> head = this.head;

        List<Entry<Dummy<Integer>, Boolean>> visited = tree.stream()
                                                                    .filter(e-> e != head)
                                                                    .map(e -> new SimpleEntry<>(e, false))
                                                                    .collect(Collectors.toList());
        //verify
        head.helperIterateDescendants(head, e-> {
            for(Entry<Dummy<Integer>, Boolean> entry: visited){
                if(e == entry.getKey()){
                    entry.setValue(true);
                }
            }
            return CONTINUE;
        });

        getIfThenAssertEmpty(visited, e-> e.getValue() == false);
    }

    @Test
    public void test_each_descendant_is_visited_only_once() {
        int size = tree.size() - 1;
        AtomicInteger count = new AtomicInteger();
        head.helperIterateDescendants(head, e -> {
            count.incrementAndGet();
            return CONTINUE;
        });
        assertEquals(size, count.get());
    }

    @Test
    public void test_each_descendant_has_predicate_applied() {
        AtomicInteger count = new AtomicInteger();
        head.helperIterateDescendants(head, e -> {
            count.incrementAndGet();
            return CONTINUE;
        });
        assertEquals(tree.size() -1, count.get());
    }

    @Test
    public void test_iteration_stops_when_predicate_is_matched() {
        AtomicInteger count = new AtomicInteger();
        head.helperIterateDescendants(head, e -> {
            count.incrementAndGet();
            return true;
        });
        assertEquals(1, count.get());
    }

    @Test
    public void test_traversal_is_in_expected_order(){
        AtomicInteger lastIndex = new AtomicInteger(head.getData());
        Predicate<Node<Integer>> checkOrder = e-> {
            Integer myIndex = e.getData();
            if(myIndex != 1 + lastIndex.get()) fail();
            lastIndex.set(myIndex);
            return CONTINUE;
        };

        use.helperIterateDescendants(head, checkOrder);
    }

    @Test
    public void test_traversal_checks_children_first() {
        Dummy<Integer> head = this.head;
        List<Node<Integer>> targets = head.getChildNodes();

        if (targets.size() == 0) {
            fail("No nodes found to search in " + targets);
        }

        AtomicReference<Node<Integer>> prevHead = new AtomicReference<>();
        AtomicReference<Integer> prevIndex = new AtomicReference<>();

        //idea is to tell iteration to stop if it doesn't check a child when expected
        Predicate<Node<Integer>> pred = e -> {
            Node<Integer> myParent = e.getParentNode();

            //init first node to visited in a given layer
            if (prevIndex.get() == null || prevHead.get() != myParent) {
                prevHead.set(myParent);
                prevIndex.set(0);
                return CONTINUE;
            }
            if (prevHead.get() == myParent) {
                int myIndex = 1 + prevIndex.get();
                prevIndex.set(myIndex);
                if(myIndex == e.getSiblingNodes().size() -1) return CONTINUE;
                if(e == e.getSiblingNodes().get(myIndex)) return CONTINUE;
            }
            return STOP;
        };

        boolean stoppedEarly = head.helperIterateDescendants(head, pred);
        assertFalse(stoppedEarly);
    }

    @Test
    public void test_traversal_secondly_moves_current_head_to_its_eldest_child(){
        AtomicReference<Node<Integer>> lastChecked = new AtomicReference<>();
        Dummy<Integer> head = this.head;
        List<Node<Integer>> children = head.getChildNodes();

        Node<Integer> expectedPrevious = children.get(children.size() - 1);
        Node<Integer> expected = children.get(0).getChildNodes().get(0);

        Predicate<Node<Integer>> pred = e -> {
            if (e == expected && lastChecked.get() == expectedPrevious) {
                return true;
            }
            lastChecked.set(e);
            return CONTINUE;
        };
        //verify
        boolean found = head.helperIterateDescendants(head, pred);
        assertTrue(found);
    }

    @Test
    public void test_traversal_pattern_1_and_2_continues_until_a_head_contains_no_children(){
        //setup/declare variables
        AtomicReference<Node<Integer>> lastChecked = new AtomicReference<>();
        Dummy<Integer> head = this.head;

        Node<Integer> expected = createNode().addChild(createNode());

        Consumer<Node<Integer>> tryInsertExpected = node -> { //inserts to make sure out of bounds won't occur
            List<Node<Integer>> siblings = node.getSiblingNodes();

            if (node == lastElement(siblings)) {
                Node<Integer> parent = node.getParentNode();
                siblings = parent.getSiblingNodes();

                for (int i = 0; i < siblings.size(); i++) {
                    if (parent == siblings.get(i)) {
                        siblings.add(i + 1, expected);
                    }
                }
            }
        };

        Predicate<Node<Integer>> isExpectedPrevious = node -> node.equals(lastElement(node.getSiblingNodes()));

        //verify
        boolean stopped = head.helperIterateDescendants(head, e -> {
            tryInsertExpected.accept(e);

            if(lastChecked.get() == null) {
                lastChecked.set(e);
                return CONTINUE;
            }
            if (isExpectedPrevious.test(lastChecked.get()) && e == expected) {
                lastChecked.set(e); //shouldn't be needed? but inconsequential
                return STOP;
            }
            lastChecked.set(e);
            return CONTINUE;
        });
        assertFalse(stopped);
    }

    @Test
    public void test_that_if_head_is_childless_then_head_moves_to_next_sibling() {
        AtomicReference<Node<Integer>> lastChecked = new AtomicReference<>();

        Predicate<Node<Integer>> isExpected = e -> {
            List<Node<Integer>> siblings = e.getSiblingNodes();
            Node<Integer> target = siblings.get(0);
            return e == target || target == null;
        };
        Predicate<Node<Integer>> isExpectedPrevious = e -> {
            if(e == null) return true; //I only expect this occurs at start, given there is no previous
            List<Node<Integer>> siblings = e.getSiblingNodes();
            return e == lastElement(siblings);
        };

        //verify
        List<Node<Integer>> failed = new ArrayList<>();
        head.helperIterateDescendants(head, e -> {
            if (isExpected.test(e)) {
                if(!isExpectedPrevious.test(lastChecked.get())){
                    failed.add(e);
                }
            }
            lastChecked.set(e);
            return CONTINUE;
        });

        assertEquals(0, failed.size());
    }

    @Test
    public void test_traversal_pattern_1_and_2_continue_after_moving_head_to_sibling(){
        AtomicReference<Node<Integer>> lastChecked = new AtomicReference<>();

        Predicate<Node<Integer>> precondition = e-> e.getChildNodes().size() == 0;

        Predicate<Node<Integer>> isExpected = e -> {
            List<Node<Integer>> siblings = e.getSiblingNodes();
            Node<Integer> target = siblings.get(0);
            return e == target || target == null;
        };
        Predicate<Node<Integer>> isExpectedPrevious = e -> {
            if(e == null) return true; //I only expect this occurs at start, given there is no previous
            List<Node<Integer>> siblings = e.getSiblingNodes();
            return e == lastElement(siblings);
        };

        //verify
        AtomicBoolean latch = new AtomicBoolean();
        List<Node<Integer>> failed = new ArrayList<>();
        head.helperIterateDescendants(head, e -> {
            latch.set(latch.get() || precondition.test(e));

            if (latch.get()) {
                if (isExpected.test(e)) {
                    if (!isExpectedPrevious.test(lastChecked.get())) {
                        failed.add(e);
                    }
                    return STOP;
                }
                lastChecked.set(e);
            }
            return CONTINUE;
        });

        assertEquals(0, failed.size());
    }

    @Test
    public void test_that_if_the_current_head_has_no_next_sibling_then_head_moves_to_parents_next_sibling_(){
        AtomicReference<Node<Integer>> lastChecked = new AtomicReference<>();

        Predicate<Node<Integer>> isExpected = e -> {
            if (lastChecked.get() == null) return CONTINUE;

            Node<Integer> myParent = e.getParentNode();
            if (myParent != null) {
                List<Node<Integer>> mySiblings = myParent.getChildNodes();


                int myIndex = ListUtil.getReferenceIndex(mySiblings, e);
                if (myIndex > 0) {
                    Node<Integer> lcParent = mySiblings.get(myIndex - 1);
                    List<Node<Integer>> lcChildren = lcParent.getChildNodes();
                    return lastChecked.get() == lastElement(lcChildren);
                }
            }
            return CONTINUE;
        };

        //verify
        AtomicBoolean passed = new AtomicBoolean();
        head.helperIterateDescendants(head, e -> {
            if (isExpected.test(e.getParentNode())) {
                passed.set(true);
                return STOP;
            }
            lastChecked.set(e);
            return CONTINUE;
        });
        assertTrue(passed.get());
    }

    @Test
    //Theres no current way to check where traversal goes if it leads to a bad result
    //instead this checks what should be the last node, and reaffirms that it is the last node checked
    public void test_traversal_ends_if_returns_to_starting_location(){
        AtomicReference<Node<Integer>> lastChecked = new AtomicReference<>();


        Supplier<Node<Integer>> getExpected = () -> {
            Node<Integer> head = this.head;
            List<Node<Integer>> children = head.getChildNodes();

            while (children != null && children.size() > 0) {
                Node<Integer> node = lastElement(children);
                if (node == null) break;
                head = node;
                children = head.getChildNodes();

            }
            return head;
        };

        Node<Integer> expected = getExpected.get();

        //verify
        boolean passed = head.helperIterateDescendants(head, e -> {
            lastChecked.set(e);
            return CONTINUE;
        });
        assertSame(lastChecked.get(), expected);
    }

//helpers

    private <T> T lastElement(List<T> list) {
        int index = list.size() - 1;
        return (index > -1) ? list.get(index)
                : null;
    }

    private <T> List<T> getIf(List<? extends T> list, Predicate<? super T> pred) {
        return ListUtil.getIf(pred, list);
    }

    private <T> void getIfThenAssertEmpty(List<? extends T> list, Predicate<? super T> pred) {
        List<? extends T> got = ListUtil.getIf(pred, list);
        assertEquals(0, got.size());
    }

    private void helperDelegationTest(Consumer<Dummy<Integer>> caller, Consumer<Dummy<Integer>> verify) {
        Dummy<Integer> mock = spy(createNode());
        caller.accept(mock);

        verify.accept(
        verify(mock, times(1)));
    }

//general ----

    @Test
    public void test_that_getSiblings_returns_its_parents_children(){
        List<Node<Integer>> parentChildren = use.getParentNode().getChildNodes();
        List<Node<Integer>> siblings = use.getSiblingNodes();
        assertSame(parentChildren, siblings);
    }

//inner class

    private static class Dummy<T> extends AbstractNode<T> {

        private T data;
        private Node<T> parent;
        private List<Node<T>> children;

        public Dummy(Node<T> parent, List<Node<T>> children) {
            this.parent = parent;
            this.children = children;
        }

        public Dummy(T data, Node<T> parent, List<Node<T>> children) {
            this.data = data;
            this.parent = parent;
            this.children = children;
        }

        public Dummy(T data) {
            this(data, null, new ArrayList<>());
        }

        public Dummy(T data, Node<T> parent) {
            this(data, parent, new ArrayList<>());
        }

        @Override
        protected Node<T> create(T data) {
            return new Dummy<>(data);
        }

        @Override
        protected Node<T> helperAddChild(Node<T> child, int index) {
            // if specified child keeps children nodes, they can still affect this class
            Node<T> copy = new Dummy<>(child.getData(), this, child.getChildNodes());

            children.add(index, copy);
            return copy;
        }

        @Override
        protected List<Node<T>> hGetChildNodes() {
            return children;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec (1) this method is used other methods of this class, and will affect them if overridden.
         * (2) The logic of the method depends assumptions of how this class works. Particularly,
         * when nodes are added, a copy of the node is added not the original. That copy
         * already has it's parent set to the correct value before being added. As such,
         * this method will only add children if the specified {@code node} has a {@code null} parent
         * @see Dummy#helperAddChild(Node, int)
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
            return children;
        }
    }


}