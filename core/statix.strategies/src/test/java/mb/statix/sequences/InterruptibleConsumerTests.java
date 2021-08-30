package mb.statix.sequences;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link InterruptibleConsumer} interface.
 */
public final class InterruptibleConsumerTests {

    @Test
    public void andThen_shouldApplySecondFunctionAfterFirst() throws InterruptedException {
        // Arrange
        final AtomicInteger i = new AtomicInteger(1);
        final InterruptibleConsumer<Integer> first = j -> i.set(i.get() + j);
        final InterruptibleConsumer<Integer> second = j -> i.set(i.get() * j);

        // Act
        final InterruptibleConsumer<Integer> combination = first.andThen(second);
        combination.accept(3);

        // Assert
        assertEquals(12, i.get());
    }

}
