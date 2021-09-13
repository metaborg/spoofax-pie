package mb.statix.utils;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Determines if a character array portion matches.
 *
 * Adapted from: <a href="https://github.com/apache/commons-text">org.apache.commons:commons-text</a>
 */
@SuppressWarnings("all")
public interface StringMatcher {

    /**
     * Returns the number of matching characters, zero for no match.
     * <p>
     * This method is called to check for a match. The parameter {@code pos} represents the current position to be
     * checked in the string {@code buffer} (a character array which must not be changed). The API guarantees that
     * {@code pos} is a valid index for {@code buffer}.
     * </p>
     * <p>
     * The matching code may check one character or many. It may check characters preceding {@code pos} as well as those
     * after.
     * </p>
     * <p>
     * It must return zero for no match, or a positive number if a match was found. The number indicates the number of
     * characters that matched.
     * </p>
     *
     * @param buffer the text content to match against, do not change
     * @param pos the starting position for the match, valid for buffer
     * @return The number of matching characters, zero for no match
     */
    default int isMatch(final char[] buffer, final int pos) {
        return isMatch(buffer, pos, 0, buffer.length);
    }

    /**
     * Returns the number of matching characters, {@code 0} if there is no match.
     * <p>
     * This method is called to check for a match against a source {@code buffer}. The parameter {@code start}
     * represents the start position to be checked in the {@code buffer} (a character array which MUST not be changed).
     * The implementation SHOULD guarantees that {@code start} is a valid index in {@code buffer}.
     * </p>
     * <p>
     * The character array may be larger than the active area to be matched. Only values in the buffer between the
     * specified indices may be accessed, in other words: {@code bufferStart <= start < bufferEnd}.
     * </p>
     * <p>
     * The matching code may check one character or many. It may check characters preceding {@code start} as well as
     * those after, so long as no checks exceed the bounds specified.
     * </p>
     * <p>
     * It must return zero for no match, or a positive number if a match was found. The number indicates the number of
     * characters that matched.
     * </p>
     *
     * @param buffer the source text to search, do not change.
     * @param start the starting position for the match, valid in {@code buffer}.
     * @param bufferStart the first active index in the buffer, valid in {@code buffer}.
     * @param bufferEnd the end index (exclusive) of the active buffer, valid in {@code buffer}.
     * @return The number of matching characters, zero if there is no match.
     */
    int isMatch(char[] buffer, int start, int bufferStart, int bufferEnd);

    /**
     * Returns the number of matching characters, zero for no match.
     * <p>
     * This method is called to check for a match. The parameter {@code pos} represents the current position to be
     * checked in the string {@code buffer} (a character array which must not be changed). The API guarantees that
     * {@code pos} is a valid index for {@code buffer}.
     * </p>
     * <p>
     * The matching code may check one character or many. It may check characters preceding {@code pos} as well as those
     * after.
     * </p>
     * <p>
     * It must return zero for no match, or a positive number if a match was found. The number indicates the number of
     * characters that matched.
     * </p>
     *
     * @param buffer the text content to match against, do not change
     * @param pos the starting position for the match, valid for buffer
     * @return The number of matching characters, zero for no match
     */
    default int isMatch(final CharSequence buffer, final int pos) {
        return isMatch(buffer, pos, 0, buffer.length());
    }

    /**
     * Returns the number of matching characters, {@code 0} if there is no match.
     * <p>
     * This method is called to check for a match against a source {@code buffer}. The parameter {@code start}
     * represents the start position to be checked in the {@code buffer} (a character array which MUST not be changed).
     * The implementation SHOULD guarantees that {@code start} is a valid index in {@code buffer}.
     * </p>
     * <p>
     * The character array may be larger than the active area to be matched. Only values in the buffer between the
     * specified indices may be accessed, in other words: {@code bufferStart <= start < bufferEnd}.
     * </p>
     * <p>
     * The matching code may check one character or many. It may check characters preceding {@code start} as well as
     * those after, so long as no checks exceed the bounds specified.
     * </p>
     * <p>
     * It must return zero for no match, or a positive number if a match was found. The number indicates the number of
     * characters that matched.
     * </p>
     *
     * @param buffer the source text to search, do not change.
     * @param start the starting position for the match, valid in {@code buffer}.
     * @param bufferStart the first active index in the buffer, valid in {@code buffer}.
     * @param bufferEnd the end index (exclusive) of the active buffer, valid in {@code buffer}.
     * @return The number of matching characters, zero if there is no match.
     */
    default int isMatch(final CharSequence buffer, final int start, final int bufferStart, final int bufferEnd) {
        final char[] array;
        final int len = StringUtils.lengthOrZero(buffer);
        if (len == 0) {
            array = new char[0];
        } else if (buffer instanceof String) {
            array = ((String) buffer).toCharArray();
        } else {
            array = new char[len];
            for(int i = 0; i < len; i++) {
                array[i] = buffer.charAt(i);
            }
        }
        return isMatch(array, start, bufferStart, bufferEnd);
    }

    /**
     * Returns the size of the matching string. Defaults to 0.
     *
     * @return the size of the matching string.
     */
    default int size() {
        return 0;
    }

}
