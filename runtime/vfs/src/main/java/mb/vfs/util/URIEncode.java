package mb.vfs.util;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 * URI encoding utility. From http://stackoverflow.com/a/10032289/499240.
 */
public final class URIEncode {
    private static final BitSet dontNeedEncoding;


    static {
        dontNeedEncoding = new BitSet(256);

        // a-z
        for(int i = 97; i <= 122; ++i) {
            dontNeedEncoding.set(i);
        }
        // A-Z
        for(int i = 65; i <= 90; ++i) {
            dontNeedEncoding.set(i);
        }
        // 0-9
        for(int i = 48; i <= 57; ++i) {
            dontNeedEncoding.set(i);
        }

        // '()*
        for(int i = 39; i <= 42; ++i) {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set(33); // !
        dontNeedEncoding.set(45); // -
        dontNeedEncoding.set(46); // .
        dontNeedEncoding.set(95); // _
        dontNeedEncoding.set(126); // ~

        // HACK: don't encode / \ : to be able to encode a full URI
        dontNeedEncoding.set(47); // /
        dontNeedEncoding.set(92); // \
        dontNeedEncoding.set(58); // :
    }


    /**
     * Escapes all characters except the following: alphabetic, decimal digits, - _ . ! ~ * ' ( )
     * 
     * @param input
     *            A component of a URI
     * @return the escaped URI component
     */
    public static String encode(String input) {
        if(input == null) {
            return input;
        }

        StringBuilder filtered = new StringBuilder(input.length());
        char c;
        for(int i = 0; i < input.length(); ++i) {
            c = input.charAt(i);
            if(dontNeedEncoding.get(c)) {
                filtered.append(c);
            } else {
                final byte[] b = charToBytesUTF(c);

                for(int j = 0; j < b.length; ++j) {
                    filtered.append('%');
                    filtered.append("0123456789ABCDEF".charAt(b[j] >> 4 & 0xF));
                    filtered.append("0123456789ABCDEF".charAt(b[j] & 0xF));
                }
            }
        }
        return filtered.toString();
    }

    private static byte[] charToBytesUTF(char c) {
        try {
            return new String(new char[] { c }).getBytes("UTF-8");
        } catch(UnsupportedEncodingException e) {
            return new byte[] { (byte) c };
        }
    }
}
