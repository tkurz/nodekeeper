package at.salzburgresearch.nodekeeper.exception;

import java.lang.Exception;import java.lang.String; /**
 * The Exception is thrown by several NodeKeeper methods.
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class NodeKeeperException extends Exception {

    public NodeKeeperException(String msg) {
        super(msg);
    }

}
