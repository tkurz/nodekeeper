package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class CurrentNodeLabel extends Function {

    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) {
        return current.getPath().substring(current.getPath().lastIndexOf("/")+1);
    }

    @Override
    public String getName() {
        return "currentNodeLabel";
    }
}
