package mb.statix.strategies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link Strategy1} interface.
 */
@SuppressWarnings("CodeBlock2Expr")
public final class Strategy1Tests {
    @Test
    public void getArity_shouldReturn1() {
        // Arrange
        final Strategy1<Object, String, String, String> strategy = new MyTestStrategy1();

        // Act
        final int arity = strategy.getArity();

        // Assert
        assertEquals(1, arity);
    }

    @Test
    public void apply_getName_shouldReturnNameOfOriginalStrategy() {
        // Arrange
        final Strategy1<Object, String, String, String> strategy = new MyTestStrategy1();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ");
        final String name = appliedStrategy.getName();

        // Assert
        assertEquals("my-test-strategy-1", name);
    }

    @Test
    public void apply_getParamName_shouldCallGetParamNameOfOriginalStrategy() {
        // Arrange
        final Strategy1<Object, String, String, String> strategy = new MyTestStrategy1();

        // Act/Assert
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ");
        assertThrows(IndexOutOfBoundsException.class, () -> {
            appliedStrategy.getParamName(0);
        });
    }

    @Test
    public void apply_writeArg_shouldCallWriteArgOfOriginalStrategy() {
        // Arrange
        final Strategy1<Object, String, String, String> strategy = new MyTestStrategy1();
        final StringBuilder sb = new StringBuilder();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ");
        appliedStrategy.writeArg(sb, 0, "xyz");

        // Assert
        assertEquals("1: \"xyz\"", sb.toString());
    }

    @Test
    public void apply_isAnonymous_shouldReturnIsAnonymousOfOriginalStrategy() {
        // Arrange
        final Strategy1<Object, String, String, String> strategy = new MyTestStrategy1();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ");
        final boolean anonymous = appliedStrategy.isAnonymous();

        // Assert
        assertFalse(anonymous);
    }

    @Test
    public void apply_isAtom_shouldReturnTrue() {
        // Arrange
        final Strategy1<Object, String, String, String> strategy = new MyTestStrategy1();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ");
        final boolean atom = appliedStrategy.isAtom();

        // Assert
        assertTrue(atom);
    }

    @Test
    public void apply_getPrecedence_shouldCallGetPrecedenceOfOriginalStrategy() {
        // Arrange
        final Strategy1<Object, String, String, String> strategy = new MyTestStrategy1();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ");
        final int precedence = appliedStrategy.getPrecedence();

        // Assert
        assertEquals(strategy.getPrecedence(), precedence);
    }

    @Test
    public void apply_writeTo_shouldWriteStrategyNameAndArguments() {
        // Arrange
        final Strategy1<Object, String, String, String> strategy = new MyTestStrategy1();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ");
        final String str = appliedStrategy.writeTo(new StringBuilder()).toString();

        // Assert
        assertEquals("my-test-strategy-1(0: \"Hello, \")", str);
    }

    @Test
    public void apply_eval_shouldImplicitlyApplyArguments() throws InterruptedException {
        // Arrange
        final Strategy1<Object, String, String, String> strategy = new MyTestStrategy1();

        // Act
        final Strategy<Object, String, String> appliedStrategy = strategy.apply("Hello, ");
        final String result = appliedStrategy.eval(new Object(), "World").single();

        // Assert
        assertEquals("Hello, World", result);
    }

}
