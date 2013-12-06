package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Concat extends Function {
    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) {
        StringBuilder b = new StringBuilder();
        for(Object param : params) {
            b.append(((Function)param).execute(nodeKeeper,current));
        }
        return b.toString();
    }

    @Override
    public String getName() {
        return "concat";
    }
}
