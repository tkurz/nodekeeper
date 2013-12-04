package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;

import java.io.IOException;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class NodeData extends Function {
    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) {
        try {
            Class clazz = params.length == 2 ? Class.forName((String)((Function)params[0]).execute(nodeKeeper,current)) : String.class;
            Node node = nodeKeeper.readNode((String)((Function)params[0]).execute(nodeKeeper,current),clazz);
            return node.getData();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NodeKeeperException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";
    }

    @Override
    public String getName() {
        return "nodeData";
    }
}
