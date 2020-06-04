package com.example.doruked.node;

import com.example.doruked.ListUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains several nested interfaces that are components to building a {@code node} interface.
 *
 * Among these interfaces, there are a few common terms worth clarifying:
 * Siblings - nodes who have the same parents
 * Other Siblings - a node's siblings excluding itself
 * Descendants - any node stemming from a given parent. (children, grand children, great grand children etc)
 *
 * It's possible "Other Siblings" will be renamed to something better. It's use is fairly minimal,
 * so it's not quite that important. The other terms should stay consistent.
 */
public interface Basic {

    /**
     * This interface represents a node that is accessed (primarily) by {@link TNode} arguments. And provides info
     * about it self and it's tree in the form of {@link TNode}
     *
     * @param <TData> the data contained by the nodes
     * @param <TNode> the type of nodes to be used
     */
    interface TreeNode<TData, TNode extends TreeNode<TData, TNode>> extends  GetNodes<List<TNode>, TNode>{

    //modify

        /**
         * Replaces this object's parent with the specified {@code node}.
         *
         * Note: This operation does not attempt to make the current parent conform
         * to the specified {@code node}. However, the current parent 'should' be updated
         * to reflect it's loss of a child.}
         *
         * @param node the parent node to use
         * @throws UnsupportedOperationException if this node does not support the given modification
         */
        void setParentAs(TNode node);

        /**
         * Adds a copy of the specified {@code node} as a child of this object. The copied node loses it's previous
         * parent and optionally keeps other relations, as specified by the implementation. It is most reasonably
         * expected to to keep child relations.
         *
         * @param node the child node to add
         * @return the added node
         * @throws NullPointerException if target is null and null is not an accepted element
         * @throws UnsupportedOperationException if this node does not support the given modification
         */
        TNode addChild(TNode node);

        /**
         * Adds a copy of the specified {@code node} as a sibling of this object. The copied node loses it's previous
         * parent and optionally keeps other relations, as specified by the implementation. It is most reasonably
         * expected to to keep child relations.
         *
         * @param node the sibling node to add
         * @return the added node
         * @throws NullPointerException if node is null and null is not an accepted element
         * @implSpec this may be equivalent to {@code this.getParent.addChild(node)}
         * @throws UnsupportedOperationException if this node does not support the given modification
         */
        TNode addSibling(TNode node);

        /**
         * Sets this object's data to be equal to the data contained
         * in the specified {@code node}
         *
         * @param node the node to get data value from
         * @throws UnsupportedOperationException if this node does not support the given modification
         */
       default void setData(TNode node){setData(node.getData());}

        /**
         * Sets this object's data to be equal to the specified {@code data}
         *
         * @param data the data to set as
         * @throws UnsupportedOperationException if this node does not support the given modification
         */
        void setData(TData data);

        //this method is used in other default implementations

        /**
         * Removes this object's parent.
         *
         * @throws UnsupportedOperationException if this node does not support the given modification
         * @implSpec this may require updating the state of other nodes.
         * Further, this method also does not specify what the default
         * replacement value for a removed parent should be.
         */
        void removeParent();

        /**
         * Removes this object's child that matches the {@code target}
         *
         * @param target the node to remove
         * @return true if a child was removed, false if not
         * @throws NullPointerException if target is null and null is not an accepted element
         * @throws UnsupportedOperationException if this node does not support the given modification
         * @implSpec this may require updating the state of other nodes.
         * If multiple nodes may match the specified target, the first removable
         * node should be removed unless documented otherwise.
         */
        boolean removeChild(TNode target);

        /**
         * Removes the specified {@code target} as this object's sibling
         *
         * @param target the node to remove
         * @return true if removed, false if not
         * @throws NullPointerException if target is null and null is not an accepted element
         * @throws UnsupportedOperationException if this node does not support the given modification
         * @implSpec this may be equivalent to {@link #getParentNode()#removeChild(TreeNode)}
         */
        boolean removeSibling(TNode target);

