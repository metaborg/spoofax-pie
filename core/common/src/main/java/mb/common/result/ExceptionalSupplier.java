package mb.common.result;

@FunctionalInterface
public interface ExceptionalSupplier<T, E extends Exception> {
    T get() throws E;
}
