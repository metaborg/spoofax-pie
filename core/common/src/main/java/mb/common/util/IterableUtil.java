/*
 * Copyright (C) 2007 The Guava Authors
 * Modifications copyright (C) 2019 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mb.common.util;

import java.util.Collection;

// Selectively copied from https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/Iterables.java
public class IterableUtil {
    /**
     * Adds all elements in {@code iterable} to {@code collection}.
     *
     * @return {@code true} if {@code collection} was modified as a result of this operation.
     */
    public static <T> boolean addAll(Collection<T> addTo, Iterable<? extends T> elementsToAdd) {
        if(elementsToAdd instanceof Collection) {
            @SuppressWarnings("unchecked") final Collection<? extends T> c = (Collection<T>) elementsToAdd;
            return addTo.addAll(c);
        }
        return IteratorUtil.addAll(addTo, elementsToAdd.iterator());
    }
}
