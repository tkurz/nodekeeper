package at.redlink.nodekeeper.handlers;

import java.io.IOException;import java.lang.Class;

/**
 * How to serialize T into byte[] and vice versa.
 * For more implementations http://www.daniweb.com/software-development/java/code/216874/primitive-types-as-byte-arrays
 * might be useful.
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public interface DataHandler<T> {

    public T parse(byte[] data) throws IOException;
    public byte[] serialize(T data) throws IOException;
    public Class<?> getType();

}