        /**
         * Returns the data contained by this object
         *
         * @return the data contained by this object
         */
        TData getData();

    //getter

        /** {@inheritDoc}*/
        default List<TNode> getSiblingNodes() {
            TNode parent = getParentNode();
            if(parent == null) return Collections.emptyList();
            else return parent.getChildNodes();
        }

        /** {@inheritDoc}*/
        default List<TNode> getOtherSiblingNodes() {
            List<TNode> list = new ArrayList<>();

            TNode parent = getParentNode();
            for(TNode n: parent.getChildNodes()){
                if(!n.equals(this)){
                    list.add(n);
                }
            }
            return list;
        }

    //query

        /**
         * Returns whether this object is a parent to some node
         *
         * @return true this node is a parent to some node, false if not
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes.
         */
        default boolean isParent() {
            return !getChildNodes().isEmpty();
        }

        /**
         * Returns whether this object is a parent to some node
         *
         * @return true this node is a child to some node, false if not
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes.
         * <p>
         * May be equivalent to {@link #isHead()}
         */
        default boolean isChild() {
            return getParentNode() != null;
        }

        /**
         * Returns whether this object has descendants.
         *
         * @return true if this object has no descendants, false if not
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes.
         */
        default boolean isLeaf() {
            return getChildNodes().isEmpty();
        }

        /**
         * Returns whether this object is the head of it's tree.
         *
         * @return true if this object is the head of it's tree, false if not
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes.
         */
        default boolean isHead() {
            return getParentNode() == null;
        }

        /**
         * Returns whether the specified {@code parent} contains a child {@code node} that matches
         * the specified {@code target}.
         *
         * @param target the child to look for
         * @param parent the node to get children from
         * @return true if the child exists, false if not
         * @throws NullPointerException if target or parent is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         */
        default boolean containsChild(TNode target, TNode parent) { //kinda uselss. better to call parent.containsChild? I guess once benefit is that it uses to logic of the specified {@code node}
            return parent.getChildNodes().contains(target);
        }

        /**
         * Returns whether this object contains a child {@code target} that matches
         * the specified {@code data}.
         *
         * @param target the child to look for
         * @return true if the child exists, false if not
         * @throws NullPointerException if target is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         */
        default boolean containsChild(TNode target) {
            return getChildNodes().contains(target);
        }

        /**
         * Returns whether the specified {@code node} contains a sibling that matches
         * the specified {@code target}. A sibling node are those that shares it's parent.
         *
         * @param target the sibling to look for
         * @param node   the node to get siblings from
         * @return true if the sibling exists, false if not
         * @throws NullPointerException if target or node is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         */
        default boolean containsSibling(TNode target, TNode node) {
            return node.getOtherSiblingNodes().contains(target);
        }

        /**
         * Returns whether this object contains a sibling {@code target} that matches
         * the specified {@code target}. A sibling target are those that shares it's parent.
         *
         * @param target the sibling to look for
         * @return true if the siblingsibling exists, false if not
         * @throws NullPointerException if target is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         */
        default boolean containsSibling(TNode target) {
            return getOtherSiblingNodes().contains(target);
        }

        /**
         * Returns whether the specified {@code head} contains a descending {@code node} that matches
         * the specified {@code target}. A descending node are those beneath it's head.
         *
         * @param target the descendant to look for
         * @param head   the node to get descendants from
         * @return true if the descendant exists, false if not
         * @throws NullPointerException if target or head is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         * @implNote this method will potentially iterate through the entire tree twice.
         * Once to collect the descendants. Second to find the specified target. It may be
         * best to override this method if you wish to avoid this.
         */
        default boolean containsDescendant(TNode target, TNode head) {
            return head.getDescendingNodes().contains(target);
        }

