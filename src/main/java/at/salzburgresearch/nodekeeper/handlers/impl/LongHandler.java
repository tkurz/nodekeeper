package at.salzburgresearch.nodekeeper.handlers.impl;

import at.salzburgresearch.nodekeeper.handlers.DataHandler;

import java.io.IOException;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class LongHandler implements DataHandler<Long> {


    @Override
    public Long parse(byte[] data) throws IOException {
        return Long.parseLong(new String(data));
    }

    @Override
    public byte[] serialize(Long data) throws IOException {
        return String.valueOf(data).getBytes();
    }

    @Override
    public Class<?> getType() {
        return Long.class;
    }
}