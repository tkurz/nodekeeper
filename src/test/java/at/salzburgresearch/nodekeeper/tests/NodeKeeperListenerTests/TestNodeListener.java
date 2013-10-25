package at.salzburgresearch.nodekeeper.tests.NodeKeeperListenerTests;

import at.salzburgresearch.nodekeeper.NodeListener;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TestNodeListener extends NodeListener<String> {

    private int slot;

    public TestNodeListener(int slot) {
        this.slot = slot;
    }

    @Override
    public void onNodeCreated(Node<String> node) throws InterruptedException, NodeKeeperException {
        NodeKeeperListenerTest.data[slot] = node.getData();
    }

    @Override
    public void onNodeUpdated(Node<String> node) throws InterruptedException, NodeKeeperException {
        NodeKeeperListenerTest.data[slot] = node.getData();
    }

    @Override
    public void onNodeDeleted(Node<String> node) throws InterruptedException, NodeKeeperException {
        NodeKeeperListenerTest.data[slot] = null;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}
