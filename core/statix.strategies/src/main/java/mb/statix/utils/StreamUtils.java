package mb.statix.utils;

import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility methods for working with streams.
 */
public final class StreamUtils {

    /**
     * Performs a transformation on the whole contents of the stream.
     *
     * This is an intermediate terminating operation,
     * meaning that it evaluates the whole chain
     * and recreates a stream out of the result.
     *
     * @param stream the stream
     * @param transformation the transformation to perform
     * @param <T> the type of input values
     * @param <R> the type of output values
     * @return the new stream
     */
    public static <T, R> Stream<R> transform(Stream<T> stream, Function<List<T>, Iterable<R>> transformation) {
        List<T> buffer = stream.collect(Collectors.toList());
        Iterable<R> result = transformation.apply(buffer);
        return StreamSupport.stream(result.spliterator(), false);
    }

    /**
     * Returns all subsets of the given size.
     *
     * This is an intermediate terminating operation,
     * meaning that it evaluates the whole chain
     * and recreates a stream out of the result.
     *
     * @param stream the stream from which to take the subsets
     * @param size the requested size of the subsets
     * @param <T> the type of values
     * @return the new stream of subsets
     */
    public static <T> Stream<Collection<T>> subsetsOfSize(Stream<T> stream, int size) {
        if (size == 0) return Stream.of(Collections.emptyList());
        List<T> list = stream.collect(Collectors.toList());
        if (size == 1) return list.stream().map(Collections::singletonList);
        if (size == list.size()) return Stream.of(list);
        if (size < 0 || size > list.size()) return Stream.empty();
        return StreamSupport.stream(new SubsetStream<>(list, size), false);
    }

    /**
     * Produces the subsets of a specific cardinality of the elements in the sequence.
     */
    private static final class SubsetStream<T> implements Spliterator<Collection<T>> {

        private final List<T> list;
        private final int size;
        private BigInteger nextSubset;

        public SubsetStream(List<T> list, int size) {
            this.list = list;
            this.size = size;
            this.nextSubset = BigInteger.ONE.shiftLeft(size).subtract(BigInteger.ONE);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Collection<T>> action) {
            if (nextSubset.bitLength() > list.size()) return false;
            Collection<T> subset = new Sublist<>(list, nextSubset, size);
            this.nextSubset = snoob(this.nextSubset);
            action.accept(subset);
            return true;
        }

        /**
         * Gets the next higher integer with the same number of bits set.
         *
         * @param x a value that is not 0
         * @return the next higher integer with the same number of bits set
         */
        private BigInteger snoob(BigInteger x) {
            // Inspired by: Hacker's Delight
            BigInteger s = x.and(x.negate());           // get the right-most set bit
            BigInteger r = x.add(s);                    // get the bit set to the left of the right-most pattern
            BigInteger m = x.xor(r);                    // isolate the pattern from the rest
            BigInteger a = m.shiftRight(2).divide(s);   // adjust and correct the pattern
            return r.or(a);                             // integrate the new pattern
        }

        @Override
        public Spliterator<Collection<T>> trySplit() {
            // Not supported
            return null;
        }

        @Override
        public long estimateSize() {
            // n choose r
            int n = list.size();
            int r = this.size;

            // Inspired by: https://stackoverflow.com/a/11809815/146622
            // NOTE: For large n or r, this may overflow
            if (r == 0 || r == n) return 1;     // the empty set or the original set
            if (r == 1) return n;               // a set for each individual element
            if (r < 0 || r > n) return 0;       // nothing
            if(r > n - r) r = n - r;            // because C(n, r) == C(n, n - r)
            long ans = 1;
            int i;

            for(i = 1; i <= r; i++) {
                ans *= n - r + i;
                ans /= i;
            }

            return ans;
        }

        @Override
        public int characteristics() {
            // We return the exact size.
            return Spliterator.SIZED;
        }

    }


    /**
     * An immutable sublist view of a given list.
     *
     * @param <T> the type of elements in the collection
     */
    private static final class Sublist<T> extends AbstractList<T> {

        private final List<T> list;
        private final BigInteger bits;
        private final int size;

        /**
         * Initializes a new instance of the {@link Sublist} class.
         *
         * @param list the list for which this is a view
         * @param bits bits indicating which elements to include in the subset (MSB of index 0 is first element of list).
         *                 Trailing empty elements may be elided.
         * @param size the size of the sublist, must match the number of 1s in the bits array
         */
        private Sublist(List<T> list, BigInteger bits, int size) {
            this.list = list;
            this.bits = bits;
            assert size == bits.bitCount() : "Expected " + size + " bits, got " + bits.bitCount() + ".";
            this.size = size;
        }

        // NOTE: Some operations internally use the listIterator() instead of the iterator().
        // These operations are O(n) as long as the listIterator() implementation uses get()
        // and is not overridden with a more efficient implementation.

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public T get(int index) {
            if (index < 0 || index > size) return null;

            // NOTE: This is an O(n) operation.
            int realIndex = bits.getLowestSetBit();
            for (int i = 0; i < index; i++) {
                realIndex = bits.andNot(BigInteger.ONE.shiftLeft(realIndex + 1).subtract(BigInteger.ONE)).getLowestSetBit();
            }
            return this.list.get(realIndex);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Collection)) return false;

            Iterator<T> iterator1 = iterator();
            Iterator<?> iterator2 = ((Collection<?>)o).iterator();
            while (iterator1.hasNext() && iterator2.hasNext()) {
                T e1 = iterator1.next();
                Object e2 = iterator2.next();
                if (!Objects.equals(e1, e2)) return false;
            }
            return !(iterator1.hasNext() || iterator2.hasNext());
        }

        @Override
        public int hashCode() {
            int hashCode = 17;
            for (T e : this)
                hashCode = 31 * hashCode + (e != null ? e.hashCode() : 0);
            return hashCode;
        }

        @Override
        public Iterator<T> iterator() {
            return new SubsetIterator();
        }

        /**
         * Subset iterator.
         */
        private final class SubsetIterator implements Iterator<T> {
            int index;

            public SubsetIterator() {
                this.index = bits.getLowestSetBit();
            }

            @Override
            public boolean hasNext() {
                return this.index >= 0;
            }

            @Override
            public T next() {
                if (this.index < 0) throw new NoSuchElementException();
                T value = list.get(this.index);
                this.index = computeNextIndex(this.index);
                return value;
            }

            /**
             * Computes the next index of an element to return.
             *
             * @param currentIndex the current index, which must be greater than or equal to zero
             * @return the next index; or -1 when there are none
             */
            private int computeNextIndex(int currentIndex) {
                return bits.andNot(BigInteger.ONE.shiftLeft(currentIndex + 1).subtract(BigInteger.ONE)).getLowestSetBit();
            }
        }

    }

}
