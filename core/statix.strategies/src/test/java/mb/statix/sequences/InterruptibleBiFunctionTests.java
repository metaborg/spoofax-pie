package mb.statix.sequences;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link InterruptibleBiFunction} interface.
 */
public final class InterruptibleBiFunctionTests {

    @Test
    public void andThen_shouldApplySecondFunctionAfterFirst() throws InterruptedException {
        // Arrange
        final InterruptibleBiFunction<String, Integer, Integer> first = (s, i) -> s.length() + i;
        final InterruptibleFunction<Integer, String> second = i -> Integer.toString(i);

        // Act
        final InterruptibleBiFunction<String, Integer, String> combination = first.andThen(second);
        final String result = combination.apply("aa", 3);

        // Assert
        assertEquals("5", result);
    }

}
