package at.reddlink.nodekeeper.tests;

import at.redlink.nodekeeper.NodeKeeper;
import at.redlink.nodekeeper.exception.NodeKeeperException;
import at.redlink.nodekeeper.model.Node;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public abstract class NodeKeeperTest {

    private static final String CONNECTION_STRING = "127.0.0.1";
    public static final int TIMEOUT = 60000;

    public ZooKeeperServer server;
    public ServerCnxnFactory standaloneServerFactory;

    public NodeKeeper nodeKeeper;
    public Properties properties;
    public String connectionString;

    public File file;

    private static final String fileDirectory = System.getProperty("java.io.tmpdir")+"zookeeper";

    @Before
    public void startNodekeeper() throws IOException, InterruptedException, NodeKeeperException {
        //start zookeeper
        ServerConfig config = new ServerConfig();
        //delete tmp file
        file = new File(fileDirectory);
        if(file.isDirectory()) FileUtils.deleteDirectory(file);
        //create tmp file
        new File(fileDirectory).mkdir();
        int port = startZookeeper();
        connectionString = CONNECTION_STRING+":"+port;

        System.out.println(String.format("run zookeeper on %s, data directory is %s",connectionString,fileDirectory));

        //start nodekeeper
        properties = new Properties();

        nodeKeeper = new NodeKeeper(connectionString,TIMEOUT,properties,null);

        System.out.println(String.format("run nodekeeper with %s timeout",TIMEOUT));

    }

    private int startZookeeper() throws IOException, InterruptedException {
        File dir = new File(fileDirectory);
        int tickTime = 2000;
        int numConnections = 5000;

        server = new ZooKeeperServer(dir, dir, tickTime);
        standaloneServerFactory = ServerCnxnFactory.createFactory(0, numConnections);
        int zkPort = standaloneServerFactory.getLocalPort();

        standaloneServerFactory.startup(server);
        return zkPort;
    }

    @After
    public void stopNodekeeper() throws InterruptedException, IOException {
        //shutdown nodekeeper
        nodeKeeper.shutdown();
        //shutdown zookeeper
        server.shutdown();

        System.out.println("Zookeeper and Nodekeeper are down");

        //delete tmp file
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir")+"zookeeper"));

        System.out.println("data directory is deleted");
    }

}