        /**
         * Returns whether this object contains a descending {@code node} that matches
         * the specified {@code target}. A descending node are those beneath this object.
         *
         * @param target the descendant to look for
         * @return true if the descendant exists, false if not
         * @throws NullPointerException if node is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         * @implNote this method will potentially iterate through the entire tree twice.
         * Once to collect the descendants. Second to find the specified target. It may be
         * best to override this method if you wish to avoid this.
         */
        default boolean containsDescendant(TNode target) {
            return getDescendingNodes().contains(target);//rename target
        }

        /**
         * Returns whether the specified {@code node} has the same data as this object
         *
         * @return true if the node has the same data as this object, false if not
         * @implSpec must be consistent with calling {@code this.getData().equals(node.getData())}
         */
        default boolean sameData(TNode node) {
            return this.getData().equals(node.getData());
        }

        /**
         * Returns whether this object has the same parent as the specified {@code node}
         *
         * @param node the node to compare against
         * @return true if this object has the same parent as the specified node, false if not
         * @throws NullPointerException if node is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         */
        default boolean sameParent(TNode node) {
            return getParentNode().equals(node.getParentNode());
        }

        /**
         * Returns whether this object has the same tree head as the specified node
         *
         * @param node the node to compare against
         * @return true uf this object has the same tree head as the specified node, false of not
         * @throws NullPointerException if node is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         */
        default boolean sameTree(TNode node) {
            return getTreeHeadNode().equals(node.getTreeHeadNode());
        }

    }
    /**
     * This interface represents a node that is accessed (primarily) by {@link TData} arguments. And provides info
     * about it self and it's tree in the form of {@link TData}
     *
     * @param <TData> the data contained by the nodes
     * @param <TNode> the type of nodes to be used
     */
    interface DataNode<TData, TNode extends DataNode<TData, TNode>> extends GetData<List<TData>, TData> {

    //modify

        /**
         * Replaces this object's parent with a node containing specified {@code node}.
         *
         * Note: This operation does not attempt to make the current parent conform
         * to the specified {@code node}. However, the current parent 'should' be updated
         * to reflect it's loss of a child.
         *
         * @param data the parent data to use
         * @throws UnsupportedOperationException if this node does not support the given modification
         */
        void setParentAs(TData data);

        /**
         * Adds a copy of the specified {@code node} as a child of this object. The copied node loses it's previous
         * parent and optionally keeps other relations, as specified by the implementation. It is most reasonably
         * expected to to keep child relations.
         *
         * @param data the data of the child to add
         * @return the added node
         * @throws NullPointerException if data is null and null is not permitted
         */
        TNode addChild(TData data);

        /**
         * Adds a copy of the specified {@code node} as a sibling of this object. The copied node loses it's previous
         * parent and optionally keeps other relations, as specified by the implementation. It is most reasonably
         * expected to to keep child relations.
         *
         * @param data the data of the sibling to add
         * @return the added node
         * @throws NullPointerException if data is null and null is not permitted
         * @throws UnsupportedOperationException if this node does not support the given modification
         * @implSpec this may be equivalent to {@code this.getParent.addChild(data)}
         */
        TNode addSibling(TData data);

        /**
         * Sets this object's data to be equal to the data contained
         * in the specified {@code data}
         *
         * @param data the data to get data value from
         * @throws UnsupportedOperationException if this node does not support the given modification
         */
        void setData(TData data);

        /**
         * Removes this object's parent.
         *
         * @throws UnsupportedOperationException if this node does not support the given modification
         * @implSpec this may require updating the state of other nodes.
         * Further, this method also does not specify what the default
         * replacement value for a removed parent should be.
         */
        void removeParent();

        /**
         * Removes this object's child that matches the {@code target}
         *
         * @param target the data of the node to remove
         * @return true if a child was removed, false if not
         * @throws NullPointerException if target is null and null is not permitted
         * @throws UnsupportedOperationException if this node does not support the given modification
         * @implSpec this may require updating the state of other nodes.
         * If multiple nodes may match the specified target, the first removable
         * node should be removed unless documented otherwise.
         */
        TNode removeChild(TData target);

