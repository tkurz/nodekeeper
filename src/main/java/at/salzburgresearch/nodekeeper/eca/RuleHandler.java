package at.salzburgresearch.nodekeeper.eca;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.NodeListener;
import at.salzburgresearch.nodekeeper.eca.function.Function;
import at.salzburgresearch.nodekeeper.eca.function.FunctionFactory;
import at.salzburgresearch.nodekeeper.eca.function.StaticValueFunction;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class RuleHandler {

    HashMap<String,Rule> rules = new HashMap<String, Rule>();
    NodeKeeper nodeKeeper;

    public RuleHandler(NodeKeeper nodeKeeper) {
        this.nodeKeeper = nodeKeeper;
    }

    public void readRules(InputStream stream) throws NodeKeeperException, IOException, InterruptedException {
        //remove old rules
        List<Rule> rs = InputOutputHandler.parseRules(stream);
        ArrayList<String> keys = new ArrayList<String>(rules.keySet());
        for(String ruleid : keys) {
            removeRule(rules.get(ruleid));
        }
        for(Rule rule : rs) {
            addRule(rule);
        }
    }

    public void writeRules(OutputStream stream) throws ParserConfigurationException, TransformerException, IOException {
        List<Rule> r = new ArrayList<Rule>();
        for(String ruleid : rules.keySet()) {
            r.add(rules.get(ruleid));
        }
        InputOutputHandler.serializeRules(r,stream);
    }

    public void addRule(Rule rule) throws NodeKeeperException, IOException, InterruptedException {
        deactivateRule(rule);
        activateRule(rule);
        rules.put(rule.id,rule);
    }

    public Rule getRule(String id) {
        return rules.get(id);
    }

    public Set<String> getRuleIds() {
        return rules.keySet();
    }

    public void removeRule(Rule rule) {
        deactivateRule(rule);
        rules.remove(rule.id);
    }

    private void activateRule(final Rule rule) throws NodeKeeperException, IOException, InterruptedException {

        //handle event
        rule.setNodeListener(new NodeListener() {

            private void execute(Node node) throws InterruptedException, IOException, NodeKeeperException {
                HashMap<String,String> bindings = bindVariables(node);
                if(checkConditions(bindings)) {
                    for(Action action : rule.actions) {
                        action.execute(nodekeeper,bindings);
                    }
                }
            }

            private HashMap<String,String> bindVariables(Node node) {
                HashMap<String,String> bindings = new HashMap<String, String>();
                for(Binding binding : rule.bindings) {
                    bindings.put(binding.name,binding.execute(nodekeeper,node));
                }
                return bindings;
            }

            private boolean checkConditions(HashMap<String,String> bindings) {
                boolean result = true;
                for(Condition condition : rule.conditions) {
                    result = condition.execute(bindings);
                }
                return result;
            }

            @Override
            public void onNodeCreated(Node node) throws InterruptedException, NodeKeeperException {
                if(rule.event.type == Event.Type.nodeCreated || rule.event.type == Event.Type.nodeCreatedUpdated) try {
                    execute(node);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            @Override
            public void onNodeUpdated(Node node) throws InterruptedException, NodeKeeperException {
                if(rule.event.type == Event.Type.nodeUpdated || rule.event.type == Event.Type.nodeCreatedUpdated) try {
                    execute(node);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            @Override
            public void onNodeDeleted(Node node) throws InterruptedException, NodeKeeperException {
                if(rule.event.type == Event.Type.nodeDeleted) try {
                    execute(node);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            @Override
            public Class getType() {
                return rule.event.nodeType;
            }
        });

        nodeKeeper.addListener(rule.event.pattern, rule.getNodeListener());
        nodeKeeper.startListeners();
    }

    private void deactivateRule(Rule rule) {
        if(rules.containsKey(rule.id)) {
            Rule r = rules.get(rule.id);
            nodeKeeper.removeListener(r.event.pattern, r.getNodeListener());
        }
        //reverting actions is not possible at the moment
    }

    public static class InputOutputHandler {

        public static List<Rule> parseRules(InputStream inputStream) throws IOException {
            try {
                List<Rule> rules = new ArrayList<Rule>();

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(inputStream);

                //get rules
                NodeList rulesNodes = doc.getDocumentElement().getChildNodes();

                for(int i = 0; i < rulesNodes.getLength(); i++) {
                    org.w3c.dom.Node ruleNode = rulesNodes.item(i);

                    if (ruleNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        Element ruleElement = (Element)ruleNode;

                        Rule rule = new Rule();
                        if(ruleElement.getAttribute("name") != null) rule.id = ruleElement.getAttribute("name");

                        //get event
                        NodeList events = ruleElement.getElementsByTagName("event");
                        NodeList bindings = ruleElement.getElementsByTagName("bindings");
                        NodeList conditions = ruleElement.getElementsByTagName("conditions");//TODO
                        NodeList actions = ruleElement.getElementsByTagName("actions");

                        for(int j = 0; j < events.getLength(); j++) {
                            org.w3c.dom.Node eventNode = events.item(j);
                            if (eventNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                Element eventElement = (Element)eventNode;
                                String typeString  = eventElement.getAttribute("type");

                                if(typeString == null) throw new IOException(String.format("Type of event #$s in rule #%s must be set", j, i));
                                if(eventElement.getElementsByTagName("params").getLength()<0) throw new IOException(String.format("Type of event #$s in rule #%s must have a param", j, i));

                                Event.Type type = Event.Type.valueOf(typeString);
                                if(type == null) throw new IOException(String.format("Type %s of event #$s in rule #%s is not supported", typeString, j, i));

                                rule.event = new Event(type,eventElement.getElementsByTagName("param").item(0).getTextContent().trim());
                            }
                        }
                        if(rule.event == null) throw new IOException(String.format("Rule #%s must contain an event element",i));

                        if(bindings.getLength()>0) {
                            NodeList bindingList = bindings.item(0).getChildNodes();
                            for(int j = 0; j < bindingList.getLength(); j++) {
                                org.w3c.dom.Node bindingNode = bindingList.item(j);
                                if (bindingNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                    Element bindingElement = (Element)bindingNode;

                                    String name  = bindingElement.getAttribute("name");
                                    String type  = bindingElement.getAttribute("type");

                                    if(type==null || type.equals("")) {
                                        Function f = new StaticValueFunction();
                                        f.init(bindingElement.getTextContent().trim());
                                        rule.bindings.add(new Binding(name,f));
                                    } else {
                                        Function f = createFunction(type,bindingElement.getChildNodes());
                                        rule.bindings.add(new Binding(name,f));
                                    }
                                }
                            }
                        }

                        //TODO conditions

                        if (actions.getLength() > 0) {
                            NodeList actionList = actions.item(0).getChildNodes();
                            for (int j = 0; j < actionList.getLength(); j++) {
                                org.w3c.dom.Node actionNode = actionList.item(j);
                                if (actionNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                    Element actionElement = (Element) actionNode;
                                    String typeString = actionElement.getAttribute("type");

                                    if (typeString == null)
                                        throw new IOException(String.format("Type of action #$s in rule #%s must be set", j, i));

                                    Action.Type type = Action.Type.valueOf(typeString);
                                    if (type == null)
                                        throw new IOException(String.format("Type %s of event #$s in rule #%s is not supported", typeString, j, i));

                                    NodeList paramNodes = actionElement.getElementsByTagName("param");
                                    String[] params = new String[paramNodes.getLength()];
                                    for (int k = 0; k < paramNodes.getLength(); k++) {
                                        org.w3c.dom.Node paramNode = paramNodes.item(k);
                                        if (paramNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                            Element paramElement = (Element) paramNode;
                                            params[k] = paramElement.getTextContent().trim();
                                        }
                                    }

                                    rule.actions.add(new Action(type,params));
                                }
                            }
                        }

                        rules.add(rule);
                    }

                }

                return rules;

            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return null;
        }

        private static Function createFunction(String name, NodeList params) {
            Function f = FunctionFactory.createFunction(name);
            ArrayList<Object> paramObjects = new ArrayList<Object>();
            for(int i = 0; i < params.getLength(); i++) {
                org.w3c.dom.Node paramNode = params.item(i);
                if(paramNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element param = (Element)paramNode;
                    String pname  = param.getAttribute("name");
                    String ptype  =param.getAttribute("type");

                    if(ptype==null || ptype.equals("")) {
                        Function fx = new StaticValueFunction();
                        fx.init(param.getTextContent());
                        paramObjects.add(fx);
                    } else {
                        Function fx = createFunction(ptype,param.getChildNodes());
                        paramObjects.add(fx);
                    }
                }
            }
            f.init(paramObjects.toArray());
            return f;
        }

        public static void serializeRules(List<Rule> rules, OutputStream stream) throws ParserConfigurationException, TransformerException, IOException  {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("rules");
            doc.appendChild(rootElement);

            for(Rule rule : rules) {
                rootElement.appendChild(rule.toElement(doc));
            }

            DOMSource domSource = new DOMSource(doc);
            StreamResult result = new StreamResult(stream);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            stream.flush();
        }

    }

    public String getDescription() {
        StringBuilder b = new StringBuilder();
        for(String id: rules.keySet()) {
            b.append(rules.get(id).getDescription());
        }
        return b.toString();
    }
}
