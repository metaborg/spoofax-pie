package mb.pipe.run.core.util;

import java.util.List;

import com.google.common.collect.Lists;

public class Lists2 {
    public static <T> List<T> concat(List<T> list, @SuppressWarnings("unchecked") T... elems) {
        final List<T> newList = Lists.newArrayList(list);
        for(T elem : elems) {
            newList.add(elem);
        }
        return newList;
    }
}
