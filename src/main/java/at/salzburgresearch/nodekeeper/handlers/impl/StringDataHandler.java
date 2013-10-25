package at.salzburgresearch.nodekeeper.handlers.impl;

import at.salzburgresearch.nodekeeper.handlers.DataHandler;

/**
 * A handler for String type
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class StringDataHandler implements DataHandler<String> {
    @Override
    public String parse(byte[] data) {
        return new String(data);
    }

    @Override
    public byte[] serialize(String data) {
        return data.getBytes();
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }
}
