package mb.common.result;

@FunctionalInterface
public interface ExceptionalFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
