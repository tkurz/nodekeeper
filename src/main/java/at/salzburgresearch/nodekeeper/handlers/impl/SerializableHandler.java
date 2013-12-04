package at.salzburgresearch.nodekeeper.handlers.impl;

import at.salzburgresearch.nodekeeper.handlers.DataHandler;
import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class SerializableHandler implements DataHandler<Serializable> {
    @Override
    public Serializable parse(byte[] data) throws IOException {
        return (Serializable) SerializationUtils.deserialize(data);
    }

    @Override
    public byte[] serialize(Serializable data) throws IOException {
        return SerializationUtils.serialize(data);
    }

    @Override
    public Class<?> getType() {
        return Object.class;
    }
}
