#NodeKeeper

NodeKeeper is a simple to use library on the top of ZooKeeper. It allows to create, read, update and delete zk-nodes,
supports custom datatypes and allows to listen and react on node events (create, update, delete) based on path patterns.

**NEW RELEASE:** NodeKeeper 1.2 is now available in maven central. [Look here](http://mvnrepository.com/artifact/at.salzburgresearch.nodekeeper/nodekeeper-java/1.2) how you can use it via dependency (maven, gradle, etc).

**NEW FEATURE:** NodeKeeper now has a better re-connection strategy, supports recursive node deletion and has an improved boostraping module.

##The NodeKeeper Instance
The NodeKeeper instance must be initialized using the constructor:

    public NodeKeeper(String connectionString, int sessionTimeout, Properties properties, String startNode)

* **connectionString**: the ZooKeeper connection string
* **sessionTimeout**: the ZooKeeper timeout
* **properties**: NodeKeeper stores the current version of nodes as properties. This allows to connect and disconnect to a running zookeeper without any data loss.
* **startNode**: if NodeKeeper just watches on subnodes of startNode (null == "/")

As soon as NodeKeeper is instantiated, it is already connected to ZooKeeper. To shutdown the connection use

```java
public void shutdown()
```

##Nodes and DataHandlers
NodeKeeper nodes are implemented using Generics. In combination with specific DataHandlers, NodeKeeper allows to handle node
data in various formats. The nodes support only 3 fields with getters and setters:

```java
Node<T> {
    String path;
    T data;
    int version;
}
```

To support T as DataFormat a specific DataHandler must be implemented using the DataHandler interface:

```java
public interface DataHandler<T> {
    public T parse(byte[] data) throws IOException;
    public byte[] serialize(T data) throws IOException;
    public Class<T> getType();
}
```

The DataHandler<?> must be added to the NodeKeeper object using:

```java
public void addDataHandler(DataHandler dataHandler)
```

##Read and Write

NodeKeeper supports the following node operations:

```java
public <T> Node<T> readNode(String path, Class<T> clazz);
public <T> void writeNode(Node<T> node, Class<T> clazz);
public <T> void deleteNode(Node<T> node);
public <T> Set<Node<T>> listChildrenNodes(String path, Class<T> clazz);
```

* **readNode** returns a Node<T> object; null if the node does not exist
* **writeNode** creates or updates the node; the path is created recursively
* **deleteNode** deletes the node
* **listNodes** returns all direct children of a path

##NodeListener

NodeKeeper allows to add listeners to pathPatterns, so you can handle CRUD events on that nodes. The listener must extend
the abstract class NodeListener:

```java
public abstract void onNodeCreated(Node<T> node) throws InterruptedException, NodeKeeperException;
public abstract void onNodeUpdated(Node<T> node) throws InterruptedException, NodeKeeperException;
public abstract void onNodeDeleted(Node<T> node) throws InterruptedException, NodeKeeperException;

public abstract Class<T> getType();
```

To append a listener to NodeKeeper, you have to use:

```java
addListener(String pathPattern, NodeListener listener);
```

To enable all appended listeners, the process must be started using

```java
startListeners();
```

After that all nodes that matches at least one pattern are checked, if they changed regarding the properties. If so, the
listener methods are called immediately.

#A simple example

```java
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
```

# Event-Binding-Action rules

NodeKeeper supports Event-Binding-Action rules since version 1.1. This means:

 - *Event:* A node is created, updated or deleted. The node itself can be specified via a regex pattern
 - *Binding:* Bind values like node paths, node labels, node data, etc. to variable names. A lot of useful functions supports you in this step.
 - *Action:* Create, update or delete nodes by using and combining variables that are defined on step 2

NodeKeeper of course supports several rules in parallel.

## The rule syntax

Here you can see a sample rule that is described inline with comments

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rules>

    <!-- A single rule that can be identified with its unique name -->
    <rule name="test_rule">

        <!-- if a node with the given pattern is created -->
        <event type="nodeCreated">
            <param>/my/event/.+</param>
        </event>

        <!-- bind this variables -->
        <bindings>

            <!-- 'data' is the data of the current node -->
            <binding name="data" type="currentNodeData"/>

            <!-- 'label' is the label of the current node in upper case -->
            <binding name="label" type="toUpperCase">
                <param type="currentNodeLabel"/>
            </binding>

        </bindings>

        <!-- and do this actions -->
        <actions>

            <!-- create or update a node with the given path and data. The data in '{}' is replaced with the bound variables -->
            <action type="createUpdateNode">
                <param>/my/action/{label}</param>
                <param>Hello {data}</param>
            </action>

        </actions>
    </rule>
</rules>
```

## Binding functions

All supported binding functions are at the moment in the package *at.salzburgresearch.nodekeeper.eca.function*. In the next version
the functions should be bound by ClassLoader, so that they are easily extendable.

## Example

This is a simple code example how you can load rules. Of course rules can also be created pragmatically. For more information
have a look at the [Testcases.](src/test/java/at/salzburgresearch/nodekeeper/tests/ruleEngineTests/SimpleRuleTests.java)

```java
InputStream in = new FileInputStream("rules.xml");

RuleHandler handler = new RuleHandler(nodeKeeper);
handler.readRules(in);

nodeKeeper.writeNode(new Node<String>("/my/event/node","World"),String.class);

//with the rule described above, a new node '/my/action/NODE' with value 'Hello World' is created

```

#Contact
Thomas Kurz, Salzburg Research Forschungsgesellschaft, Salzburg, Austria

<thomas.kurz@salzburgresearch.at>

#License
Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html)