package at.salzburgresearch.nodekeeper.eca;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.eca.function.Function;
import at.salzburgresearch.nodekeeper.eca.function.StaticValueFunction;
import at.salzburgresearch.nodekeeper.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Binding {

    String name;
    private Function function;

    public Binding(String name, Function function) {
        this.name = name;
        this.function = function;
    }

    public String execute(NodeKeeper nodeKeeper, Node current) {
        return (String)function.execute(nodeKeeper,current);
    }

    public Element toElement(Document doc) {
        Element element = doc.createElement("binding");
        element.setAttribute("name",name);
        if(function instanceof StaticValueFunction) element.setTextContent((String)function.execute(null,null));
        else {
            element.setAttribute("type",function.getName());
            for(Object func : function.params) {
                element.appendChild(((Function)func).toElement(doc));
            }
        }
        return element;
    }

}
