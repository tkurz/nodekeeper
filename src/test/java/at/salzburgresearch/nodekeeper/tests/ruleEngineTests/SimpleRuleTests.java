package at.salzburgresearch.nodekeeper.tests.ruleEngineTests;

import at.salzburgresearch.nodekeeper.eca.*;
import at.salzburgresearch.nodekeeper.eca.function.CurrentNodeData;
import at.salzburgresearch.nodekeeper.eca.function.CurrentNodeLabel;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;
import at.salzburgresearch.nodekeeper.tests.NodeKeeperTest;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class SimpleRuleTests extends NodeKeeperTest {

    @Test
    public void createNodeWithRuleTest() throws InterruptedException, IOException, NodeKeeperException, TransformerException, ParserConfigurationException {
        RuleHandler ruleHandler = new RuleHandler(nodeKeeper);

        Rule rule = new Rule();
        rule.event = new Event(Event.Type.nodeCreated,"/my/event/.+");
        rule.bindings.add(new Binding("name",new CurrentNodeLabel(),false));
        rule.bindings.add(new Binding("data",new CurrentNodeData(),false));
        rule.actions.add(new Action(Action.Type.createUpdateNode,"/my/action/{name}","Hallo {data}"));

        ruleHandler.addRule(rule);

        nodeKeeper.writeNode(new Node<String>("/my/event/node1","Testwelt"),String.class);

        Thread.sleep(2000);

        Node<String> node = nodeKeeper.readNode("/my/action/node1",String.class);

        nodeKeeper.writeNode(new Node<String>("/my/event/node2","World2"),String.class);
        nodeKeeper.writeNode(new Node<String>("/my/event/node3","World3"),String.class);

        Thread.sleep(2000);

        Node<String> node2 = nodeKeeper.readNode("/my/action/node2",String.class);
        Node<String> node3 = nodeKeeper.readNode("/my/action/node3",String.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node2);
        Assert.assertNotNull(node3);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ruleHandler.writeRules(baos);

        //System.out.println(baos.toString());

    }

    @Test
    public void checkConditions() throws InterruptedException, IOException, NodeKeeperException, TransformerException, ParserConfigurationException {
        RuleHandler ruleHandler = new RuleHandler(nodeKeeper);

        Rule rule = new Rule();
        rule.event = new Event(Event.Type.nodeCreated,"/my/event/.+");
        rule.bindings.add(new Binding("name",new CurrentNodeLabel(),false));
        rule.bindings.add(new Binding("data",new CurrentNodeData(),false));
        rule.conditions.add(new Condition(Condition.Type.equals,"{name}","node1"));
        rule.actions.add(new Action(Action.Type.createUpdateNode,"/my/action/{name}","Hallo {data}"));

        ruleHandler.addRule(rule);

        nodeKeeper.writeNode(new Node<String>("/my/event/node1","Testwelt"),String.class);

        Thread.sleep(2000);

        Node<String> node = nodeKeeper.readNode("/my/action/node1",String.class);

        nodeKeeper.writeNode(new Node<String>("/my/event/node2","World2"),String.class);
        nodeKeeper.writeNode(new Node<String>("/my/event/node3","World3"),String.class);

        Thread.sleep(2000);

        Node<String> node2 = nodeKeeper.readNode("/my/action/node2",String.class);
        Node<String> node3 = nodeKeeper.readNode("/my/action/node3",String.class);

        Assert.assertNotNull(node);
        Assert.assertNull(node2);
        Assert.assertNull(node3);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ruleHandler.writeRules(baos);

        System.out.println(baos.toString());

    }

    @Test
    public void deleteNodeWithRuleTest() throws InterruptedException, IOException, NodeKeeperException {

        nodeKeeper.writeNode(new Node<String>("/my/event/node1","One"),String.class);
        nodeKeeper.writeNode(new Node<String>("/my/action/node1","Two"),String.class);
        Node<String> node1 = nodeKeeper.readNode("/my/action/node1",String.class);

        RuleHandler ruleHandler = new RuleHandler(nodeKeeper);

        Rule rule = new Rule();
        rule.event = new Event(Event.Type.nodeDeleted,"/my/event/.+");
        rule.bindings.add(new Binding("$name",new CurrentNodeLabel(),false));
        rule.actions.add(new Action(Action.Type.deleteNode,"/my/action/$name"));

        ruleHandler.addRule(rule);

        nodeKeeper.deleteNode(new Node<String>("/my/event/node1"));

        Thread.sleep(2000);

        Node<String> node2 = nodeKeeper.readNode("/my/action/node1",String.class);
    }

    @Test
    public void parseRulesTest() throws InterruptedException, IOException, NodeKeeperException {
        InputStream in = new FileInputStream("src/test/resources/rules.xml");

        RuleHandler handler = new RuleHandler(nodeKeeper);
        handler.readRules(in);

        nodeKeeper.writeNode(new Node<String>("/my/event/node","World"),String.class);

        Thread.sleep(2000);

        Assert.assertEquals("Hello World: Test",nodeKeeper.readNode("/my/action/NODE",String.class).getData());

        InputStream in2 = new FileInputStream("src/test/resources/rules2.xml");
        handler.readRules(in2);

        nodeKeeper.writeNode(new Node<String>("/my/event/node","Update"),String.class);
        Thread.sleep(2000);

        Assert.assertNotNull(nodeKeeper.readNode("/my/action/NODE",String.class));

        System.out.println(handler.getDescription());

    }

    @Test
    public void testOrFunction() throws IOException, InterruptedException, NodeKeeperException {
        InputStream in = new FileInputStream("src/test/resources/rules3.xml");

        RuleHandler handler = new RuleHandler(nodeKeeper);
        handler.readRules(in);

        nodeKeeper.writeNode(new Node<String>("/parent/instance","instance123"),String.class);
        nodeKeeper.writeNode(new Node<String>("/parent/node1","node1"),String.class);

        Thread.sleep(2000);

        Assert.assertEquals("/data/stanbol/indexes/instance123",nodeKeeper.readNode("/result/node1",String.class).getData());

        nodeKeeper.writeNode(new Node<String>("/indexdir","/standard"),String.class);
        nodeKeeper.writeNode(new Node<String>("/parent/node2","node2"),String.class);

        Thread.sleep(2000);

        Assert.assertEquals("/standard",nodeKeeper.readNode("/result/node2",String.class).getData());
    }

    @Test
    public void testBindingStrictMode() throws IOException, InterruptedException, NodeKeeperException {
        InputStream in = new FileInputStream("src/test/resources/rules2.xml");

        RuleHandler handler = new RuleHandler(nodeKeeper);
        handler.readRules(in);
        Thread.sleep(2000);

        nodeKeeper.writeNode(new Node<String>("/my/demo/test/set/version","0"),String.class);
        Thread.sleep(2000);

        Node<String> n = nodeKeeper.readNode("/stanbol/instances/sites/test/set_kiwi_yard", String.class);
        Assert.assertNotNull(n);
        Assert.assertEquals(Binding.DEFAULT_BINDING,n.getData());

        in.close();
    }

    @Test
    public void testBindingStrictModeTrue() throws IOException, InterruptedException, NodeKeeperException {
        InputStream in = new FileInputStream("src/test/resources/rules4.xml");

        RuleHandler handler = new RuleHandler(nodeKeeper);
        handler.readRules(in);

        nodeKeeper.writeNode(new Node<String>("/my/demo/test/set/version","0"),String.class);
        Thread.sleep(2000);

        Node<String> n = nodeKeeper.readNode("/stanbol/instances/sites/test/set_kiwi_yard", String.class);
        Assert.assertNull(n);

        in.close();
    }

}
