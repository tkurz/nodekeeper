package at.salzburgresearch.nodekeeper.model;

import java.lang.String; /**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class Node<T> {

    private String path;
    private T data;
    private int version;

    public Node(String path) {
        this.path = path;
        this.data = null;
        this.version = -1;
    }

    public Node(String path, T data) {
        this(path,data,-1);
    }

    public Node(String path, T data, int version) {
        this.path = path;
        this.data = data;
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public T getData() {
        return data;
    }

    public int getVersion() {
        return version;
    }

    public String toString() {
        return "["+getPath()+" ("+getVersion()+") "+getData().toString()+"]";
    }

}
