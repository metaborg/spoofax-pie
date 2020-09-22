package mb.common.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AggregateException extends Exception {
    private final List<Exception> nestedThrowables;
    private final Collection<?> values; // For completeness maintain values as well

    public AggregateException(Collection<?> values, Collection<? extends Exception> exceptions) {
        nestedThrowables = new ArrayList<>(exceptions);
        if(nestedThrowables.size() == 1) {
            initCause(nestedThrowables.get(0));
        } else {
            for(Exception e : exceptions) addSuppressed(e);
        }
        this.values = values;
    }

    public List<Exception> getInnerExceptions() {
        return new ArrayList<>(nestedThrowables);
    }

    public Collection<?> getValues() {
        return values;
    }


    @Override public String getMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("One or more exceptions occurred:\n\n");
        for(Exception e : nestedThrowables) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created.
    }
}
