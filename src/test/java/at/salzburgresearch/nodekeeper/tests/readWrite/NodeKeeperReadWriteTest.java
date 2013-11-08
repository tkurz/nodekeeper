package at.salzburgresearch.nodekeeper.tests.readWrite;

import at.salzburgresearch.nodekeeper.tests.NodeKeeperTest;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import static junit.framework.Assert.*;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class NodeKeeperReadWriteTest extends NodeKeeperTest {

    @Test
    public void writeStringDataTest() throws NodeKeeperException, InterruptedException, IOException {
        String data = "My test data";
        String path = "/node";

        nodeKeeper.writeNode(new Node<String>(path,data),String.class);

        Node<String> node = nodeKeeper.readNode(path,String.class);

        assertEquals(node.getData(),data);
    }

    @Test
    public void writeIntegerDataTest() throws NodeKeeperException, InterruptedException, IOException {
        int data = 123;
        String path = "/node";

        nodeKeeper.writeNode(new Node<Integer>(path,data),Integer.class);

        Node<Integer> node = nodeKeeper.readNode(path,Integer.class);

        assertTrue(node.getData() == data);
    }

    @Test
    public void readChildrenNodesTest() throws NodeKeeperException, InterruptedException, IOException {

        int data1 = 1;
        String path1 = "/node1";
        int data2 = 2;
        String path2 = "/node2";

        nodeKeeper.writeNode(new Node<Integer>(path1,data1),Integer.class);
        nodeKeeper.writeNode(new Node<Integer>(path2,data2),Integer.class);

        HashMap<String,Integer> map = new HashMap<String,Integer>();

        for(Node<Integer> node : nodeKeeper.listChildrenNodes("/",Integer.class)) {
            map.put(node.getPath(),node.getData());
        }

        assertTrue(map.containsKey(path1));
        assertTrue(map.containsKey(path2));

        assertTrue(map.get(path1) == 1);
        assertTrue(map.get(path2) == 2);
    }

    @Test
    public void createRecursively() throws NodeKeeperException, InterruptedException, IOException {
        String path = "/my/sample/path/node";

        nodeKeeper.writeNode(new Node<String>(path,""),String.class);

        Node<String> node = nodeKeeper.readNode(path,String.class);

        assertTrue(node != null);
        assertEquals(node.getPath(),path);
        assertEquals(node.getData(),"");
    }

    @Test
    public void createRecursivelyWithExistent() throws NodeKeeperException, InterruptedException, IOException {
        String path1 = "/my/sample/path";
        String path2 = "/my/sample/path/with/more/ancestors/node";

        nodeKeeper.writeNode(new Node<String>(path1,""),String.class);
        nodeKeeper.writeNode(new Node<String>(path2,""),String.class);

        Node<String> node = nodeKeeper.readNode(path2,String.class);

        assertTrue(node != null);
        assertEquals(node.getPath(),path2);
        assertEquals(node.getData(),"");
    }

    @Test
    public void updateNode() throws NodeKeeperException, InterruptedException, IOException {
        String path = "/node";
        String data1 = "data1";
        String data2 = "data2";

        nodeKeeper.writeNode(new Node<String>(path,data1),String.class);

        Node<String> node1 = nodeKeeper.readNode(path,String.class);
        assertEquals(node1.getData(),data1);
        assertTrue(node1.getVersion() == 0);

        nodeKeeper.writeNode(new Node<String>(path,data2),String.class);

        Node<String> node2 = nodeKeeper.readNode(path,String.class);
        assertEquals(node2.getData(),data2);
        assertTrue(node2.getVersion() == 1);
    }

    @Test
    public void deleteNode() throws NodeKeeperException, InterruptedException, IOException {
        int data = 123;
        String path = "/node";

        nodeKeeper.writeNode(new Node<Integer>(path,data),Integer.class);
        Node<Integer> node1 = nodeKeeper.readNode(path,Integer.class);

        assertTrue(node1 != null);

        nodeKeeper.deleteNode(node1);

        Node<Integer> node2 = nodeKeeper.readNode(path,Integer.class);
        assertTrue(node2 == null);

    }

    @Test
    public void deleteEmpytNode() throws NodeKeeperException, InterruptedException {
        int data = 123;
        String path = "/node";
        nodeKeeper.deleteNode(new Node<Integer>(path,data));
    }

    @Test
    @Ignore
    public void wrongClassCast() throws NodeKeeperException, InterruptedException, IOException {
        String data = "Test";
        String path = "/node";

        nodeKeeper.writeNode(new Node<String>(path,data),String.class);

        try {
            Node<Boolean> node = nodeKeeper.readNode(path,Boolean.class);
            fail("String node should not be parsable to Boolean");
        } catch (IOException e) {
            //success
        }
    }

    @Test
    public void testNewDataHandler() throws InterruptedException, IOException, NodeKeeperException {

        nodeKeeper.addDataHandler(new DateDataHandler());

        Date date = new Date();
        String string = "a test";
        String path1 = "/datenode";
        String path2 = "/stringnode";

        nodeKeeper.writeNode(new Node<Date>(path1,date),Date.class);
        nodeKeeper.writeNode(new Node<String>(path2,string),String.class);

        Node<Date> node1 = nodeKeeper.readNode(path1,Date.class);

        assertTrue(node1.getData().compareTo(date) == 0);

        try {
            Node<Date> node2 = nodeKeeper.readNode(path2,Date.class);

            fail("non-date string must not be parsed to date");
        } catch (IOException e) {
            //success
        }

    }

}
