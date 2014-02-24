package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class PathNode extends Function {
    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) {
        String[] path = current.getPath().split("/");
        int slot = Integer.parseInt(((String)((Function)params[0]).execute(nodeKeeper,current)));
        if(slot < path.length) return path[slot];
        return "";
    }

    @Override
    public String getName() {
        return "pathNode";
    }

    @Override
    public String getDescription() {
        return "the label of the node with path-length " + ((Function)params[0]).getDescription() + " of the current path";
    }
}
