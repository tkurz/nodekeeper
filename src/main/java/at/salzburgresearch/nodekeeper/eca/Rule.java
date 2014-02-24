package at.salzburgresearch.nodekeeper.eca;

import at.salzburgresearch.nodekeeper.NodeListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
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

    public String getDescription() {
        StringBuilder b = new StringBuilder();
        b.append("<b>Rule '" + id + "' says</b></br>");
        b.append("If a node is ");
        switch(event.type) {
            case nodeCreated: b.append("created");break;
            case nodeUpdated: b.append("updated");break;
            case nodeDeleted: b.append("deleted");break;
            case nodeCreatedUpdated: b.append("created or updated");break;
        }
        b.append(" that follows the pattern <b>");
        b.append(event.pattern);
        b.append("</b>, then:<ul>");
        for(Action action : actions) {
            b.append("<li>");
            switch(action.type) {
                case createUpdateNode: b.append("<b>create or update</b>");break;
                case deleteNode: b.append("<b>delete</b>");break;
            }
            b.append(" node <b>");
            b.append(action.args[0]);
            b.append("</b>");
            if(action.args.length > 1) b.append(" with data <b>" + action.args[1]);
            b.append("</b></li>");
        }

        b.append("</ul>whereby the variables are defined as follows:<ul>");
        for(Binding binding : bindings) {
            b.append("<li>");
            b.append(binding.getDescription());
            b.append("</li>");
        }
        b.append("</ul>");
        return b.toString();
    }

}
