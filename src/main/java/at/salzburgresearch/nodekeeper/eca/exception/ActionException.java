package at.salzburgresearch.nodekeeper.eca.exception;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class ActionException extends Exception {
    public ActionException(String msg) {
        super(msg);
    }

    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
