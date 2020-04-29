package mb.common.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Enumeration;

public class Properties extends java.util.Properties {
    public void storeWithoutDate(BufferedWriter writer) throws IOException {
        final Enumeration<Object> e = keys();
        while(e.hasMoreElements()) {
            final Object key = e.nextElement();
            final Object value = get(key);
            writer.write(key + "=" + value);
            writer.newLine();
        }
        writer.flush();
    }
}
