package at.salzburgresearch.nodekeeper.tests.readWrite;

import at.salzburgresearch.nodekeeper.bootstrap.ZKBootstrap;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;
import at.salzburgresearch.nodekeeper.tests.NodeKeeperTest;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class BootstrapTest extends NodeKeeperTest {

    @Test
    public void load() throws NodeKeeperException, InterruptedException, IOException, URISyntaxException {

        File file = new File(this.getClass().getResource("bootstrap.properties").toURI());
        ZKBootstrap bootstrap = new ZKBootstrap(nodeKeeper);
        bootstrap.load(file,false);

        Thread.sleep(2000);

        Node<String> node = nodeKeeper.readNode("/my/node",String.class);
        Node<String> node2 = nodeKeeper.readNode("/my/node2",String.class);
        Node<String> node3 = nodeKeeper.readNode("/other/node",String.class);
        Node<String> node4 = nodeKeeper.readNode("/stanbol/test",String.class);

        //TODO test

        File file2 = new File("/tmp/1.txt");
        FileOutputStream out = new FileOutputStream(file2);
        bootstrap.write(out);

        File file3 = new File("/tmp/2.txt");
        FileOutputStream out2 = new FileOutputStream(file3);
        bootstrap.write(out2,"/my");
        out2.flush();
        out2.close();
    }

}
