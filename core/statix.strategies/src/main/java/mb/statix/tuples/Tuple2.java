package mb.statix.tuples;

import java.util.Objects;

/**
 * A tuple with two components.
 *
 * @param <T1> the type of the first component (contravariant)
 * @param <T2> the type of the second component (contravariant)
 */
public final class Tuple2<T1, T2> {

    private final T1 item1;
    private final T2 item2;

    /**
     * Initializes a new instance of the {@link Tuple2} class.
     * @param item1 the first item in the tuple
     * @param item2 the second item in the tuple
     */
    public Tuple2(T1 item1, T2 item2) {

        this.item1 = item1;
        this.item2 = item2;
    }

    /**
     * Gets the first item in the tuple.
     *
     * @return the first item in the tuple
     */
    public T1 getItem1() {
        return item1;
    }

    /**
     * Gets the second item in the tuple.
     *
     * @return the second item in the tuple
     */
    public T2 getItem2() {
        return item2;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Tuple2)) return false;
        Tuple2<?, ?> that = (Tuple2<?, ?>)o;
        return Objects.equals(this.item1, that.item1)
            && Objects.equals(this.item2, that.item2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item1, item2);
    }

    @Override public String toString() {
        return "(" + item1 + item2 + ')';
    }
}
