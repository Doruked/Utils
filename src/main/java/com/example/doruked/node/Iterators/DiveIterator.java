package com.example.doruked.node.Iterators;

import com.example.doruked.ListUtil;
import com.example.doruked.node.Basic;
import com.example.doruked.node.mynodes.Node;

import java.util.NoSuchElementException;

/**
 * This iterator is defined by the order that it visits {@link Node}s.
 * The word "dive" is used, and can be thought of as "traversing downwards". In the context of a tree,
 * downwards from a given node will exclusively be {@code node.getChildNodes.get(0)}
 * <p>
 * The default behavior of this iterator is to continual "dive" and check the node that it travels toK.
 * When diving is unsuccessful, the iterator will seek leftward(later indices) nodes, backtracking upwards from parents if necessary.
 * Once an unvisited node is found, it is checked, then the iterator resumes diving. This process continues until an
 * unvisited node cannot be produced.
 * <p>
 * The resulting behavior is that any given node(from a specified starting location) will have all of it's descendants
 * checked before visiting it'a later siblings. In addition, this iterator will not visit a younger sibling.
 *
 * @param <TNode> the type of nodes traversed
 * @implSpec only implements {@link NodeIterator} for consistency atm. None of the methods
 * are implemented. In the future it is planned to be supported with proper implementations.
 */
public class DiveIterator<TNode extends Basic.CompatibleNode<?, TNode>> implements NodeIterator<TNode> {

    private TNode next;

    public DiveIterator(TNode next) {
        this.next = next;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        return next != null;
    }

    /** {@inheritDoc} */
    @Override
    public TNode next() {
        TNode current = next;
        TNode parent = current.getParentNode();
        TNode target;

        if (next != null) {
            //C0
            target = current.getChildNodes().get(0);
            if (target != null) return helperSetNext(target);

            //S+1
            target = helperNextSibling(current);
            if (target != null) return helperSetNext(target);

            //AU + 1
            target = helperNextSibling(parent);
            if (target != null) return helperSetNext(target);

            //P -> AU +1
            TNode grandparent = parent.getParentNode();
            while (parent.getParentNode() != null) {
                target = helperNextSibling(grandparent);
                if (target != null) return helperSetNext(target);

                parent = grandparent;
                grandparent = parent.getParentNode();
            }
            return helperSetNext(null);
        }
        throw new NoSuchElementException();
    }

    @Override
    public void clearData() {
        throw new UnsupportedOperationException("This method is not implemented yet");
    }

    private TNode helperNextSibling(TNode target) {
        int index = ListUtil.getReferenceIndex(target.getSiblingNodes(), target);
        return target.getSiblingNodes().get(index + 1);
    }

    private TNode helperSetNext(TNode target) {
        TNode ret = next;
        next = target;
        return ret;
    }
}
