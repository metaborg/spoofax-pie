package mb.tego.sequences;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link Seq} class.
 */
@SuppressWarnings("Convert2MethodRef")
public final class SeqTests {

    @Test
    public void of_shouldReturnEmptySequence_whenGivenNoArguments() throws InterruptedException {
        // Act
        final Seq<Integer> seq = Seq.of();

        // Assert
        assertFalse(seq.next());
        assertThrows(Exception.class, () -> seq.getCurrent(), "Undefined behavior");
    }

    @Test
    public void of_shouldReturnSequence_whenGivenArguments() throws InterruptedException {
        // Assert
        final int a = 10;
        final int b = 20;
        final int c = 30;

        // Act
        final Seq<Integer> seq = Seq.of(a, b, c);

        // Assert
        assertThrows(Exception.class, () -> seq.getCurrent(), "Undefined behavior");
        assertTrue(seq.next());
        assertEquals(a, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(b, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(c, seq.getCurrent());
        assertFalse(seq.next());
        assertThrows(Exception.class, () -> seq.getCurrent(), "Undefined behavior");
    }

    @Test
    public void of_shouldReturnEmptySequence_whenGivenEmptyArray() throws InterruptedException {
        // Act
        final Seq<Integer> seq = Seq.of(new Integer[0]);

        // Assert
        assertFalse(seq.next());
    }@Test
    public void from_shouldWrapSupplier() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = counter::getAndIncrement;

        // Act
        final Seq<Integer> seq = Seq.fromRepeat(supplier);

        // Assert
        assertTrue(seq.next());
        assertEquals(0, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(1, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(2, seq.getCurrent());
        // ... Infinite sequence
    }

    @Test
    public void from_shouldFinishAndThrow_whenSupplierThrows() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = () -> {
            if (counter.get() > 2) throw new IllegalStateException("Exception thrown");
            return counter.getAndIncrement();
        };

        // Act
        final Seq<Integer> seq = Seq.fromRepeat(supplier);

        // Assert
        assertTrue(seq.next());
        assertEquals(0, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(1, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(2, seq.getCurrent());
        assertThrows(IllegalStateException.class, () -> seq.next());
    }

    @Test
    public void from_shouldFinish_whenSupplierThrowsNoSuchElementException() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = () -> {
            if (counter.get() > 2) throw new NoSuchElementException("Done!");
            return counter.getAndIncrement();
        };

        // Act
        final Seq<Integer> seq = Seq.fromRepeat(supplier);

        // Assert
        assertTrue(seq.next());
        assertEquals(0, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(1, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(2, seq.getCurrent());
        assertFalse(seq.next());
    }

    @Test
    public void from_shouldCloseSupplier_whenSupplierIsAutoCloseable() throws Exception {
        // Arrange
        final AtomicBoolean closed = new AtomicBoolean();
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = new CloseableSupplier<Integer>() {
            @Override
            public Integer get() {
                return counter.getAndIncrement();
            }

            @Override
            public void close() {
                final boolean nowClosed = closed.compareAndSet(false, true);
                if (!nowClosed) throw new IllegalStateException("Already closed");
            }
        };

        // Act
        final Seq<Integer> seq = Seq.fromRepeat(supplier);

        // Assert
        assertFalse(closed.get());
        seq.close();
        assertTrue(closed.get());
    }

    @Test
    public void fromOnce_shouldWrapSupplier() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = counter::getAndIncrement;

        // Act
        final Seq<Integer> seq = Seq.fromOnce(supplier);

        // Assert
        assertTrue(seq.next());
        assertEquals(0, seq.getCurrent());
        assertFalse(seq.next());
    }

    @Test
    public void fromOnce_shouldFinishAndThrow_whenSupplierThrows() {
        // Arrange
        final InterruptibleSupplier<Integer> supplier = () -> {
            throw new IllegalStateException("Exception thrown");
        };

        // Act
        final Seq<Integer> seq = Seq.fromOnce(supplier);

        // Assert
        assertThrows(IllegalStateException.class, () -> seq.next());
    }

    @Test
    public void fromOnce_shouldFinish_whenSupplierThrowsNoSuchElementException() throws InterruptedException {
        // Arrange
        final InterruptibleSupplier<Integer> supplier = () -> {
            throw new NoSuchElementException("Done!");
        };

        // Act
        final Seq<Integer> seq = Seq.fromOnce(supplier);

        // Assert
        assertFalse(seq.next());
    }

    @Test
    public void fromOnce_shouldCloseSupplier_whenSupplierIsAutoCloseable() throws Exception {
        // Arrange
        final AtomicBoolean closed = new AtomicBoolean();
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = new CloseableSupplier<Integer>() {
            @Override
            public Integer get() {
                return counter.getAndIncrement();
            }

            @Override
            public void close() {
                final boolean nowClosed = closed.compareAndSet(false, true);
                if (!nowClosed) throw new IllegalStateException("Already closed");
            }
        };

        // Act
        final Seq<Integer> seq = Seq.fromOnce(supplier);

        // Assert
        assertFalse(closed.get());
        seq.close();
        assertTrue(closed.get());
    }

    @Test
    public void from_shouldReturnIteratorValues_whenWrappingIterable() throws InterruptedException {
        // Arrange
        final Iterable<Integer> iterable = Arrays.asList(0, 1, 2);

        // Act
        final Seq<Integer> seq = Seq.fromIterable(iterable);

        // Assert
        assertTrue(seq.next());
        assertEquals(0, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(1, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(2, seq.getCurrent());
        assertFalse(seq.next());
    }

    @Test
    public void from_shouldNotInstantiateIterator_untilCalledForTheFirstTime() throws InterruptedException {
        // Arrange
        final List<Integer> list = Arrays.asList(0, 1, 2);
        final AtomicBoolean iteratorCalled = new AtomicBoolean(false);
        final Iterable<Integer> iterable = () -> {
            iteratorCalled.set(true);
            return list.iterator();
        };

        // Act
        final Seq<Integer> seq = Seq.fromIterable(iterable);

        // Assert
        assertFalse(iteratorCalled.get());
        assertTrue(seq.next());
        assertEquals(0, seq.getCurrent());
        assertTrue(iteratorCalled.get());
    }

    @Test
    public void asSeq_shouldReturnIteratorValues_whenWrappingIterator() throws InterruptedException {
        // Arrange
        final Iterator<Integer> iterator = Arrays.asList(0, 1, 2).listIterator();

        // Act
        final Seq<Integer> seq = Seq.asSeq(iterator);

        // Assert
        assertTrue(seq.next());
        assertEquals(0, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(1, seq.getCurrent());
        assertTrue(seq.next());
        assertEquals(2, seq.getCurrent());
        assertFalse(seq.next());
    }

    @Test
    public void asSeq_shouldReturnOriginalSequence_whenWrappingIteratorWrappingSequence() {
        // Arrange
        final Seq<Integer> seq = Seq.of(0, 1, 2);
        final Iterator<Integer> iterator = Seq.asIterator(seq);

        // Act
        final Seq<Integer> newSeq = Seq.asSeq(iterator);

        // Assert
        assertSame(seq, newSeq);
    }

    @Test
    public void asIterator_shouldReturnSequenceValues_whenWrappingSequence() {
        // Arrange
        final Seq<Integer> seq = Seq.of(0, 1, 2);

        // Act
        final Iterator<Integer> iterator = Seq.asIterator(seq);

        // Assert
        assertTrue(iterator.hasNext());
        assertEquals(0, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(2, iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> iterator.next());
    }


    @Test
    public void asIterator_shouldReturnOriginalIterator_whenWrappingSequenceWrappingIterator() {
        // Arrange
        final Iterator<Integer> iterator = Arrays.asList(0, 1, 2).listIterator();
        final Seq<Integer> seq = Seq.asSeq(iterator);

        // Act
        final Iterator<Integer> newIterator = Seq.asIterator(seq);

        // Assert
        assertSame(iterator, newIterator);
    }

    @Test
    public void asIterator_shouldCloseSequence() throws Exception {
        // Arrange
        final AtomicBoolean closed = new AtomicBoolean();
        final Seq<Integer> seq = new SeqBase<Integer>() {
            @Override
            protected void computeNext() throws InterruptedException {
                yieldBreak();
            }

            @Override
            public void close() throws Exception {
                final boolean nowClosed = closed.compareAndSet(false, true);
                if (!nowClosed) throw new IllegalStateException("Already closed");
            }
        };

        // Act
        final Iterator<Integer> iterator = Seq.asIterator(seq);

        // Assert
        assertFalse(closed.get());
        ((AutoCloseable)iterator).close();
        assertTrue(closed.get());
    }

    @Test
    public void collect_shouldCallCollectorOnEachElement_whenCollectingIntoList() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of(1, 2, 3);

        // Act
        final List<Integer> result = seq.collect(Collectors.toList());

        // Assert
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    @Test
    public void filter_shouldOnlyReturnElementsThatMatchThePredicate() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = counter::getAndIncrement;
        final Seq<Integer> seq = Seq.fromRepeat(supplier);

        // Act
        final Seq<Integer> filteredSeq = seq.filter(i -> i % 2 == 0);

        // Assert
        assertTrue(filteredSeq.next());
        assertEquals(0, filteredSeq.getCurrent());
        assertTrue(filteredSeq.next());
        assertEquals(2, filteredSeq.getCurrent());
        assertTrue(filteredSeq.next());
        assertEquals(4, filteredSeq.getCurrent());
        assertTrue(filteredSeq.next());
        assertEquals(6, filteredSeq.getCurrent());
        // ... Infinite sequence
    }

    @Test
    public void filterIsInstance_shouldOnlyReturnElementsThatMatchTheType() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = counter::getAndIncrement;
        final Seq<Object> seq = Seq.fromRepeat(() -> {
            final int value = counter.getAndIncrement();
            if (value % 2 == 0) {
                // Even values are returned as Integer
                return value;
            } else {
                // Odd values are returned as String
                return Integer.toString(value);
            }
        });

        // Act
        final Seq<Integer> filteredSeq = seq.filterIsInstance(Integer.class);

        // Assert
        assertTrue(filteredSeq.next());
        assertEquals(0, filteredSeq.getCurrent());
        assertTrue(filteredSeq.next());
        assertEquals(2, filteredSeq.getCurrent());
        assertTrue(filteredSeq.next());
        assertEquals(4, filteredSeq.getCurrent());
        assertTrue(filteredSeq.next());
        assertEquals(6, filteredSeq.getCurrent());
        // ... Infinite sequence
    }

    @Test
    public void forEach_shouldApplyActionToEachElement() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of(0, 1, 2);
        final List<Integer> results = new ArrayList<>();

        // Act
        seq.forEach(e -> results.add(e));

        // Assert
        assertEquals(Arrays.asList(0, 1, 2), results);
    }

    @Test
    public void map_shouldTransformEachElement() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = counter::getAndIncrement;
        final Seq<Integer> seq = Seq.fromRepeat(supplier);

        // Act
        final Seq<String> mappedSeq = seq.map(i -> Integer.toString(i));

        // Assert
        assertTrue(mappedSeq.next());
        assertEquals("0", mappedSeq.getCurrent());
        assertTrue(mappedSeq.next());
        assertEquals("1", mappedSeq.getCurrent());
        assertTrue(mappedSeq.next());
        assertEquals("2", mappedSeq.getCurrent());
        assertTrue(mappedSeq.next());
        assertEquals("3", mappedSeq.getCurrent());
        // ... Infinite sequence
    }

    @Test
    public void limit_shouldReturnOnlyTheSpecifiedNumberOfElements_whenTheSequenceIsLongerThanTheLimit() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = counter::getAndIncrement;
        final Seq<Integer> seq = Seq.fromRepeat(supplier);

        // Act
        final Seq<Integer> limitedSeq = seq.limit(3);

        // Assert
        assertTrue(limitedSeq.next());
        assertEquals(0, limitedSeq.getCurrent());
        assertTrue(limitedSeq.next());
        assertEquals(1, limitedSeq.getCurrent());
        assertTrue(limitedSeq.next());
        assertEquals(2, limitedSeq.getCurrent());
        assertFalse(limitedSeq.next());
    }

    @Test
    public void limit_shouldReturnAllElements_whenTheSequenceIsShorterThanTheLimit() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of(0, 1);

        // Act
        final Seq<Integer> limitedSeq = seq.limit(3);

        // Assert
        assertTrue(limitedSeq.next());
        assertEquals(0, limitedSeq.getCurrent());
        assertTrue(limitedSeq.next());
        assertEquals(1, limitedSeq.getCurrent());
        assertFalse(limitedSeq.next());
    }

    @Test
    public void limit_shouldReturnNothing_whenTheSequenceIsEmpty() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of();

        // Act
        final Seq<Integer> limitedSeq = seq.limit(3);

        // Assert
        assertFalse(limitedSeq.next());
    }

    @Test
    public void limit_shouldReturnNothing_whenTheLimitIsZero() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = counter::getAndIncrement;
        final Seq<Integer> seq = Seq.fromRepeat(supplier);

        // Act
        final Seq<Integer> limitedSeq = seq.limit(0);

        // Assert
        assertFalse(limitedSeq.next());
    }

