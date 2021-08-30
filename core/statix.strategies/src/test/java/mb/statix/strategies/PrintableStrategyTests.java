package mb.statix.strategies;

import mb.statix.sequences.InterruptibleBiConsumer;
import mb.statix.sequences.InterruptibleBiFunction;
import mb.statix.sequences.InterruptibleBiPredicate;
import mb.statix.sequences.InterruptibleConsumer;
import mb.statix.sequences.InterruptibleFunction;
import mb.statix.sequences.InterruptiblePredicate;
import mb.statix.sequences.InterruptibleSupplier;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link PrintableStrategy} interface.
 */
public final class PrintableStrategyTests {

    @Test
    public void getName_shouldReturnAFormattedClassNameByDefault() {
        // Arrange
        final MyTest2 strategy = new MyTest2();

        // Act
        final String name = strategy.getName();

        // Assert
        assertEquals("my-test-2", name);
    }

    @Test
    public void getName_shouldRemoveStrategySuffixByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();

        // Act
        final String name = strategy.getName();

        // Assert
        assertEquals("my-test", name);
    }

    @Test
    public void getParamName_shouldReturnAParameterNameByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();

        // Act
        final String param0 = strategy.getParamName(0);
        final String param1 = strategy.getParamName(1);
        final String param2 = strategy.getParamName(2);

        // Assert
        assertEquals("a0", param0);
        assertEquals("a1", param1);
        assertEquals("a2", param2);
    }

    @Test
    public void isAnonymous_shouldReturnTrueByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();

        // Act
        final boolean anonymous = strategy.isAnonymous();

        // Assert
        assertTrue(anonymous);
    }

    @Test
    public void getPrecedence_shouldReturnIntegerMAX_VALUEByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();

        // Act
        final int precedence = strategy.getPrecedence();

        // Assert
        assertEquals(Integer.MAX_VALUE, precedence);
    }

    @Test
    public void isAtom_shouldReturnTrueByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();

        // Act
        final boolean anonymous = strategy.isAtom();

        // Assert
        assertTrue(anonymous);
    }

    @Test
    public void writeArg_shouldCallWriteToForAWritableByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();
        final StringBuilder sb = new StringBuilder();
        final AtomicInteger calls = new AtomicInteger();
        final Writable writable = new Writable() {
            @Override
            public StringBuilder writeTo(StringBuilder sb) {
                calls.getAndIncrement();
                sb.append("!! writable !!");
                return sb;
            }

            @Override
            public String toString() {
                return "!! tostring !!";
            }
        };

        // Act
        strategy.writeArg(sb, 0, writable);

        // Assert
        assertEquals("!! writable !!", sb.toString());
        assertEquals(1, calls.get());
    }

    @Test
    public void writeArg_shouldNotWriteFunctionsByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();
        final StringBuilder sb = new StringBuilder();

        final Predicate<Integer> predicate = it -> it % 2 == 0;
        final BiPredicate<String, Integer> bipredicate = (s, l) -> s.length() == l;
        final Function<String, Integer> function = String::length;
        final BiFunction<String, Integer, Integer> bifunction = (it, s) -> it.length() + s;
        final Consumer<Integer> consumer = it -> { };
        final BiConsumer<String, Integer> biconsumer = (s, it) -> { };
        final Supplier<Integer> supplier = () -> 42;

        // Act/Assert
        strategy.writeArg(sb, 0, predicate);
        assertEquals("<predicate>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, bipredicate);
        assertEquals("<bipredicate>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, function);
        assertEquals("<function>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, bifunction);
        assertEquals("<bifunction>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, consumer);
        assertEquals("<consumer>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, biconsumer);
        assertEquals("<biconsumer>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, supplier);
        assertEquals("<supplier>", sb.toString());
        sb.setLength(0);
    }

    @Test
    public void writeArg_shouldNotWriteInterruptibleFunctionsByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();
        final StringBuilder sb = new StringBuilder();

        final InterruptiblePredicate<Integer> predicate = it -> it % 2 == 0;
        final InterruptibleBiPredicate<String, Integer> bipredicate = (s, l) -> s.length() == l;
        final InterruptibleFunction<String, Integer> function = String::length;
        final InterruptibleBiFunction<String, Integer, Integer> bifunction = (it, s) -> it.length() + s;
        final InterruptibleConsumer<Integer> consumer = it -> { };
        final InterruptibleBiConsumer<String, Integer> biconsumer = (s, it) -> { };
        final InterruptibleSupplier<Integer> supplier = () -> 42;

        // Act/Assert
        strategy.writeArg(sb, 0, predicate);
        assertEquals("<predicate>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, bipredicate);
        assertEquals("<bipredicate>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, function);
        assertEquals("<function>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, bifunction);
        assertEquals("<bifunction>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, consumer);
        assertEquals("<consumer>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, biconsumer);
        assertEquals("<biconsumer>", sb.toString());
        sb.setLength(0);

        strategy.writeArg(sb, 0, supplier);
        assertEquals("<supplier>", sb.toString());
        sb.setLength(0);
    }

    @Test
    public void writeArg_shouldWriteClassNameByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();
        final StringBuilder sb = new StringBuilder();

        // Act
        strategy.writeArg(sb, 0, String.class);

        // Assert
        assertEquals("String", sb.toString());
    }

    @Test
    public void writeArg_shouldWriteStringQuotedAndEscapedByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();
        final StringBuilder sb = new StringBuilder();

        // Act
        strategy.writeArg(sb, 0, "On a\nnew line!");

        // Assert
        assertEquals("\"On a\\nnew line!\"", sb.toString());
    }

    @Test
    public void writeArg_shouldWriteStringRepresentationByDefault() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();
        final StringBuilder sb = new StringBuilder();

        // Act
        strategy.writeArg(sb, 0, new Object() {
            @Override
            public String toString() {
                return "!! tostring !!";
            }
        });

        // Assert
        assertEquals("!! tostring !!", sb.toString());
    }


    @Test
    public void writeLeft_shouldParenthesizeWhenStrategyHasLowerPrecedence() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return -1; } // Lower precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("x + y"); return sb; }
        };

        // Act
        final StringBuilder sb2 = PrintableStrategy.writeLeft(sb, strategy, 0, PrintableStrategy.Associativity.None);
        sb.append(" * z");

        // Assert
        assertSame(sb, sb2);
        assertEquals("(x + y) * z", sb.toString());
    }

    @Test
    public void writeLeft_shouldParenthesizeWhenStrategyHasEqualPrecedenceButRightAssociative() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return 0; } // Equal precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("x ?: y"); return sb; }
        };

        // Act
        final StringBuilder sb2 = PrintableStrategy.writeLeft(sb, strategy, 0, PrintableStrategy.Associativity.Right);
        sb.append(" ?: z");

        // Assert
        assertSame(sb, sb2);
        assertEquals("(x ?: y) ?: z", sb.toString());
    }

    @Test
    public void writeLeft_shouldNotParenthesizeWhenStrategyHasHigherPrecedence() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return 1; } // Higher precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("x * y"); return sb; }
        };

        // Act
        final StringBuilder sb2 = PrintableStrategy.writeLeft(sb, strategy, 0, PrintableStrategy.Associativity.Right);
        sb.append(" + z");

        // Assert
        assertSame(sb, sb2);
        assertEquals("x * y + z", sb.toString());
    }

    @Test
    public void writeLeft_shouldNotParenthesizeWhenStrategyHasEqualPrecedenceButNotRightAssociative() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return 0; } // Equal precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("x + y"); return sb; }
        };

        // Act
        final StringBuilder sb2 = PrintableStrategy.writeLeft(sb, strategy, 0, PrintableStrategy.Associativity.Left);
        sb.append(" + z");

        // Assert
        assertSame(sb, sb2);
        assertEquals("x + y + z", sb.toString());
    }

    @Test
    public void writeLeft_shouldNotParenthesizeWhenStrategyIsAtom() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return true; }
            @Override public int getArity() { return 0; }
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("x"); return sb; }
        };

        // Act
        final StringBuilder sb2 = PrintableStrategy.writeLeft(sb, strategy, 0, PrintableStrategy.Associativity.Left);
        sb.append(" + z");

        // Assert
        assertSame(sb, sb2);
        assertEquals("x + z", sb.toString());
    }


    @Test
    public void writeRight_shouldParenthesizeWhenStrategyHasLowerPrecedence() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return -1; } // Lower precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("y + z"); return sb; }
        };

        // Act
        sb.append("x * ");
        final StringBuilder sb2 = PrintableStrategy.writeRight(sb, strategy, 0, PrintableStrategy.Associativity.None);

        // Assert
        assertSame(sb, sb2);
        assertEquals("x * (y + z)", sb.toString());
    }

    @Test
    public void writeRight_shouldParenthesizeWhenStrategyHasEqualPrecedenceButLeftAssociative() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return 0; } // Equal precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("y ?: z"); return sb; }
        };

        // Act
        sb.append("x ?: ");
        final StringBuilder sb2 = PrintableStrategy.writeRight(sb, strategy, 0, PrintableStrategy.Associativity.Left);


        // Assert
        assertSame(sb, sb2);
        assertEquals("x ?: (y ?: z)", sb.toString());
    }

    @Test
    public void writeRight_shouldNotParenthesizeWhenStrategyHasHigherPrecedence() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return 1; } // Higher precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("y * z"); return sb; }
        };

        // Act
        sb.append("x + ");
        final StringBuilder sb2 = PrintableStrategy.writeRight(sb, strategy, 0, PrintableStrategy.Associativity.Left);

        // Assert
        assertSame(sb, sb2);
        assertEquals("x + y * z", sb.toString());
    }

    @Test
    public void writeRight_shouldNotParenthesizeWhenStrategyHasEqualPrecedenceButNotLeftAssociative() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return 0; } // Equal precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("y + z"); return sb; }
        };

        // Act
        sb.append("x + ");
        final StringBuilder sb2 = PrintableStrategy.writeRight(sb, strategy, 0, PrintableStrategy.Associativity.Right);

        // Assert
        assertSame(sb, sb2);
        assertEquals("x + y + z", sb.toString());
    }

    @Test
    public void writeRight_shouldNotParenthesizeWhenStrategyIsAtom() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return true; }
            @Override public int getArity() { return 0; }
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("z"); return sb; }
        };

        // Act
        sb.append("x + ");
        final StringBuilder sb2 = PrintableStrategy.writeRight(sb, strategy, 0, PrintableStrategy.Associativity.Right);

        // Assert
        assertSame(sb, sb2);
        assertEquals("x + z", sb.toString());
    }


    @Test
    public void writeMiddle_shouldParenthesizeWhenStrategyHasLowerPrecedence() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return -1; } // Lower precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("a < b + c"); return sb; }
        };

        // Act
        sb.append("x < ");
        final StringBuilder sb2 = PrintableStrategy.writeMiddle(sb, strategy, 0);
        sb.append(" + z");

        // Assert
        assertSame(sb, sb2);
        assertEquals("x < (a < b + c) + z", sb.toString());
    }

    @Test
    public void writeMiddle_shouldNotParenthesizeWhenStrategyHasEqualPrecedence() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return 1; } // Higher precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("a ? b : c"); return sb; }
        };

        // Act
        sb.append("x ? ");
        final StringBuilder sb2 = PrintableStrategy.writeMiddle(sb, strategy, 0);
        sb.append(" : z");

        // Assert
        assertSame(sb, sb2);
        assertEquals("x ? a ? b : c : z", sb.toString());
    }

    @Test
    public void writeMiddle_shouldNotParenthesizeWhenStrategyHasHigherPrecedence() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return false; }
            @Override public int getArity() { return 0; }
            @Override public int getPrecedence() { return 1; } // Higher precedence
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("a * b"); return sb; }
        };

        // Act
        sb.append("x ? ");
        final StringBuilder sb2 = PrintableStrategy.writeMiddle(sb, strategy, 0);
        sb.append(" : z");

        // Assert
        assertSame(sb, sb2);
        assertEquals("x ? a * b : z", sb.toString());
    }

    @Test
    public void writeMiddle_shouldNotParenthesizeWhenStrategyIsAtom() {
        // Arrange
        final StringBuilder sb = new StringBuilder();
        final PrintableStrategy strategy = new PrintableStrategy() {
            @Override public boolean isAtom() { return true; }
            @Override public int getArity() { return 0; }
            @Override public StringBuilder writeTo(StringBuilder sb) { sb.append("y"); return sb; }
        };

        // Act
        sb.append("x ? ");
        final StringBuilder sb2 = PrintableStrategy.writeMiddle(sb, strategy, 0);
        sb.append(" : z");

        // Assert
        assertSame(sb, sb2);
        assertEquals("x ? y : z", sb.toString());
    }

    private static class MyTestStrategy implements PrintableStrategy {
        @Override public int getArity() { return 3; }
    }
    private static class MyTest2 implements PrintableStrategy {
        @Override public int getArity() { return 3; }
    }

}
