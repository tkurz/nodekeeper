package at.salzburgresearch.nodekeeper.bootstrap;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class ZKBootstrap {

    private Logger logger = LoggerFactory.getLogger(ZKBootstrap.class);

    private NodeKeeper nodeKeeper;

    public ZKBootstrap(NodeKeeper nodeKeeper) {
        this.nodeKeeper = nodeKeeper;
    }

    public void load(File file, boolean clean) throws IOException, NodeKeeperException, InterruptedException {
        load(new FileInputStream(file),clean);
    }

    public void load(InputStream is, boolean clean) throws IOException, NodeKeeperException, InterruptedException {

        if(clean) cleanNode(new Node("/"));

        Properties properties = new Properties();
        properties.load(is);

        for(Object key : properties.keySet()) {
            Node<String> node = new Node<String>((String)key,(String)properties.get(key));
            nodeKeeper.writeNode(node,String.class);
        }

    }

    public void write(OutputStream os, String start) throws NodeKeeperException, InterruptedException, IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
        stringifyChildren(out,start);
        out.flush();
        out.close();
    }

    public void write(OutputStream os) throws NodeKeeperException, InterruptedException, IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
        stringifyChildren(out,"/");
        out.flush();
        out.close();
    }

    private void stringifyChildren( BufferedWriter out, String path ) throws NodeKeeperException, InterruptedException, IOException {
        Set<Node<String>> nodes = nodeKeeper.listChildrenNodes(path,String.class);
        for(Node<String> node : nodes) {
            out.write(node.stringify());
            out.newLine();
            stringifyChildren(out, node.getPath());
        }
    }

    /**
     * remove nodes recursively
     * @param node
     * @throws NodeKeeperException
     * @throws InterruptedException
     */
    private void cleanNode(Node node) throws NodeKeeperException, InterruptedException {
        for(Node child : nodeKeeper.listChildrenNodes(node.getPath(),String.class)) {
            cleanNode(child);
        }
        try {
            nodeKeeper.deleteNode(node);
            logger.debug("Node {} deleted", node.getPath());
        } catch (Exception e) {
            logger.warn("Node {} could not be deleted", node.getPath());
            logger.debug(e.getMessage());
        }
    }
}