    @Test
    public void single_shouldReturnTheLastElement_whenTheSequenceHasOnlyOneElementRemaining() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of(0, 1);
        seq.next(); // skip '0'

        // Act
        final Integer result = seq.single();

        // Assert
        assertEquals(1, result);
    }

    @Test
    public void single_shouldThrow_whenTheSequenceIsEmpty() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of(0);
        seq.next(); // skip '0'

        // Act/Assert
        assertThrows(NoSuchElementException.class, () -> {
            seq.single();
        });
    }

    @Test
    public void single_shouldThrow_whenTheSequenceHasMoreThanOneElementRemaining() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of(0, 1, 2);
        seq.next(); // skip '0'

        // Act/Assert
        assertThrows(IllegalStateException.class, () -> {
            seq.single();
        });
    }

    @Test
    public void peekable_shouldReturnPeekableSequence() throws InterruptedException {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final InterruptibleSupplier<Integer> supplier = counter::getAndIncrement;
        final Seq<Integer> seq = Seq.fromRepeat(supplier);

        // Act
        final PeekableSeq<Integer> peekableSeq = seq.peekable();

        // Assert
        assertTrue(peekableSeq.peek());
        assertTrue(peekableSeq.next());
        assertEquals(0, peekableSeq.getCurrent());
        assertTrue(peekableSeq.peek());
        assertTrue(peekableSeq.next());
        assertEquals(1, peekableSeq.getCurrent());
        assertTrue(peekableSeq.peek());
        assertTrue(peekableSeq.next());
        assertEquals(2, peekableSeq.getCurrent());
        assertTrue(peekableSeq.peek());
        assertTrue(peekableSeq.next());
        // ... Infinite sequence
    }

    @Test
    public void peekable_shouldPeekFalse_whenNoMoreElements() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of(0, 1, 2);

        // Act
        final PeekableSeq<Integer> peekableSeq = seq.peekable();

        // Assert
        assertTrue(peekableSeq.peek());
        assertTrue(peekableSeq.next());
        assertEquals(0, peekableSeq.getCurrent());
        assertTrue(peekableSeq.peek());
        assertTrue(peekableSeq.next());
        assertEquals(1, peekableSeq.getCurrent());
        assertTrue(peekableSeq.peek());
        assertTrue(peekableSeq.next());
        assertEquals(2, peekableSeq.getCurrent());
        assertFalse(peekableSeq.peek());
        assertFalse(peekableSeq.next());
    }

    @Test
    public void peekable_shouldPeekFalse_whenSequenceEmpty() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of();

        // Act
        final PeekableSeq<Integer> peekableSeq = seq.peekable();

        // Assert
        assertFalse(peekableSeq.peek());
        assertFalse(peekableSeq.next());
    }

    @Test
    public void peekable_shouldNotAdvanceToNextElement_whenPeeking() throws InterruptedException {
        // Arrange
        final Seq<Integer> seq = Seq.of(0, 1, 2);

        // Act
        final PeekableSeq<Integer> peekableSeq = seq.peekable();

        // Assert
        assertTrue(peekableSeq.peek());
        assertTrue(peekableSeq.next());
        assertEquals(0, peekableSeq.getCurrent());
        assertTrue(peekableSeq.peek());
        assertEquals(0, peekableSeq.getCurrent());
        assertTrue(peekableSeq.next());
        assertEquals(1, peekableSeq.getCurrent());
        assertTrue(peekableSeq.peek());
        assertEquals(1, peekableSeq.getCurrent());
        assertTrue(peekableSeq.next());
        assertEquals(2, peekableSeq.getCurrent());
        assertFalse(peekableSeq.peek());
        assertEquals(2, peekableSeq.getCurrent());
        assertFalse(peekableSeq.next());
    }

    private interface CloseableSupplier<T> extends InterruptibleSupplier<T>, AutoCloseable { }

}
