package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class SearchReplace extends Function {


    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) {
        //first param is the string where values should be replaced
        String data = (String)((Function)params[0]).execute(nodeKeeper,current);

        for(int i = 1; i < params.length; i++) {
            String value = (String)((Function)params[i]).execute(nodeKeeper,current);
            data = data.replaceAll("\\{"+i+"\\}",value);
        }

        return data;
    }

    @Override
    public String getName() {
        return "searchReplace";
    }
}
