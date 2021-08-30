package mb.statix.strategies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link Strategy2} interface.
 */
@SuppressWarnings("CodeBlock2Expr")
public final class Strategy2Tests {
    @Test
    public void getArity_shouldReturn2() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final int arity = strategy.getArity();

        // Assert
        assertEquals(2, arity);
    }

    @Test
    public void apply1_getName_shouldReturnNameOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy1<Object, String, String, String> appliedStrategy = strategy.apply("Hello, ");
        final String name = appliedStrategy.getName();

        // Assert
        assertEquals("my-test-strategy-2", name);
    }

    @Test
    public void apply1_getParamName_shouldCallGetParamNameOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act/Assert
        final Strategy1<Object, String, String, String> appliedStrategy = strategy.apply("Hello, ");
        final String param0 = appliedStrategy.getParamName(0);
        assertThrows(IndexOutOfBoundsException.class, () -> {
            appliedStrategy.getParamName(1);
        });

        // Assert
        assertEquals("part2", param0);
    }

    @Test
    public void apply1_writeArg_shouldCallWriteArgOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();
        final StringBuilder sb = new StringBuilder();

        // Act
        final Strategy1<Object, String, String, String> appliedStrategy = strategy.apply("Hello, ");
        appliedStrategy.writeArg(sb, 0, "xyz");

        // Assert
        assertEquals("1: \"xyz\"", sb.toString());
    }

    @Test
    public void apply1_isAnonymous_shouldReturnIsAnonymousOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy1<Object, String, String, String> appliedStrategy = strategy.apply("Hello, ");
        final boolean anonymous = appliedStrategy.isAnonymous();

        // Assert
        assertFalse(anonymous);
    }

    @Test
    public void apply1_isAtom_shouldReturnTrue() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy1<Object, String, String, String> appliedStrategy = strategy.apply("Hello, ");
        final boolean atom = appliedStrategy.isAtom();

        // Assert
        assertTrue(atom);
    }

    @Test
    public void apply1_getPrecedence_shouldCallGetPrecedenceOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy1<Object, String, String, String> appliedStrategy = strategy.apply("Hello, ");
        final int precedence = appliedStrategy.getPrecedence();

        // Assert
        assertEquals(strategy.getPrecedence(), precedence);
    }

    @Test
    public void apply1_apply1_shouldBeEquivalentToApply2() throws InterruptedException {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy1<Object, String, String, String> applied1Strategy = strategy.apply("Hello, ");
        final Strategy<Object, String, String> applied2Strategy = applied1Strategy.apply("corona ");
        final String result = applied2Strategy.eval(new Object(), "World").single();

        // Assert
        assertEquals("Hello, corona World", result);
    }

    @Test
    public void apply1_writeTo_shouldWriteStrategyNameAndArguments() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy1<Object, String, String, String> appliedStrategy = strategy.apply("Hello, ");
        final String str = appliedStrategy.writeTo(new StringBuilder()).toString();

        // Assert
        assertEquals("my-test-strategy-2(0: \"Hello, \", ..)", str);
    }

    @Test
    public void apply1_eval_shouldImplicitlyApplyArguments() throws InterruptedException {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy1<Object, String, String, String> appliedStrategy = strategy.apply("Hello, ");
        final String result = appliedStrategy.eval(new Object(), "cruel ", "World").single();

        // Assert
        assertEquals("Hello, cruel World", result);
    }



    @Test
    public void apply2_getName_shouldReturnNameOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ", "beautiful ");
        final String name = appliedStrategy.getName();

        // Assert
        assertEquals("my-test-strategy-2", name);
    }

    @Test
    public void apply2_getParamName_shouldCallGetParamNameOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act/Assert
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ", "beautiful ");
        assertThrows(IndexOutOfBoundsException.class, () -> {
            appliedStrategy.getParamName(0);
        });
    }

    @Test
    public void apply2_writeArg_shouldCallWriteArgOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();
        final StringBuilder sb = new StringBuilder();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ", "beautiful ");
        appliedStrategy.writeArg(sb, 0, "xyz");

        // Assert
        assertEquals("2: \"xyz\"", sb.toString());
    }

    @Test
    public void apply2_isAnonymous_shouldReturnIsAnonymousOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ", "beautiful ");
        final boolean anonymous = appliedStrategy.isAnonymous();

        // Assert
        assertFalse(anonymous);
    }

    @Test
    public void apply2_isAtom_shouldReturnTrue() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ", "beautiful ");
        final boolean atom = appliedStrategy.isAtom();

        // Assert
        assertTrue(atom);
    }

    @Test
    public void apply2_getPrecedence_shouldCallGetPrecedenceOfOriginalStrategy() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ", "beautiful ");
        final int precedence = appliedStrategy.getPrecedence();

        // Assert
        assertEquals(strategy.getPrecedence(), precedence);
    }

    @Test
    public void apply2_writeTo_shouldWriteStrategyNameAndArguments() {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ", "beautiful ");
        final String str = appliedStrategy.writeTo(new StringBuilder()).toString();

        // Assert
        assertEquals("my-test-strategy-2(0: \"Hello, \", 1: \"beautiful \")", str);
    }

    @Test
    public void apply2_eval_shouldImplicitlyApplyArguments() throws InterruptedException {
        // Arrange
        final Strategy2<Object, String, String, String, String> strategy = new MyTestStrategy2();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ", "beautiful ");
        final String result = appliedStrategy.eval(new Object(), "World").single();

        // Assert
        assertEquals("Hello, beautiful World", result);
    }

}
