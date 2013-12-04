package at.salzburgresearch.nodekeeper.tests.ruleEngineTests;

import at.salzburgresearch.nodekeeper.eca.function.Function;
import at.salzburgresearch.nodekeeper.eca.function.FunctionFactory;
import at.salzburgresearch.nodekeeper.model.Node;
import org.junit.Test;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class FunctionTests {

    @Test
    public void testSplitter() {
        Object[] obj = {"4"};
        Function function = FunctionFactory.createFunction("pathNode",obj);
        String s = (String)function.execute(null,new Node("/users/user1/dataset/dataset1/version"));
        System.out.println(s);
    }

}
