package at.salzburgresearch.nodekeeper.eca;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Condition {

    Type type;

    String arg1, arg2;

    public Condition(Type type, String arg1, String arg2) {
        this.type = type;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public enum Type {
        equals, notEquals, lowerThan, greaterThan, lowerThanEquals, greaterThanEquals
    }

    public boolean execute(HashMap<String,String> bindings) {

        String _arg1 = arg1;
        String _arg2 = arg2;

        for(String name : bindings.keySet()) {
            _arg1 = _arg1.replaceAll("\\{"+name+"\\}",bindings.get(name));
            _arg2 = _arg2.replaceAll("\\{"+name+"\\}", bindings.get(name));
        }

        //test
        try {
            int i1 = Integer.parseInt(_arg1);
            int i2 = Integer.parseInt(_arg2);

            switch(type) {
                case equals: return i1 == i2;
                case notEquals: return i1 != i2;
                case lowerThan: return i1 < i2;
                case greaterThan: return i1 > i2;
                case lowerThanEquals: return i1 <= i2;
                case greaterThanEquals: return i1 >= i2;
                default: return false;
            }
        } catch(NumberFormatException e) {
            switch(type) {
                case equals: return _arg1.equals(_arg2);
                case notEquals: return !_arg1.equals(_arg2);
                case lowerThan: return _arg1.length() < _arg2.length();
                case greaterThan: return _arg1.length() > _arg2.length();
                case lowerThanEquals: return _arg1.length() <= _arg2.length();
                case greaterThanEquals: return _arg1.length() >= _arg2.length();
                default: return false;
            }
        }
    }

    public Element toElement(Document doc) {
        Element element = doc.createElement("condition");
        element.setAttribute("type", type.toString());

        Element param1 = doc.createElement("param");
        param1.setTextContent(arg1);
        Element param2 = doc.createElement("param");
        param2.setTextContent(arg2);

        element.appendChild(param1);
        element.appendChild(param2);

        return element;
    }

    public String getDescription() {
        StringBuffer b = new StringBuffer();

        b.append("<span>" + arg1 + "<span>");

        switch(type) {
            case equals: b.append(" is equal to ");break;
            case notEquals: b.append(" is not equal to ");break;
            case lowerThan: b.append(" is lower than to ");break;
            case greaterThan: b.append(" is greater than to ");break;
            case lowerThanEquals: b.append(" is equal or lower than to ");break;
            case greaterThanEquals: b.append(" is equal or greater than to ");break;
        }

        b.append("<span>" + arg2 + "<span>");

        return b.toString();
    }
}
