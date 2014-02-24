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
public abstract class Function {

    public Object[] params = new Object[0];

    public void init(Object... params) {
        this.params = params;
    }
    public abstract Object execute(NodeKeeper nodeKeeper, Node current);
    public abstract String getName();

    public abstract String getDescription();

    public Element toElement(Document doc) {
        Element element = doc.createElement("param");
        element.setAttribute("type",getName());
        for(Object param : params) {
            element.appendChild(((Function)param).toElement(doc));
        }
        return element;
    }
}
