package mb.statix.utils;

import java.io.PrintWriter;
import java.util.function.Function;

/**
 * String utility functions.
 */
public final class StringUtils {
    private StringUtils() { /* Cannot be instantiated. */ }

    /**
     * Escapes the specified string for use in Java.
     *
     * @param s the string to escape
     * @return the escaped string
     */
    public static String escapeJava(CharSequence s) {
        if (s == null) return null;
        if (s.length() == 0) return "";

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '\'': sb.append("\\'"); break;
                case '\"': sb.append("\\\""); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\r': sb.append("\\r"); break;
                case '\n': sb.append("\\n"); break;
                case '\\': sb.append("\\\\"); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }

    /**
     * Gets the size of a {@link CharSequence}; or 0 when it is {@code null}.
     *
     * @param cs the {@link CharSequence}
     * @return the length of the {@link CharSequence}; or 0 when the sequence is {@code null}
     */
    public static int lengthOrZero(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }
}
