package at.redlink.nodekeeper;

import at.redlink.nodekeeper.exception.NodeKeeperException;
import at.redlink.nodekeeper.handlers.DataHandler;
import at.redlink.nodekeeper.handlers.impl.BooleanHandler;
import at.redlink.nodekeeper.handlers.impl.DictionaryDataHandler;
import at.redlink.nodekeeper.handlers.impl.IntegerHandler;
import at.redlink.nodekeeper.handlers.impl.StringDataHandler;
import at.redlink.nodekeeper.model.Node;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.Class;
import java.lang.Integer;
import java.lang.InterruptedException;
import java.lang.Override;
import java.lang.String;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * NodeKeeper is a ZooKeeper API wrapper, that makes it more comfortable to work with ZooKeeper nodes. The main methods are:
 * <ul>
 *     <li>handle reading and writing nodes with type specific (and expandable) data handlers</li>
 *     <li>handle node lifecycle easily (no existence checks etc.)</li>
 *     <li>appending NodeListeners to path patterns</li>
 * </ul>
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class NodeKeeper implements Watcher {

    private static Logger log = LoggerFactory.getLogger(NodeKeeper.class);
    private static HashMap<Class,DataHandler> handlers = new HashMap<Class, DataHandler>();
    private static final String PATH_SEPARATOR = "/";

    private ZooKeeper zk;
    private Properties properties;
    private String startNode;
    private String connectionString;
    private int sessionTimeout;

    private HashMap<String,List<NodeListener>> listeners = new HashMap<String,List<NodeListener>>();

    /**
     * NodeKeeper enables a ZooKeeper connection.
     * @param connectionString comma-separated list of url-strings of ZooKeeper servers
     * @param sessionTimeout sessionTimeout for connection
     * @param properties should be persisted at the end to guarantee clean node versioning (on re-startup)
     * @throws IOException
     * @throws InterruptedException
     */
    public NodeKeeper(String connectionString, int sessionTimeout, Properties properties, String startNode) throws InterruptedException, IOException, NodeKeeperException {
        this.properties = properties;
        this.startNode = startNode == null ? "/" : startNode;
        this.connectionString = connectionString;
        this.sessionTimeout = sessionTimeout;

        //add default handlers
        this.addDataHandler(new StringDataHandler());
        this.addDataHandler(new DictionaryDataHandler());
        this.addDataHandler(new IntegerHandler());
        this.addDataHandler(new BooleanHandler());

        final CountDownLatch connectedSignal = new CountDownLatch(1);
        zk = new ZooKeeper(connectionString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                switch(event.getState()) {
                    case SyncConnected:
                        connectedSignal.countDown();
                        break;
                }
            }
        });
        connectedSignal.await();

        log.info(" - nodekeeper initialized");
    }

    public void startListeners() throws InterruptedException, NodeKeeperException, IOException {
        try {
            deleteRemoved();
            appendWatcherToSubnodes(startNode);
        } catch (KeeperException e) {
            e.printStackTrace();
            throw new NodeKeeperException("cannot append listeners");
        }
    }

    private void appendWatcherToSubnodes(String path) throws KeeperException, InterruptedException, NodeKeeperException, IOException {
        Stat stat = zk.exists(path,this);
        if(stat != null) {

            Event.EventType version = getStatus(path,stat.getVersion());
            if(version != null) handleNode(path,stat,version);

            for(String child : zk.getChildren(path,this)) {
                appendWatcherToSubnodes((path.equals("/") ? "" : path) +"/"+child);
            }
        }
    }

    public void handleNode(String path, Stat stat, Event.EventType version) throws KeeperException, InterruptedException, NodeKeeperException, IOException {
        setStatus(path, stat.getVersion());
        for(String pattern : listeners.keySet()) {
            if(path.matches(pattern)) {
                for(NodeListener listener : listeners.get(pattern)) {
                    if(handlers.containsKey(listener.getType())) {
                        switch (version) {
                            case NodeCreated:
                                listener.onNodeCreated(new Node(path,handlers.get(listener.getType()).parse(zk.getData(path,this,stat))));
                                setStatus(path,stat.getVersion());
                                break;
                            case NodeDataChanged:
                                listener.onNodeUpdated(new Node(path,handlers.get(listener.getType()).parse(zk.getData(path,this,stat))));
                                setStatus(path,stat.getVersion());
                                break;
                            case NodeDeleted:
                                listener.onNodeDeleted(new Node(path));
                                removeStatus(path);
                                break;
                        }
                    } else throw new NodeKeeperException(String.format("cannot handle type %s",listener.getType()));
                }
            }
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                //a child has been added
                for(String child : zk.getChildren(watchedEvent.getPath(),this)) {
                    appendWatcherToSubnodes(watchedEvent.getPath().equals("/") ? "/" + child : watchedEvent.getPath() + "/" + child);
                }
            } else {
                Stat stat = zk.exists(watchedEvent.getPath(),this);

                if(stat != null && watchedEvent.getPath().equals(startNode)) appendWatcherToSubnodes(watchedEvent.getPath());
                else handleNode(watchedEvent.getPath(),stat, watchedEvent.getType());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (NodeKeeperException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns the ZooKeeper connection if it is connected
     * @return
     */
    public ZooKeeper getZooKeeper() {
        return zk;
    }

    /**
     * shut down ZooKeeper connection
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        if(zk != null) {
            zk.close();
            log.info(" - nodekeeper closed");
        }
    }

    /**
     * read node to ZooKeeper, return null if it does not exist
     * @param path path of the node
     * @param clazz class of the node, is handled by handler
     * @param <T> class of the node, is handled by handler
     * @return
     * @throws InterruptedException
     * @throws at.redlink.nodekeeper.exception.NodeKeeperException
     */
    public <T> Node<T> readNode(String path, Class<T> clazz) throws InterruptedException, NodeKeeperException, IOException {
        try {
            Stat stat;
            if((stat=zk.exists(path,false))!=null) {
                if(handlers.containsKey(clazz)) {
                    byte[] data = zk.getData(path,false,stat);
                    return new Node(path,handlers.get(clazz).parse(data),stat.getVersion());
                }
            }
        } catch (KeeperException e) {
            e.printStackTrace();
            throw new NodeKeeperException("cannot read node");
        }
        return null;
    }

    private void buildRecursively(String path, byte[] data) throws InterruptedException, NodeKeeperException {
        try {
            if(zk.exists(path,false)==null) {
                if(path.contains(PATH_SEPARATOR) && path.lastIndexOf(PATH_SEPARATOR)>0) {
                    buildRecursively(path.substring(0,path.lastIndexOf(PATH_SEPARATOR)),data);
                }
                zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
            throw new NodeKeeperException(String.format("error while creating node '#s'", path));
        }
    }

    /**
     * write node to ZooKeeper, create parent nodes recursively if they do not exist
     * @param node the node that should be written
     * @param clazz
     * @param <T> the handler for writing the data
     */
    public <T> void writeNode(Node<T> node, Class<T> clazz) throws InterruptedException, NodeKeeperException, IOException {
        try {
            Stat stat;
            //set if exists
            if((stat=zk.exists(node.getPath(),false))!=null) {
                if(handlers.containsKey(clazz)) {
                    //set this node
                    zk.setData(node.getPath(), handlers.get(clazz).serialize(node.getData()),stat.getVersion());
                } else {
                    throw new NodeKeeperException(String.format("cannot find handler for '%s'",node.getData().getClass().getName()));
                }
            } else {
                //create all node parent recursively if necessary
                if(node.getPath().contains(PATH_SEPARATOR) && node.getPath().lastIndexOf(PATH_SEPARATOR)>0) {
                    buildRecursively(node.getPath().substring(0, node.getPath().lastIndexOf(PATH_SEPARATOR)), String.format("created by %s", this.getClass().getName()).getBytes());
                }
                //serialize data
                if(handlers.containsKey(clazz)) {
                    //create this node
                    zk.create(node.getPath(), handlers.get(clazz).serialize(node.getData()), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                } else {
                    throw new NodeKeeperException(String.format("cannot find handler for '%s'",node.getData().getClass().getName()));
                }
            }
        } catch (KeeperException e) {
            e.printStackTrace();
            throw new NodeKeeperException(String.format("cannot write data for node '%s'", node.getPath()));
        }
    }

    /**
     * delete the node from ZooKeeper
     * @param node a node instance
     * @param <T> the class for the handler (not used in this method)
     * @throws InterruptedException
     * @throws NodeKeeperException
     */
    public <T> void deleteNode(Node<T> node) throws InterruptedException, NodeKeeperException {
        try {
            Stat stat;
            if((stat=zk.exists(node.getPath(),false))!=null) {
                zk.delete(node.getPath(),stat.getVersion());
            }
        } catch (KeeperException e) {
            e.printStackTrace();
            throw new NodeKeeperException(String.format("cannot delete node '%s'", node.getPath()));
        }
    }

    public <T> Set<Node<T>> listChildrenNodes(String path, Class<T> clazz) throws InterruptedException, NodeKeeperException {
        try {
            Set<Node<T>> nodes = new HashSet<Node<T>>();
            for(String key : zk.getChildren(path,false)) {
                Node<T> node = readNode(path.equals("/") ? path+key : path+PATH_SEPARATOR+key, clazz);
                if(node != null) nodes.add(node);
            }
            return nodes;
        } catch (KeeperException e) {
            e.printStackTrace();
            throw new NodeKeeperException(String.format("cannot read children for '%s'", path));
        } catch (IOException e) {
            e.printStackTrace();
            throw new NodeKeeperException(String.format("cannot read children for '%s'", path));
        }
    }

    /**
     * Appends a listener to the pathPattern. The methods of the lister are called when a node that matches the pathPattern
     * is created, updated or deleted.
     * @param pathPattern a regular expression that declares the node the listener is listen to.
     * @param listener a implementation of the NodeListener interface that handles the node events create, update and delete.
     * @throws NodeKeeperException
     */
    public void addListener(String pathPattern, NodeListener listener) throws NodeKeeperException {
        listener.nodekeeper = this;
        if(!listeners.containsKey(pathPattern)) {
            listeners.put(pathPattern,new ArrayList<NodeListener>());
        }
        listeners.get(pathPattern).add(listener);
    }

    private void deleteRemoved() throws KeeperException, InterruptedException, NodeKeeperException {
        for (String path : properties.stringPropertyNames()) {
            for(String pathPattern : listeners.keySet()) {
                if (!path.matches(pathPattern)) continue;
                if (zk.exists(path, false) == null) {
                    Node node = new Node(path);
                    for(NodeListener listener : listeners.get(pathPattern)) {
                        listener.onNodeDeleted(node);
                    }
                    removeStatus(path);
                }
            }
        }
    }

    /**
     * Adds a data handler that is used to read and write node data
     * @param dataHandler
     */
    public void addDataHandler(DataHandler dataHandler) {
        handlers.put(dataHandler.getType(),dataHandler);
    }

    private Event.EventType getStatus (String path, int version) throws KeeperException, InterruptedException {
        if(!properties.containsKey(path)) return Event.EventType.NodeCreated;
        //get version node
        int current_version = Integer.parseInt(properties.getProperty(path));
        if(current_version != version) return Event.EventType.NodeDataChanged;
        return null;
    }

    private void setStatus(String path, int version) throws KeeperException, InterruptedException {
        //set new Version
        properties.setProperty(path,String.valueOf(version));
    }

    private void removeStatus(String path) {
        properties.remove(path);
    }

}
