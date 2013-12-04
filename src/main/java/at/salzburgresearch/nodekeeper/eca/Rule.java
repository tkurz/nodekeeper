package at.salzburgresearch.nodekeeper.eca;

import at.salzburgresearch.nodekeeper.NodeListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Rule {

    String id;
    public Event event;
    public List<Binding> bindings;
    public List<Condition> conditions;
    public List<Action> actions;

    public Rule() {
        id = UUID.randomUUID().toString();
        bindings = new ArrayList<Binding>();
        conditions = new ArrayList<Condition>();
        actions = new ArrayList<Action>();
    }

    public NodeListener getNodeListener() {
        return nodeListener;
    }

    public void setNodeListener(NodeListener nodeListener) {
        this.nodeListener = nodeListener;
        this.nodeListener.setId(this.id);
    }

    private NodeListener nodeListener;

    public Element toElement(Document doc) {
        Element rule = doc.createElement("rule");
        rule.setAttribute("name",id);

        rule.appendChild(event.toElement(doc));

        Element bs = doc.createElement("bindings");
        for(Binding binding : bindings) {
            bs.appendChild(binding.toElement(doc));
        }
        rule.appendChild(bs);

        Element cs = doc.createElement("conditions");
        for(Condition condition : conditions) {
            cs.appendChild(condition.toElement(doc));
        }
        rule.appendChild(cs);

        Element as = doc.createElement("actions");
        for(Action action : actions) {
            as.appendChild(action.toElement(doc));
        }
        rule.appendChild(as);

        return rule;
    }

}
