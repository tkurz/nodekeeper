package at.salzburgresearch.nodekeeper.handlers.impl;

import at.salzburgresearch.nodekeeper.handlers.DataHandler;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A handler for Integer type
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class IntegerHandler implements DataHandler<Integer> {


    @Override
    public Integer parse(byte[] data) throws IOException {
        return Integer.parseInt(new String(data));
    }

    @Override
    public byte[] serialize(Integer data) throws IOException {
        return String.valueOf(data).getBytes();
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }
}
