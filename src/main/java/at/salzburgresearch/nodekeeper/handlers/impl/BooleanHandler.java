package at.salzburgresearch.nodekeeper.handlers.impl;

import at.salzburgresearch.nodekeeper.handlers.DataHandler;

import java.io.IOException;

/**
 * A handler for Boolean type. Boolean is stored as string value in zookeeper.
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class BooleanHandler implements DataHandler<Boolean> {
    @Override
    public Boolean parse(byte[] data) throws IOException {
        if(new String(data).equalsIgnoreCase("true")) return true;
        if(new String(data).equalsIgnoreCase("false")) return false;
        throw new IOException("cannot parse to boolean");
    }

    @Override
    public byte[] serialize(Boolean data) throws IOException {
        return String.valueOf(data).getBytes();
    }

    @Override
    public Class<?> getType() {
        return Boolean.class;
    }
}