        /**
         * Removes the specified {@code target} as this object's sibling
         *
         * @param target the data of the node to remove
         * @return true if removed, false if not
         * @throws NullPointerException if target is null and null is not permitted
         * @throws UnsupportedOperationException if this node does not support the given modification
         */
        TNode removeSibling(TData target);

        /**
         * Returns the data contained by this object
         *
         * @return the data contained by this object
         */
        TData getData();

    //getter

        /** {@inheritDoc} */
        default List<TData> getOtherSiblingData() {
            List<TData> others = new ArrayList<>(getSiblingData());
            others.remove(this.getData());
            return others;
        }

    //query

        /**
         * Returns whether this object is a parent to some node
         *
         * @return true this node is a child to some node, false if not
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes.
         * <p>
         * May be equivalent to {@link #isHead()}
         */
        boolean isChild();

        /**
         * Returns whether this object has descendants.
         *
         * @return true if this object has no descendants, false if not
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes.
         */
        boolean isLeaf();

        /**
         * Returns whether this object is the head of it's tree.
         *
         * @return true if this object is the head of it's tree, false if not
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes.
         */
        boolean isHead();

        /**
         * Returns whether this object contains a child with {@code data} that matches
         * the specified {@code target}.
         *
         * @param target the data of the child to look for
         * @return true if the child exists, false if not
         * @throws NullPointerException if target is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         */
        default boolean containsChild(TData target) {
            return getChildData().contains(target);
        }

        /**
         * Returns whether this object contains a sibling with {@code data} that matches
         * the specified {@code target}.
         *
         * @param target the data of the sibling to look for
         * @return true if the sibling exists, false if not
         * @throws NullPointerException if target is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         */
        default boolean containsSibling(TData target) {
            return getOtherSiblingData().contains(target);
        }

        /**
         * Returns whether the specified {@code head} contains a descending {@code node} that matches
         * the specified {@code target}. A descending node are those beneath it's head.
         *
         * @param target the descendant to look for
         * @return true if the descendant exists, false if not
         * @throws NullPointerException if target is null
         * @implSpec by default, this method is not guaranteed to check the state of other nodes.
         * As such, results do not mean other nodes will not produce conflicting results. To avoid conflicts,
         * state must be properly managed across nodes
         * @implNote this method will potentially iterate through the entire tree twice.
         * Once to collect the descendants. Second to find the specified target. It may be
         * best to override this method if you wish to avoid this.
         */
        default boolean containsDescendant(TData target) {
            return getDescendingData().contains(target);
        }

        /**
         * Returns whether the specified {@code node} has the same data as this object
         *
         * @return true if the node has the same data as this object, false if not
         * @throws NullPointerException if node is null
         * @implSpec must be consistent with calling {@code this.getData().equals(node.getData())}
         */
        default boolean sameData(TData data) {
            return this.getData().equals(data);
        }

        /**
         * Returns whether this object has the same tree head as the specified node
         * @implNote potentially searches the entire tree twice. Once to get all nodes. Second
         * to check for the contained data.
         *
         * @param data the node to compare against
         * @return true uf this object has the same tree head as the specified node, false of not
         * @throws NullPointerException if node is null
         */
        default boolean sameTree(TData data) {
            return getAllData().contains(data);
        }

    }

    /**
     * This interface represents node that provides the behavior of a {@link DataNode} and a {@link TreeNode}
     * As such, users of this interface may access it's behavior by specifying either {@link TData} and {@link TNode}
     * arguments, or retrieve {@code nodes} values in either form.
     *
     * @param <TData> the data contained by the nodes
     * @param <TNode> the type of nodes to be used
     */
    interface CompatibleNode<TData, TNode extends CompatibleNode<TData, TNode>> extends TreeNode<TData,TNode>, DataNode<TData,TNode> {


