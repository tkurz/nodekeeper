package at.redlink.nodekeeper.handlers.impl;

import at.redlink.nodekeeper.handlers.DataHandler;
import org.apache.felix.cm.file.ConfigurationHandler;

import java.io.ByteArrayInputStream;import java.io.ByteArrayOutputStream;import java.io.IOException;import java.io.InputStream;import java.lang.Class;import java.lang.Override;import java.util.Dictionary;

/**
 * A handler for Dictionary type
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class DictionaryDataHandler implements DataHandler<Dictionary> {
    @Override
    public Dictionary parse(byte[] data) throws IOException {
        InputStream in = new ByteArrayInputStream(data);
        Dictionary dictionary = ConfigurationHandler.read(in);
        in.close();
        return dictionary;
    }

    @Override
    public byte[] serialize(Dictionary data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ConfigurationHandler.write(out,data);
        byte[] result = out.toByteArray();
        out.flush();
        out.close();
        return result;
    }

    @Override
    public Class<?> getType() {
        return Dictionary.class;
    }
}
