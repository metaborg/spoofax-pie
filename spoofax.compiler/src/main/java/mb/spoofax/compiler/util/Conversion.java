package mb.spoofax.compiler.util;

public class Conversion {
    public static String packageIdToPath(String packageId) {
        return packageId.replace('.', '/');
    }

    public static String pathToPackageId(String path) {
        return path.replace('/', '.');
    }


    public static String nameToJavaId(String name) {
        final StringBuilder output = new StringBuilder();
        for(int i = 0; i < name.length(); i++) {
            if((i == 0 && Character.isJavaIdentifierStart(name.charAt(i))) || (i > 0 && Character.isJavaIdentifierPart(name.charAt(i)))) {
                output.append(name.charAt(i));
            }
        }
        return output.toString();
        // TODO: filter out Java keywords.
    }

    public static String nameToJavaPackageId(String name) {
        // Replace ' ' by '.'.
        final char[] input = name.toLowerCase().replace(' ', '.').toCharArray();
        final StringBuilder output = new StringBuilder();
        int i = 0;
        // Copy 1 character that a letter, '.', or '_', removing invalid characters from the start.
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetter(c) || c == '.' || c == '_') {
                output.append(c);
                break;
            }
        }
        // Copy anything that is a letter, digit, '.', or '_', removing invalid characters.
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetterOrDigit(c) || c == '.' || c == '_')
                output.append(c);
        }
        // Remove '.' at the end, '..', and '.' followed by a number.
        return output.toString().replaceAll("\\.(?=\\.|[0-9]|\\Z)", "");
        // TODO: filter out bad package IDs such as Java keywords, 'java' (not allowed by Java), 'nul' (not allowed by Windows), etc.
    }

    public static String nameToFileExtension(String name) {
        final String input = name
            .toLowerCase() // Convert to lowercase.
            // Remove '-', '.', ' ', and ':'.
            .replace("-", "")
            .replace(".", "")
            .replace(" ", "")
            .replace(":", "");
        // Create a prefix from the first three characters of the input.
        final String prefix = input.substring(0, Math.min(input.length(), 3));
        if(input.length() == 0) {
            return "";
        }
        // Add numbers from the end of the input. TODO: what does this precisely do?
        for(int i = input.length() - 1; ; i--) {
            if(!Character.isDigit(input.charAt(i)) && input.charAt(i) != '.') {
                return prefix + input.substring(Math.max(prefix.length(), Math.min(input.length(), i + 1)));
            } else if(i == prefix.length()) {
                return prefix + input.substring(i);
            }
        }
    }
}
