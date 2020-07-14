package mb.common.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AggregateException extends Exception {
    private final List<Exception> nestedThrowables;
    private final Collection<?> values; // For completeness maintain values as well

    public AggregateException(Collection<?> values, Collection<? extends Exception> exceptions) {
        nestedThrowables = new ArrayList<>(exceptions);
        this.values = values;
    }

    public List<Exception> getInnerExceptions() {
        return new ArrayList<>(nestedThrowables);
    }

    public Collection<?> getValues() {
        return values;
    }
}
