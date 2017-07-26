package mb.vfs.list;

import java.io.Serializable;

/**
 * This is a utility class used by selectors, adapted from Apache Ant.
 */
public final class AntPattern implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SEPARATOR = "/";
    private static final char SEPARATOR_CHAR = '/';

    private final String[] tokenizedPattern;
    private final boolean isCaseSensitive;


    public AntPattern(String pattern) {
        this(pattern, true);
    }

    public AntPattern(String pattern, boolean isCaseSensitive) {
        if(pattern.endsWith(SEPARATOR)) {
            pattern += DEEP_TREE_MATCH;
        }
        this.tokenizedPattern = tokenizePathAsArray(pattern);
        this.isCaseSensitive = isCaseSensitive;
    }


    public boolean match(String path) {
        return matchPath(tokenizedPattern, tokenizePathAsArray(path), this.isCaseSensitive);
    }

    /***********************************************************
     * Code below adapted from Apache Ant's SelectorUtils.java *
     ***********************************************************/

    /**
     * The pattern that matches an arbitrary number of directories.
     * 
     * @since Ant 1.8.0
     */
    private static final String DEEP_TREE_MATCH = "**";

    /**
     * Core implementation of matchPath. It is isolated so that it can be called from TokenizedPattern.
     */
    private static boolean matchPath(String[] tokenizedPattern, String[] strDirs, boolean isCaseSensitive) {
        int patIdxStart = 0;
        int patIdxEnd = tokenizedPattern.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strDirs.length - 1;

        // up to first '**'
        while(patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = tokenizedPattern[patIdxStart];
            if(patDir.equals(DEEP_TREE_MATCH)) {
                break;
            }
            if(!match(patDir, strDirs[strIdxStart], isCaseSensitive)) {
                return false;
            }
            patIdxStart++;
            strIdxStart++;
        }
        if(strIdxStart > strIdxEnd) {
            // String is exhausted
            for(int i = patIdxStart; i <= patIdxEnd; i++) {
                if(!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                    return false;
                }
            }
            return true;
        } else {
            if(patIdxStart > patIdxEnd) {
                // String not exhausted, but pattern is. Failure.
                return false;
            }
        }

        // up to last '**'
        while(patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = tokenizedPattern[patIdxEnd];
            if(patDir.equals(DEEP_TREE_MATCH)) {
                break;
            }
            if(!match(patDir, strDirs[strIdxEnd], isCaseSensitive)) {
                return false;
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if(strIdxStart > strIdxEnd) {
            // String is exhausted
            for(int i = patIdxStart; i <= patIdxEnd; i++) {
                if(!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                    return false;
                }
            }
            return true;
        }

        while(patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for(int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if(tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                    patIdxTmp = i;
                    break;
                }
            }
            if(patIdxTmp == patIdxStart + 1) {
                // '**/**' situation, so skip one
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop: for(int i = 0; i <= strLength - patLength; i++) {
                for(int j = 0; j < patLength; j++) {
                    String subPat = tokenizedPattern[patIdxStart + j + 1];
                    String subStr = strDirs[strIdxStart + i + j];
                    if(!match(subPat, subStr, isCaseSensitive)) {
                        continue strLoop;
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if(foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        for(int i = patIdxStart; i <= patIdxEnd; i++) {
            if(!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests whether or not a string matches against a pattern. The pattern may contain two special characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern
     *            The pattern to match against. Must not be <code>null</code>.
     * @param str
     *            The string which must be matched against the pattern. Must not be <code>null</code>.
     * @param caseSensitive
     *            Whether or not matching should be performed case sensitively.
     *
     *
     * @return <code>true</code> if the string matches against the pattern, or <code>false</code> otherwise.
     */
    private static boolean match(String pattern, String str, boolean caseSensitive) {
        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;

        boolean containsStar = false;
        for(int i = 0; i < patArr.length; i++) {
            if(patArr[i] == '*') {
                containsStar = true;
                break;
            }
        }

        if(!containsStar) {
            // No '*'s, so we make a shortcut
            if(patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }
            for(int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if(ch != '?') {
                    if(different(caseSensitive, ch, strArr[i])) {
                        return false; // Character mismatch
                    }
                }
            }
            return true; // String matches against pattern
        }

        if(patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while(true) {
            ch = patArr[patIdxStart];
            if(ch == '*' || strIdxStart > strIdxEnd) {
                break;
            }
            if(ch != '?') {
                if(different(caseSensitive, ch, strArr[strIdxStart])) {
                    return false; // Character mismatch
                }
            }
            patIdxStart++;
            strIdxStart++;
        }
        if(strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            return allStars(patArr, patIdxStart, patIdxEnd);
        }

        // Process characters after last star
        while(true) {
            ch = patArr[patIdxEnd];
            if(ch == '*' || strIdxStart > strIdxEnd) {
                break;
            }
            if(ch != '?') {
                if(different(caseSensitive, ch, strArr[strIdxEnd])) {
                    return false; // Character mismatch
                }
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if(strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            return allStars(patArr, patIdxStart, patIdxEnd);
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while(patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for(int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if(patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if(patIdxTmp == patIdxStart + 1) {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop: for(int i = 0; i <= strLength - patLength; i++) {
                for(int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if(ch != '?') {
                        if(different(caseSensitive, ch, strArr[strIdxStart + i + j])) {
                            continue strLoop;
                        }
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if(foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        return allStars(patArr, patIdxStart, patIdxEnd);
    }

    private static boolean allStars(char[] chars, int start, int end) {
        for(int i = start; i <= end; ++i) {
            if(chars[i] != '*') {
                return false;
            }
        }
        return true;
    }

    private static boolean different(boolean caseSensitive, char ch, char other) {
        return caseSensitive ? ch != other : Character.toUpperCase(ch) != Character.toUpperCase(other);
    }

    /**
     * Breaks a path up into an array of path elements, tokenizing on <code>File.separator</code>.
     *
     * @param path
     *            Path to tokenize. Must not be <code>null</code>.
     *
     * @return an array of path elements from the tokenized path
     */
    private static String[] tokenizePathAsArray(String path) {
        String root = null;
        if(isAbsolutePath(path)) {
            String[] s = dissect(path);
            root = s[0];
            path = s[1];
        }
        char sep = SEPARATOR_CHAR;
        int start = 0;
        int len = path.length();
        int count = 0;
        for(int pos = 0; pos < len; pos++) {
            if(path.charAt(pos) == sep) {
                if(pos != start) {
                    count++;
                }
                start = pos + 1;
            }
        }
        if(len != start) {
            count++;
        }
        String[] l = new String[count + ((root == null) ? 0 : 1)];

        if(root != null) {
            l[0] = root;
            count = 1;
        } else {
            count = 0;
        }
        start = 0;
        for(int pos = 0; pos < len; pos++) {
            if(path.charAt(pos) == sep) {
                if(pos != start) {
                    String tok = path.substring(start, pos);
                    l[count++] = tok;
                }
                start = pos + 1;
            }
        }
        if(len != start) {
            String tok = path.substring(start);
            l[count/* ++ */] = tok;
        }
        return l;
    }

    /*******************************************************
     * Code below adapted from Apache Ant's FileUtils.java *
     *******************************************************/

    /**
     * Verifies that the specified filename represents an absolute path. Differs from new
     * java.io.File("filename").isAbsolute() in that a path beginning with a double file separator--signifying a Windows
     * UNC--must at minimum match "\\a\b" to be considered an absolute path.
     * 
     * @param filename
     *            the filename to be checked.
     * @return true if the filename represents an absolute path.
     * @throws java.lang.NullPointerException
     *             if filename is null.
     * @since Ant 1.6.3
     */
    public static boolean isAbsolutePath(String filename) {
        int len = filename.length();
        if(len == 0) {
            return false;
        }
        char sep = SEPARATOR_CHAR;
        char c = filename.charAt(0);
        return(c == sep);
    }

    /**
     * Dissect the specified absolute path.
     * 
     * @param path
     *            the path to dissect.
     * @return String[] {root, remaining path}.
     * @throws java.lang.NullPointerException
     *             if path is null.
     * @since Ant 1.7
     */
    public static String[] dissect(String path) {
        // make sure we are dealing with an absolute path
        if(!isAbsolutePath(path)) {
            throw new RuntimeException(path + " is not an absolute path");
        }
        String root = null;
        root = SEPARATOR;
        path = path.substring(1);
        return new String[] { root, path };
    }
}
