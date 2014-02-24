package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class StaticValueFunction extends Function {
    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) {
        return params[0];
    }

    @Override
    public String getName() {
        return "staticValue";
    }

    @Override
    public String getDescription() {
        return "'"+(String)params[0]+"'";
    }

    public Element toElement(Document doc) {
        Element element = doc.createElement("param");
        element.setTextContent((String)params[0]);
        return element;
    }
}
