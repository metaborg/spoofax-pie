package mb.common.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AggregateException extends Exception {
    private final List<Exception> nestedThrowables;
    private final Collection<?> values;

    public <C extends Collection<?>, E extends Exception> AggregateException(C values, Collection<E> exceptions) {
        nestedThrowables = new ArrayList<>(exceptions);
        this.values = values;
    }

    public List<Exception> getInnerExceptions() {
        return new ArrayList<>(nestedThrowables);
    }

    @SuppressWarnings("unchecked") // method is only called on result of ResultAccumulator<E>, so cast is safe
    <E extends Exception> Collection<E> getInnerExceptionsAs() {
        return nestedThrowables
            .stream()
            .map(e ->  (E) e)
            .collect(Collectors.toList());
    }

    public Collection<?> getValues() {
        return values;
    }
}
