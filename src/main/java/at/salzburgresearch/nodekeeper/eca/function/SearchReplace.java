package at.salzburgresearch.nodekeeper.eca.function;

import at.salzburgresearch.nodekeeper.eca.exception.BindingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.model.Node;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class SearchReplace extends Function {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Object execute(NodeKeeper nodeKeeper, Node current) throws BindingException {
        //first param is the string where values should be replaced
        String data = (String)((Function)params[0]).execute(nodeKeeper,current);
        if(data != null){
            for(int i = 1; i < params.length; i++) {
                String value = (String)((Function)params[i]).execute(nodeKeeper,current);
                data = data.replaceAll("\\{"+i+"\\}",value);
            }
        } else {
            throw new BindingException(String.format("missing Node '%s'! return empty String", current != null ? current.getPath() : current));
        }
        return data;
    }

    @Override
    public String getName() {
        return "searchReplace";
    }

    @Override
    public String getDescription() {
        StringBuilder b = new StringBuilder();
        b.append(((Function)params[0]).getDescription() +", whereby the data is substituted as follows:<ul>");
        for(int i = 1; i < params.length; i++) {
            b.append("<li>{"+i+"} is " + ((Function)params[i]).getDescription());
            b.append("</li>");
        }
        b.append("</ul>");
        return b.toString();
    }
}
