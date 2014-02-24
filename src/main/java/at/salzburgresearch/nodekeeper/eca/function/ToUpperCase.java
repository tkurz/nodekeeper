package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class ToUpperCase extends Function {

    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) {
        return ((String)((Function)params[0]).execute(nodeKeeper,current)).toUpperCase();
    }

    @Override
    public String getName() {
        return "toUpperCase";
    }

    @Override
    public String getDescription() {
        return "the UpperCase of "+((Function)params[0]).getDescription();
    }


}
