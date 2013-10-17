package at.reddlink.nodekeeper.tests.readWrite;

import at.redlink.nodekeeper.handlers.DataHandler;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class DateDataHandler implements DataHandler<Date> {

    private SimpleDateFormat formater = new SimpleDateFormat("yyyy-mm-dd HH:MM:ss.SSS");

    @Override
    public Date parse(byte[] data) throws IOException {
        try {
            return formater.parse(new String(data));
        } catch (ParseException e) {
            throw new IOException(String.format("cannot parse data to 'Date'"));
        }
    }

    @Override
    public byte[] serialize(Date data) throws IOException {
        return formater.format(data).getBytes();
    }

    @Override
    public Class<?> getType() {
        return Date.class;
    }
}
