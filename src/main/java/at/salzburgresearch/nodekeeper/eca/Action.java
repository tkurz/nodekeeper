package at.salzburgresearch.nodekeeper.eca;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.eca.function.StaticValueFunction;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Action {

    public enum Type {
        createUpdateNode,deleteNode
    }

    private Type type;
    private String[] args;

    public Action(Type type, String... args) {
        this.type = type;
        this.args = args;
    }

    public void execute(NodeKeeper nodeKeeper,HashMap<String,String> bindings) throws InterruptedException, NodeKeeperException, IOException {
        switch(type) {
            case createUpdateNode: createUpdateNode(nodeKeeper, bindings); break;
            case deleteNode: deleteNode(nodeKeeper, bindings);
        }
    }

    private void createUpdateNode(NodeKeeper nodeKeeper,HashMap<String,String> bindings) throws InterruptedException, IOException, NodeKeeperException {
        nodeKeeper.writeNode(prepareNode(nodeKeeper,bindings),String.class);
    }

    private void deleteNode(NodeKeeper nodeKeeper,HashMap<String,String> bindings) throws NodeKeeperException, InterruptedException {
        nodeKeeper.deleteNode(prepareNode(nodeKeeper, bindings));
    }

    private Node prepareNode(NodeKeeper nodeKeeper,HashMap<String,String> bindings) {
        String data = args.length > 1 ? args[1] : "";
        String label = args[0];
        for(String name : bindings.keySet()) {
            data = data.replaceAll("\\{"+name+"\\}",bindings.get(name));
            label = label.replaceAll("\\{"+name+"\\}", bindings.get(name));
        }
        Node<String> node = new Node<String>(label,data);
        return node;
    }

    public Element toElement(Document doc) {
        Element element = doc.createElement("action");
        element.setAttribute("type",type.name());
        for(String arg : args) {
            Element p = doc.createElement("param");
            p.setTextContent(arg);
            element.appendChild(p);
        }
        return element;
    }

}
