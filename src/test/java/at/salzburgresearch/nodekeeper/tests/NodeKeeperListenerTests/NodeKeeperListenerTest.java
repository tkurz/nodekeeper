package at.salzburgresearch.nodekeeper.tests.NodeKeeperListenerTests;

import at.salzburgresearch.nodekeeper.tests.NodeKeeperTest;
import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class NodeKeeperListenerTest extends NodeKeeperTest {

    public static String[] data;

    @Before
    public void init() {
        data = new String[10];
    }

    @Test
    public void testListeningToPathString() throws NodeKeeperException, IOException, InterruptedException {

        String path = "/my/test/node";
        nodeKeeper.addListener(path,new TestNodeListener(0));
        nodeKeeper.startListeners();

        nodeKeeper.writeNode(new Node(path,"one"),String.class);
        Thread.sleep(10);
        assertEquals(data[0], "one");

        nodeKeeper.writeNode(new Node(path,"two"),String.class);
        nodeKeeper.writeNode(new Node(path,"three"),String.class);
        Thread.sleep(10);
        assertEquals(data[0],"three");

        nodeKeeper.deleteNode(new Node(path, ""));
        nodeKeeper.writeNode(new Node(path,"one"),String.class);
        nodeKeeper.writeNode(new Node(path,"two"),String.class);
        Thread.sleep(10);
        assertEquals(data[0],"two");

        nodeKeeper.writeNode(new Node(path+"/other","other-node"),String.class);
        Thread.sleep(10);
        assertEquals(data[0],"two");
    }

    @Test
    public void testTwoListeners() throws NodeKeeperException, IOException, InterruptedException {

        String parent = "/parent";
        String child1 = "/parent/child1";
        String child2 = "/parent/child2";

        nodeKeeper.writeNode(new Node<String>(parent,"parent"),String.class);

        nodeKeeper.addListener(parent,new TestNodeListener(0));
        nodeKeeper.addListener(child1,new TestNodeListener(1));
        nodeKeeper.addListener(child1,new TestNodeListener(2));
        nodeKeeper.startListeners();

        Thread.sleep(10);
        assertEquals(data[0],"parent");
        assertEquals(data[1],null);
        assertEquals(data[2],null);

        nodeKeeper.writeNode(new Node<String>(child1,"child"),String.class);
        nodeKeeper.writeNode(new Node<String>(child2,"child2"),String.class);
        Thread.sleep(10);
        assertEquals(data[0],"parent");
        assertEquals(data[1],"child");
        assertEquals(data[2],"child");

    }

    @Test
    @Ignore //TODO how can it be handled?
    public void testConnectionLost() throws NodeKeeperException, IOException, InterruptedException {
        String path = "/my/test/node";
        nodeKeeper.addListener(path,new TestNodeListener(0));
        nodeKeeper.startListeners();

        nodeKeeper.writeNode(new Node(path,"one"),String.class);
        Thread.sleep(10);
        assertEquals(data[0], "one");

        standaloneServerFactory.shutdown();
        assertFalse(server.isRunning());
        Thread.sleep(10);
        standaloneServerFactory.startup(server);
        assertTrue(server.isRunning());
        Thread.sleep(10);

        try {
            nodeKeeper.writeNode(new Node(path,"two"),String.class);
            fail("should throw an exception");
        } catch(Exception e) {

        }
        Thread.sleep(10);
        nodeKeeper.writeNode(new Node(path,"two"),String.class);
        assertEquals(data[0], "two");

    }

    @Test
    public void testListeningToFullPatternString() throws NodeKeeperException, IOException, InterruptedException {
        String pattern = "/test/my/pattern/[^_]+_test";
        String path1 = "/test/my/pattern/one";
        String path2 = "/test/my/pattern/two_test";
        String path3 = "/test/my/pattern/test/three";

        nodeKeeper.addListener(pattern, new TestNodeListener(0));
        nodeKeeper.startListeners();

        nodeKeeper.writeNode(new Node(path1,"one"),String.class);
        Thread.sleep(10);
        assertEquals(data[0], null);

        nodeKeeper.writeNode(new Node(path2,"two"),String.class);
        Thread.sleep(10);
        assertEquals(data[0], "two");

        nodeKeeper.writeNode(new Node(path3,"two"),String.class);
        Thread.sleep(10);
        assertEquals(data[0], "two");
    }

    @Test
    public void testListeningToComplicatedFullPatternString() throws NodeKeeperException, IOException, InterruptedException {
        String pattern = "/test/my/pattern/[^/]+/test/[^/]";
        String path1 = "/test/my/pattern/one/test/1";
        String path2 = "/test/my/pattern/one/test/2";
        String path3 = "/test/my/pattern/two/test/1";
        String path4 = "/test/my/pattern/two/test/test/1";

        nodeKeeper.addListener(pattern, new TestNodeListener(0));
        nodeKeeper.startListeners();

        nodeKeeper.writeNode(new Node(path1,path1),String.class);
        Thread.sleep(50);
        assertEquals(data[0], path1);

        nodeKeeper.writeNode(new Node(path2,path2),String.class);
        Thread.sleep(50);
        assertEquals(data[0],path2);

        nodeKeeper.writeNode(new Node(path3, path3), String.class);
        Thread.sleep(50);
        assertEquals(data[0],path3);

        nodeKeeper.writeNode(new Node(path4,path4),String.class);
        Thread.sleep(10);
        assertEquals(data[0],path3);
    }

    @Test
    @Ignore //cannot be guaranteed !!!
    public void testCounter() throws NodeKeeperException, IOException, InterruptedException {
        String path = "/test";
        nodeKeeper.addListener(path, new CountingNodeListener());
        nodeKeeper.startListeners();

        nodeKeeper.writeNode(new Node(path,""),String.class);//0:1
        nodeKeeper.writeNode(new Node(path,""),String.class);//1:1
        nodeKeeper.writeNode(new Node(path,""),String.class);//1:2
        nodeKeeper.writeNode(new Node(path,""),String.class);//1:3
        nodeKeeper.deleteNode(new Node(path));            //2:1
        nodeKeeper.writeNode(new Node(path,""),String.class);//0:2
        nodeKeeper.writeNode(new Node(path,""),String.class);//1:4
        Thread.sleep(50);
        assertEquals(data[0],"2");
        assertEquals(data[1],"4");
        assertEquals(data[2],"1");

    }

    @Test
    public void testNotListenedEvent() throws NodeKeeperException, IOException, InterruptedException {
        String path = "/test";
        nodeKeeper.addListener("/test", new CountingNodeListener());
        nodeKeeper.startListeners();
        Thread.sleep(50);
        nodeKeeper.writeNode(new Node(path,"123"),String.class);
        Thread.sleep(50);
        nodeKeeper.deleteNode(new Node(path));
    }

    @Test
    public void testPrefix() throws InterruptedException, NodeKeeperException, IOException {
        NodeKeeper myNodeKeeper = new NodeKeeper(connectionString,TIMEOUT,properties,"/test");
        myNodeKeeper.addListener("/[^/]+/.+",new TestNodeListener(0));
        myNodeKeeper.startListeners();

        myNodeKeeper.writeNode(new Node("/wrong/path","test0"),String.class);
        Thread.sleep(10);
        assertEquals(data[0],null);

        myNodeKeeper.writeNode(new Node("/test/path","test1"),String.class);
        Thread.sleep(10);
        assertEquals(data[0],"test1");

        myNodeKeeper.writeNode(new Node("/test/path2","test2"),String.class);
        Thread.sleep(10);
        assertEquals(data[0],"test2");

        myNodeKeeper.writeNode(new Node("/test/path2/path3","test3"),String.class);
        Thread.sleep(10);
        assertEquals(data[0],"test3");

        myNodeKeeper.writeNode(new Node("/wrong/path2","test0"),String.class);
        Thread.sleep(10);
        assertEquals(data[0],"test3");

    }

}
