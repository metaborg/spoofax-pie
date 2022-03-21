package mb.spoofax.compiler.util;

public class StringUtil {
    public static String doubleQuote(Object obj) {
        return "\"" + obj + "\"";
    }

    public static String capitalizeFirstCharacter(String str) {
        final char firstChar = str.charAt(0);
        if(Character.isLowerCase(firstChar)) {
            return Character.toUpperCase(firstChar) + str.substring(1);
        }
        return str;
    }

    public static String uncapitalizeFirstCharacter(String str) {
        final char firstChar = str.charAt(0);
        if(Character.isUpperCase(firstChar)) {
            return Character.toLowerCase(firstChar) + str.substring(1);
        }
        return str;
    }
}
