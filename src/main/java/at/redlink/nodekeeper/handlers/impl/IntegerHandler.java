package at.redlink.nodekeeper.handlers.impl;

import at.redlink.nodekeeper.handlers.DataHandler;

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
        ByteBuffer buffer= ByteBuffer.allocate(4);
        buffer.put(data);
        return buffer.getInt(0);
    }

    @Override
    public byte[] serialize(Integer data) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(data);
        return buffer.array();
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }
}