        /** {@inheritDoc */
        default boolean isChild() {
            return getParentNode() != null;
        }

        /** {@inheritDoc */
        default boolean isLeaf() {
            return ListUtil.emptyOrNull(getChildNodes());
        }

        /** {@inheritDoc */
        default boolean isHead() {
            return getParentNode() == null;
        }

        /** {@inheritDoc */
        default boolean isParent() {
            return !getChildNodes().isEmpty();
        }

        /** {@inheritDoc */
        default TData getTreeHeadData(){
           return getTreeHeadNode().getData();
        }

        /** {@inheritDoc */
        default TData getParentData(){
            return getParentNode().getData();
        }
    }

    /**
     * This interface provides basic getters, designed for use with nodes
     *
     * @param <TCollection> a collection representing multiple {@link TNode}s
     * @param <TNode>       the type of nodes to get
     * @implSpec this interface expects that {@link TCollection} is some data structure
     * containing {@link TNode}s. However, to provide greater flexibility, this is not directly
     * enforced but still expected.
     */
    interface GetNodes<TCollection, TNode> {

        /** Returns this object's parent's node */
        TNode getParentNode();

        /**
         * Returns this object's tree head's node
         * @throws IllegalStateException if {@code this} contains itself as a parent
         */
        TNode getTreeHeadNode();

        /** Returns this object's collection of children node */
        TCollection getChildNodes();

        /** Returns this object's sibling nodes */
        TCollection getSiblingNodes();

        /** Returns this object's sibling nodes (self-excluded) */
        TCollection getOtherSiblingNodes();

        /** Returns the nodes that descend from this object (children, grand-children etc) */
        TCollection getDescendingNodes();

        /** Returns all nodes in this object's tree */
        TCollection getAllNodes();
    }

    /**
     * This interface provides basic getters, designed for use with nodes.
     * The provided getters retrieve data from nodes
     *
     * @param <TCollection>
     * @param <TData> the type of data to get
     * @implSpec this interface expects that {@link TCollection} is some data structure
     * containing {@link TData}s. However, to provide greater flexibility, this is not directly
     * enforced but still expected.
     */
    interface GetData<TCollection, TData>{

        /** Returns this object's parent's data */
        TData getParentData();

        /** Returns this object's tree head's data */
        TData getTreeHeadData();

        /** Returns the data from this object's collection of children */
        TCollection getChildData();

        /** Returns the data from this object's siblings */
        TCollection getSiblingData();

        /** Returns the data from this object's siblings (self-excluded) */
        TCollection getOtherSiblingData();

        /** Returns the data from the nodes that descend from this object (children, grand-children etc) */
        TCollection getDescendingData();

        /** Returns all data in this object's tree */
        TCollection getAllData();
    }

    /**
     * This interface provides basic getters, designed for use with nodes
     *
     * @param <TCollection> a collection representing multiple {@link TNode}s
     * @param <TNode>       the type of nodes to get
     * @implSpec this interface expects that {@link TCollection} is some data structure
     * containing {@link TNode}s. However, to provide greater flexibility, this is not directly
     * enforced but still expected.
     */
    interface Get<TCollection, TNode> {

        /** Returns this object's parent's {@link TNode} */
        TNode getParent();

        /** Returns this object's tree head's {@link TNode} */
        TNode getTreeHead();

        /** Returns this object's collection of child {@link TNode}s*/
        TCollection getChild();

        /** Returns a collection of this object's sibling {@link TNode}s */
        TCollection getSibling();

        /** Returns a collection of this object's sibling {@link TNode}s (self-excluded) */
        TCollection getOtherSibling();

        /** Returns the {@link TNode}s that descend from this object (children, grand-children etc) */
        TCollection getDescending();

        /** Returns all {@link TNode}s in this object's tree */
        TCollection getAll();
    }
}
