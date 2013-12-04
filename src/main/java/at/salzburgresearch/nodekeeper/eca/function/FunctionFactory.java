package at.salzburgresearch.nodekeeper.eca.function;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class FunctionFactory {

    //TODO: should be done with service loader
    private static HashMap<String,Class> functions = new HashMap<String, Class>(){{
        put("currentNodeData",CurrentNodeData.class);
        put("currentNodeLabel",CurrentNodeLabel.class);
        put("staticValue",StaticValueFunction.class);
        put("toUpperCase",ToUpperCase.class);
        put("nodeData",NodeData.class);
        put("pathNode",PathNode.class);
        put("searchReplace",SearchReplace.class);
    }};

    public static Function createFunction(String clazzname, Object... args) {
        try {
            if(!functions.containsKey(clazzname)) throw new RuntimeException(String.format("Function %s is not supported",clazzname));
            Function f = (Function) functions.get(clazzname).newInstance();
            f.init(args);
            return f;
        } catch (InstantiationException e) {
            throw new RuntimeException(String.format("Function %s cannot be instantiated",clazzname),e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Function %s cannot be instantiated",clazzname),e);
        }
    }

}
