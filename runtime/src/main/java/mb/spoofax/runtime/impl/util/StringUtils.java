/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package mb.spoofax.runtime.impl.util;

import java.io.File;

/**
 * Supplement of commons-lang, the stringSubstitution() was in a simpler implementation available in an older
 * commons-lang implementation.
 * <p>
 * This class is not part of the public API and could change without warning.
 *
 * @version $Id$
 */
public class StringUtils {
    private static final String SINGLE_QUOTE = "\'";
    private static final String DOUBLE_QUOTE = "\"";
    private static final char SLASH_CHAR = '/';
    private static final char BACKSLASH_CHAR = '\\';

    /**
     * Fixes the file separator char for the target platform using the following replacement.
     * <p>
     * <ul>
     * <li>'/' &#x2192; File.separatorChar</li>
     * <li>'\\' &#x2192; File.separatorChar</li>
     * </ul>
     *
     * @param arg the argument to fix
     * @return the transformed argument
     */
    public static String fixFileSeparatorChar(final String arg) {
        return arg.replace(SLASH_CHAR, File.separatorChar).replace(BACKSLASH_CHAR, File.separatorChar);
    }

    /**
     * Put quotes around the given String if necessary.
     * <p>
     * If the argument doesn't include spaces or quotes, return it as is. If it contains double quotes, use single
     * quotes - else surround the argument by double quotes.
     * </p>
     *
     * @param argument the argument to be quoted
     * @return the quoted argument
     * @throws IllegalArgumentException If argument contains both types of quotes
     */
    public static String quoteArgument(final String argument) {
        String cleanedArgument = argument.trim();

        // strip the quotes from both ends
        while(cleanedArgument.startsWith(SINGLE_QUOTE) || cleanedArgument.startsWith(DOUBLE_QUOTE)) {
            cleanedArgument = cleanedArgument.substring(1);
        }

        while(cleanedArgument.endsWith(SINGLE_QUOTE) || cleanedArgument.endsWith(DOUBLE_QUOTE)) {
            cleanedArgument = cleanedArgument.substring(0, cleanedArgument.length() - 1);
        }

        final StringBuilder buf = new StringBuilder();
        if(cleanedArgument.indexOf(DOUBLE_QUOTE) > -1) {
            if(cleanedArgument.indexOf(SINGLE_QUOTE) > -1) {
                throw new IllegalArgumentException("Can't handle single and double quotes in same argument");
            }
            return buf.append(SINGLE_QUOTE).append(cleanedArgument).append(SINGLE_QUOTE).toString();
        } else if(cleanedArgument.indexOf(SINGLE_QUOTE) > -1 || cleanedArgument.indexOf(" ") > -1) {
            return buf.append(DOUBLE_QUOTE).append(cleanedArgument).append(DOUBLE_QUOTE).toString();
        } else {
            return cleanedArgument;
        }
    }
}
