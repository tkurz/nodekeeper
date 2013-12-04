package at.salzburgresearch.nodekeeper.eca;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Condition {

    String expressions;

    public boolean execute(HashMap<String,String> bindings) {
        //TODO
        return true;
    }

    public Element toElement(Document doc) {
        Element element = doc.createElement("constaint");
        //TODO
        return element;
    }
}
