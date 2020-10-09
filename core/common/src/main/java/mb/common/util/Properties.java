package mb.common.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Properties extends java.util.Properties {
    // From org.eclipse.core.internal.preferences.SortedProperties
    @Override public synchronized Enumeration<Object> keys() {
        final TreeSet<Object> set = new TreeSet<>();
        for(Enumeration<?> e = super.keys(); e.hasMoreElements(); ) {
            set.add(e.nextElement());
        }
        return Collections.enumeration(set);
    }

    // From org.eclipse.core.internal.preferences.SortedProperties
    @Override public Set<Map.Entry<Object, Object>> entrySet() {
        final TreeSet<Map.Entry<Object, Object>> set = new TreeSet<>(propertyComparator);
        set.addAll(super.entrySet());
        return set;
    }


    public void storeWithoutDate(Writer writer) throws IOException {
        for(Map.Entry<Object, Object> entry : entrySet()) {
            writer.write(entry.getKey().toString());
            writer.write("=");
            writer.write(entry.getValue().toString());
            writer.write(System.lineSeparator());
        }
    }


    private static class PropertyComparator implements Comparator<Map.Entry<Object, Object>> {
        @Override public int compare(Map.Entry<Object, Object> e1, Map.Entry<Object, Object> e2) {
            final String s1 = e1.getKey().toString();
            final String s2 = e2.getKey().toString();
            return s1.compareTo(s2);
        }
    }

    private static final PropertyComparator propertyComparator = new PropertyComparator();
}
