package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.eca.exception.BindingException;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Concat extends Function {
    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) throws BindingException {
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

    @Override
    public String getDescription() {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < params.length; i++) {
            b.append("<li>");
            if(i!=0) b.append("AND ");
            b.append(((Function)params[i]).getDescription());
            b.append("</li>");

        }
        return "the concatenation of<ul>" + b.toString() + "</ul>";
    }
}
