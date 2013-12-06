package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Parent extends Function {

    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) {
        return current.getPath().substring(0,current.getPath().lastIndexOf("/"));
    }

    @Override
    public String getName() {
        return "parentNode";
    }
}
