/*
 * Copyright (c) 2004-2011 QOS.ch
 * Modifications copyright (C) 2019 Delft University of Technology
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

// Selectively copied from https://github.com/qos-ch/slf4j/blob/master/slf4j-api/src/main/java/org/slf4j/helpers/MessageFormatter.java.
public class StringFormatter {
    private static final char DELIM_START = '{';
    private static final String DELIM_STR = "{}";
    private static final char ESCAPE_CHAR = '\\';

    public static String format(String messagePattern, Object... argArray) {
        int i = 0;
        int j;
        // use string builder for better multicore performance
        StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);

        int L;
        for(L = 0; L < argArray.length; L++) {

            j = messagePattern.indexOf(DELIM_STR, i);

            if(j == -1) {
                // no more variables
                if(i == 0) { // this is a simple string
                    return messagePattern;
                } else { // add the tail string which contains no variables and return
                    // the result.
                    sbuf.append(messagePattern, i, messagePattern.length());
                    return sbuf.toString();
                }
            } else {
                if(isEscapedDelimeter(messagePattern, j)) {
                    if(!isDoubleEscaped(messagePattern, j)) {
                        L--; // DELIM_START was escaped, thus should not be incremented
                        sbuf.append(messagePattern, i, j - 1);
                        sbuf.append(DELIM_START);
                        i = j + 1;
                    } else {
                        // The escape character preceding the delimiter start is
                        // itself escaped: "abc x:\\{}"
                        // we have to consume one backward slash
                        sbuf.append(messagePattern, i, j - 1);
                        deeplyAppendParameter(sbuf, argArray[L], new HashMap<>());
                        i = j + 2;
                    }
                } else {
                    // normal case
                    sbuf.append(messagePattern, i, j);
                    deeplyAppendParameter(sbuf, argArray[L], new HashMap<>());
                    i = j + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        sbuf.append(messagePattern, i, messagePattern.length());
        return sbuf.toString();
    }

    private static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {
        if(delimeterStartIndex == 0) {
            return false;
        }
        char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
        return potentialEscape == ESCAPE_CHAR;
    }

    private static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
        return delimeterStartIndex >= 2 && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR;
    }

    // special treatment of array values was suggested by 'lizongbo'
    private static void deeplyAppendParameter(StringBuilder sbuf, @Nullable Object o, Map<Object[], Object> seenMap) {
        if(o == null) {
            sbuf.append("null");
            return;
        }
        if(!o.getClass().isArray()) {
            safeObjectAppend(sbuf, o);
        } else {
            // check for primitive array types because they
            // unfortunately cannot be cast to Object[]
            if(o instanceof boolean[]) {
                booleanArrayAppend(sbuf, (boolean[]) o);
            } else if(o instanceof byte[]) {
                byteArrayAppend(sbuf, (byte[]) o);
            } else if(o instanceof char[]) {
                charArrayAppend(sbuf, (char[]) o);
            } else if(o instanceof short[]) {
                shortArrayAppend(sbuf, (short[]) o);
            } else if(o instanceof int[]) {
                intArrayAppend(sbuf, (int[]) o);
            } else if(o instanceof long[]) {
                longArrayAppend(sbuf, (long[]) o);
            } else if(o instanceof float[]) {
                floatArrayAppend(sbuf, (float[]) o);
            } else if(o instanceof double[]) {
                doubleArrayAppend(sbuf, (double[]) o);
            } else {
                objectArrayAppend(sbuf, (Object[]) o, seenMap);
            }
        }
    }

    private static void safeObjectAppend(StringBuilder sbuf, Object o) {
        try {
            String oAsString = o.toString();
            sbuf.append(oAsString);
        } catch(Throwable t) {
            System.err.println("Failed toString() invocation on an object of type [" + o.getClass().getName() + "]");
            t.printStackTrace();
            sbuf.append("[FAILED toString()]");
        }

    }

    private static void objectArrayAppend(StringBuilder sbuf, Object[] a,
        Map<Object[], Object> seenMap) {
        sbuf.append('[');
        if(!seenMap.containsKey(a)) {
            seenMap.put(a, null);
            final int len = a.length;
            for(int i = 0; i < len; i++) {
                deeplyAppendParameter(sbuf, a[i], seenMap);
                if(i != len - 1)
                    sbuf.append(", ");
            }
            // allow repeats in siblings
            seenMap.remove(a);
        } else {
            sbuf.append("...");
        }
        sbuf.append(']');
    }

    private static void booleanArrayAppend(StringBuilder sbuf, boolean[] a) {
        sbuf.append('[');
        final int len = a.length;
        for(int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if(i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void byteArrayAppend(StringBuilder sbuf, byte[] a) {
        sbuf.append('[');
        final int len = a.length;
        for(int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if(i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void charArrayAppend(StringBuilder sbuf, char[] a) {
        sbuf.append('[');
        final int len = a.length;
        for(int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if(i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void shortArrayAppend(StringBuilder sbuf, short[] a) {
        sbuf.append('[');
        final int len = a.length;
        for(int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if(i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void intArrayAppend(StringBuilder sbuf, int[] a) {
        sbuf.append('[');
        final int len = a.length;
        for(int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if(i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void longArrayAppend(StringBuilder sbuf, long[] a) {
        sbuf.append('[');
        final int len = a.length;
        for(int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if(i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void floatArrayAppend(StringBuilder sbuf, float[] a) {
        sbuf.append('[');
        final int len = a.length;
        for(int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if(i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void doubleArrayAppend(StringBuilder sbuf, double[] a) {
        sbuf.append('[');
        final int len = a.length;
        for(int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if(i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }
}
