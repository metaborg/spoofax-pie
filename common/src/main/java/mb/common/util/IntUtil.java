/*
 * Copyright (C) 2008 The Guava Authors
 * Modifications copyright (C) 2019 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package mb.common.util;

import java.util.Collection;

import static mb.common.util.Preconditions.checkArgument;
import static mb.common.util.Preconditions.checkNotNull;

// Selectively copied from https://github.com/google/guava/blob/master/guava/src/com/google/common/primitives/Ints.java.
public class IntUtil {
    /**
     * Returns the least value present in {@code array}.
     *
     * @param array a <i>nonempty</i> array of {@code int} values
     * @return the value present in {@code array} that is less than or equal to every other value in the array
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static int min(int... array) {
        checkArgument(array.length > 0);
        int min = array[0];
        for(int i = 1; i < array.length; i++) {
            if(array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * Returns the greatest value present in {@code array}.
     *
     * @param array a <i>nonempty</i> array of {@code int} values
     * @return the value present in {@code array} that is greater than or equal to every other value in the array
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static int max(int... array) {
        checkArgument(array.length > 0);
        int max = array[0];
        for(int i = 1; i < array.length; i++) {
            if(array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * Clamps a value within a closed range.
     *
     * @param value The integer value.
     * @param min The minimum value, inclusive.
     * @param max The maximum value, inclusive.
     * @return The clamped value, which is between min and max.
     */
    public static int clamp(int value, int min, int max) {
        if (min > max) throw new IllegalArgumentException("max (" + max + ") must be greater than or equal to min (" + min + ").");
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Returns an array containing each value of {@code collection}, converted to a {@code int} value in the manner of
     * {@link Number#intValue}.
     *
     * <p>Elements are copied from the argument collection as if by {@code collection.toArray()}.
     * Calling this method is as thread-safe as calling that method.
     *
     * @param collection a collection of {@code Number} instances
     * @return an array containing the same values as {@code collection}, in the same order, converted to primitives
     * @throws NullPointerException if {@code collection} or any of its elements is null
     * @since 1.0 (parameter was {@code Collection<Integer>} before 12.0)
     */
    public static int[] toArray(Collection<? extends Number> collection) {
        Object[] boxedArray = collection.toArray();
        int len = boxedArray.length;
        int[] array = new int[len];
        for(int i = 0; i < len; i++) {
            // checkNotNull for GWT (do not optimize)
            array[i] = ((Number) checkNotNull(boxedArray[i])).intValue();
        }
        return array;
    }
}
