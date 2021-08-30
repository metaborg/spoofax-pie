package mb.statix.sequences;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link InterruptibleBiConsumer} interface.
 */
public final class InterruptibleBiConsumerTests {

    @Test
    public void andThen_shouldApplySecondFunctionAfterFirst() throws InterruptedException {
        // Arrange
        final AtomicInteger i = new AtomicInteger(1);
        final InterruptibleBiConsumer<String, Integer> first = (a, b) -> i.set(i.get() + b);
        final InterruptibleBiConsumer<String, Integer> second = (a, b) -> i.set(i.get() * b);

        // Act
        final InterruptibleBiConsumer<String, Integer> combination = first.andThen(second);
        combination.accept("foo", 3);

        // Assert
        assertEquals(12, i.get());
    }

}
