package at.salzburgresearch.nodekeeper.eca;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.eca.exception.ActionException;
import at.salzburgresearch.nodekeeper.eca.exception.BindingException;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashMap;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Action {

    private Logger logger = LoggerFactory.getLogger(Action.class);

    public enum Type {
        createUpdateNode,deleteNode
    }

    public Type type;
    public String[] args;

    public Action(Type type, String... args) {
        this.type = type;
        this.args = args;
    }

    public void execute(NodeKeeper nodeKeeper,HashMap<String,Object> bindings) throws InterruptedException, NodeKeeperException, IOException, ActionException {
        try {
            switch(type) {
                case createUpdateNode: createUpdateNode(nodeKeeper, bindings); break;
                case deleteNode: deleteNode(nodeKeeper, bindings);
            }
        } catch (BindingException e) {
            throw new ActionException(String.format("Action of type %s is not executed because: %s", type, e.getMessage()),e);
        }
    }

    private void createUpdateNode(NodeKeeper nodeKeeper,HashMap<String,Object> bindings) throws InterruptedException, IOException, NodeKeeperException, BindingException {
        nodeKeeper.writeNode(prepareNode(nodeKeeper,bindings),String.class);
    }

    private void deleteNode(NodeKeeper nodeKeeper,HashMap<String,Object> bindings) throws NodeKeeperException, InterruptedException, BindingException {
        nodeKeeper.deleteNode(prepareNode(nodeKeeper, bindings));
    }

    private Node prepareNode(NodeKeeper nodeKeeper,HashMap<String,Object> bindings) throws BindingException {
        String data = args.length > 1 ? args[1] : "";
        String label = args[0];
        for(String name : bindings.keySet()) {
            String value;
            if(bindings.get(name) instanceof BindingException) {
                BindingException e = (BindingException) bindings.get(name);
                if(e.isStrict()) {
                    throw e;
                } else {
                    value = Binding.DEFAULT_BINDING;
                }
            } else {
                value = (String)bindings.get(name);
            }
            data = data.replaceAll("\\{"+name+"\\}", value);
            label = label.replaceAll("\\{"+name+"\\}", value);
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
