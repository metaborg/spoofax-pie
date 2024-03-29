package mb.tego.sequences;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link InterruptibleFunction} interface.
 */
public final class InterruptibleFunctionTests {

    @SuppressWarnings("Convert2MethodRef") @Test
    public void andThen_shouldApplySecondFunctionAfterFirst() throws InterruptedException {
        // Arrange
        final InterruptibleFunction<String, Integer> first = s -> s.length();
        final InterruptibleFunction<Integer, String> second = i -> Integer.toString(i);

        // Act
        final InterruptibleFunction<String, String> combination = first.andThen(second);
        final String result = combination.apply("aa");

        // Assert
        assertEquals("2", result);
    }

    @SuppressWarnings("Convert2MethodRef") @Test
    public void compose_shouldApplySecondFunctionBeforeFirst() throws InterruptedException {
        // Arrange
        final InterruptibleFunction<Integer, String> first = i -> Integer.toString(i);
        final InterruptibleFunction<String, Integer> second = s -> s.length();

        // Act
        final InterruptibleFunction<String, String> combination = first.compose(second);
        final String result = combination.apply("aa");

        // Assert
        assertEquals("2", result);
    }

}
