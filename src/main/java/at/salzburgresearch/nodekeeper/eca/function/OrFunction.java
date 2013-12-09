package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class OrFunction extends Function {

    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) {
        for(Object param : params) {
            Object value = ((Function)param).execute(nodeKeeper,current);
            if(value != null) return value;
        }
        return null;
    }

    @Override
    public String getName() {
        return "or";
    }
}
