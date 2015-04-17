package at.salzburgresearch.nodekeeper.eca;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.eca.exception.BindingException;
import at.salzburgresearch.nodekeeper.eca.function.Function;
import at.salzburgresearch.nodekeeper.eca.function.StaticValueFunction;
import at.salzburgresearch.nodekeeper.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Binding {

    public static String DEFAULT_BINDING = "_not_set_";

    String name;
    private Function function;

    //if strict is true, an action that references the binding is not executed
    private boolean strict;

    public Binding(String name, Function function, boolean strict) {
        this.name = name;
        this.function = function;
        this.strict = strict;
    }

    public Object execute(NodeKeeper nodeKeeper, Node current) {
        try {
            return function.execute(nodeKeeper,current);
        } catch (BindingException e) {
            e.setStrict(strict);
            return e;
        }
    }

    public Element toElement(Document doc) {
        Element element = doc.createElement("binding");
        element.setAttribute("name",name);
        element.setAttribute("strict", String.valueOf(strict));
        if(function instanceof StaticValueFunction) {
            try {
                element.setTextContent((String)function.execute(null,null));
            } catch (BindingException e) {
                //will never happen
                e.printStackTrace();
            }
        } else {
            element.setAttribute("type", function.getName());
            for(Object func : function.params) {
                element.appendChild(((Function)func).toElement(doc));
            }
        }
        return element;
    }

    public String getDescription() {
        return "<b>{"+name+"} "+(strict ? "(strict)" : "")+"</b> is " + function.getDescription();
    }

}
