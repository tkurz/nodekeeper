package at.salzburgresearch.nodekeeper.eca;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Event {

    public enum Type {
        nodeCreated, nodeCreatedUpdated, nodeUpdated, nodeDeleted
    }

    public Class nodeType = String.class;
    public Type type;
    public String pattern;

    public Event(Type type, String pattern) {
        this.type = type;
        this.pattern = pattern;
    }

    public Element toElement(Document doc) {
        Element element = doc.createElement("event");
        element.setAttribute("type",type.name());
        Element p = doc.createElement("param");
        p.setTextContent(pattern);
        element.appendChild(p);
        return element;
    }
}
