package at.salzburgresearch.nodekeeper;


import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;

import java.lang.Class;import java.lang.InterruptedException; /**
 * The interface must be used by handlers that are appended to NodeKeeper. The generic type for the listener must be
 * handled by a DataHandler
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public abstract class NodeListener<T> {

    protected NodeKeeper nodekeeper = null;

    public abstract void onNodeCreated(Node<T> node) throws InterruptedException, NodeKeeperException;
    public abstract void onNodeUpdated(Node<T> node) throws InterruptedException, NodeKeeperException;
    public abstract void onNodeDeleted(Node<T> node) throws InterruptedException, NodeKeeperException;

    public abstract Class<T> getType();
}
