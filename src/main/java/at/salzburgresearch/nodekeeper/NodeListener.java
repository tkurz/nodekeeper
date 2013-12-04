package at.salzburgresearch.nodekeeper;


import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;

import java.lang.Class;import java.lang.InterruptedException;
import java.util.UUID;

/**
 * The interface must be used by handlers that are appended to NodeKeeper. The generic type for the listener must be
 * handled by a DataHandler
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public abstract class NodeListener<T> implements Comparable<T> {

    protected NodeKeeper nodekeeper = null;
    private String id = UUID.randomUUID().toString();

    public abstract void onNodeCreated(Node<T> node) throws InterruptedException, NodeKeeperException;
    public abstract void onNodeUpdated(Node<T> node) throws InterruptedException, NodeKeeperException;
    public abstract void onNodeDeleted(Node<T> node) throws InterruptedException, NodeKeeperException;

    public abstract Class<T> getType();

    public int compareTo(Object o) {
        return id.compareTo(((NodeListener)o).id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
