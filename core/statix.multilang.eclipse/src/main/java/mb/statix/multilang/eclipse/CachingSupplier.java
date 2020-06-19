package mb.statix.multilang.eclipse;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

class CachingSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private final AtomicReference<T> value = new AtomicReference<>();

    public CachingSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    public T get() {
        T val = value.get();
        if(val == null) {
            synchronized(value) {
                val = value.get();
                if(val == null) {
                    val = Objects.requireNonNull(delegate.get());
                    value.set(val);
                }
            }
        }
        return val;
    }
}
