#NodeKeeper

NodeKeeper is a simple to use library on the top of ZooKeeper. It allows to create, read, update and delete zk-nodes,
supports custom datatypes and allows to listen and react on node events (create, update, delete) based on path patterns.

##The NodeKeeper Instance
The NodeKeeper instance must be initialized using the constructor:

    public NodeKeeper(String connectionString, int sessionTimeout, Properties properties, String startNode)

* **connectionString**: the ZooKeeper connection string
* **sessionTimeout**: the ZooKeeper timeout
* **properties**: NodeKeeper stores the current version of nodes as properties. This allows to connect and disconnect to a running zookeeper without any data loss.
* **startNode**: if NodeKeeper just watches on subnodes of startNode (null == "/")

As soon as NodeKeeper is instantiated, it is already connected to ZooKeeper. To shutdown the connection use

    public void shutdown()

##Nodes and DataHandlers
NodeKeeper nodes are implemented using Generics. In combination with specific DataHandlers, NodeKeeper allows to handle node
data in various formats. The nodes support only 3 fields with getters and setters:

    Node<T> {
        String path;
        T data;
        int version;
    }

To support T as DataFormat a specific DataHandler must be implemented using the DataHandler interface:

    public interface DataHandler<T> {
        public T parse(byte[] data) throws IOException;
        public byte[] serialize(T data) throws IOException;
        public Class<T> getType();
    }

The DataHandler<?> must be added to the NodeKeeper object using:

    public void addDataHandler(DataHandler dataHandler)

##Read and Write

NodeKeeper supports the following node operations:

    public <T> Node<T> readNode(String path, Class<T> clazz);
    public <T> void writeNode(Node<T> node, Class<T> clazz);
    public <T> void deleteNode(Node<T> node);
    public <T> Set<Node<T>> listChildrenNodes(String path, Class<T> clazz);

* **readNode** returns a Node<T> object; null if the node does not exist
* **writeNode** creates or updates the node; the path is created recursively
* **deleteNode** deletes the node
* **listNodes** returns all direct children of a path

##NodeListener

NodeKeeper allows to add listeners to pathPatterns, so you can handle CRUD events on that nodes. The listener must implement
the interface NodeListener:

    public abstract void onNodeCreated(Node<T> node) throws InterruptedException, NodeKeeperException;
    public abstract void onNodeUpdated(Node<T> node) throws InterruptedException, NodeKeeperException;
    public abstract void onNodeDeleted(Node<T> node) throws InterruptedException, NodeKeeperException;

    public abstract Class<T> getType();

To append a listener to NodeKeeper, you have to use:

    addListener(String pathPattern, NodeListener listener)

To enable all appended listeners, the process must be started using

    startListeners()

After that all nodes that matches at least one pattern are checked, if they changed regarding the properties. If so, the
listener methods are called immediately.

#A simple example

    Properties properties = new Properties();
    String basicPath = "/basic/path"

    //create instance
    NodeKeeper nodeKeeper = new NodeKeeper("127.0.0.1:8121",5000,basicPath);

    //add custom listener
    nodeKeeper.addListener(/basic/path/nodes/.+,new MyNodeListener());
    nodeKeeper.startListeners();

    String nodePath1 = basicPath+"/nodes/node1";
    String data = "data";

    //write string node
    nodeKeeper.writeNode(new Node<String>(nodePath1,data),String.class);

    //read string node
    Node<String> node = nodeKeeper.readNode(nodePath1,String.class);