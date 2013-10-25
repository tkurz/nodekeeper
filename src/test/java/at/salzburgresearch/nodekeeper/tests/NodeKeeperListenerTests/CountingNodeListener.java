package at.salzburgresearch.nodekeeper.tests.NodeKeeperListenerTests;

import at.salzburgresearch.nodekeeper.NodeListener;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class CountingNodeListener extends NodeListener<String> {

    @Override
    public void onNodeCreated(Node<String> node) throws InterruptedException, NodeKeeperException {
        NodeKeeperListenerTest.data[0] = NodeKeeperListenerTest.data[0] == null ? "1" : String.valueOf(Integer.parseInt(NodeKeeperListenerTest.data[0])+1);
    }

    @Override
    public void onNodeUpdated(Node<String> node) throws InterruptedException, NodeKeeperException {
        NodeKeeperListenerTest.data[1] = NodeKeeperListenerTest.data[1] == null ? "1" : String.valueOf(Integer.parseInt(NodeKeeperListenerTest.data[1])+1);
    }

    @Override
    public void onNodeDeleted(Node<String> node) throws InterruptedException, NodeKeeperException {
        NodeKeeperListenerTest.data[2] = NodeKeeperListenerTest.data[2] == null ? "1" : String.valueOf(Integer.parseInt(NodeKeeperListenerTest.data[2])+1);
    }

    @Override
    public Class getType() {
        return String.class;
    }
}
