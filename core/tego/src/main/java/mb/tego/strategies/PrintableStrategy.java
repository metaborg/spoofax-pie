package mb.tego.strategies;

import mb.tego.sequences.InterruptibleBiConsumer;
import mb.tego.sequences.InterruptibleBiFunction;
import mb.tego.sequences.InterruptibleBiPredicate;
import mb.tego.sequences.InterruptibleConsumer;
import mb.tego.sequences.InterruptibleFunction;
import mb.tego.sequences.InterruptiblePredicate;
import mb.tego.sequences.InterruptibleSupplier;
import mb.tego.utils.CaseFormat;
import mb.tego.utils.StringUtils;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A pretty-printable strategy.
 */
public interface PrintableStrategy extends StrategyDecl, Writable {

    /**
     * Gets the arity of the strategy.
     *
     * The arity of a basic strategy {@code T -> R} is 0.
     *
     * @return the arity of the strategy, excluding the input argument
     */
    int getArity();

    /**
     * Gets the name of the parameter.
     *
     * @param index the zero-based index
     * @return the parameter name
     */
    default String getParamName(int index) {
        if (index < 0 || index >= getArity())
            throw new IndexOutOfBoundsException("Parameter index " + index + " out of range for " + getName() + "`" + getArity());
        return "a" + index;
    }

    /**
     * Gets the precedence of this strategy relative to other strategies.
     *
     * @return the precedence, higher means higher precedence
     */
    default int getPrecedence() { return Integer.MAX_VALUE; }

    /**
     * Gets whether this is an atom strategy.
     *
     * @return {@code true} when this is an atom strategy;
     * otherwise, {@code false} when this is a binary strategy
     */
    default boolean isAtom() { return true; }

    /**
     * Writes the specified argument to the specified {@link StringBuilder}.
     *
     * @param sb the {@link StringBuilder} to write to
     * @param index the one-based index of the argument to write
     * @param arg the argument to write
     */
    default void writeArg(StringBuilder sb, int index, Object arg) {
        if (arg instanceof Writable) {
            ((Writable)arg).writeTo(sb);
        } else if (arg instanceof Predicate || arg instanceof InterruptiblePredicate) {
            sb.append("<predicate>");
        } else if (arg instanceof BiPredicate || arg instanceof InterruptibleBiPredicate) {
            sb.append("<bipredicate>");
        } else if (arg instanceof Function || arg instanceof InterruptibleFunction) {
            sb.append("<function>");
        } else if (arg instanceof BiFunction || arg instanceof InterruptibleBiFunction) {
            sb.append("<bifunction>");
        } else if (arg instanceof Consumer || arg instanceof InterruptibleConsumer) {
            sb.append("<consumer>");
        } else if (arg instanceof BiConsumer || arg instanceof InterruptibleBiConsumer) {
            sb.append("<biconsumer>");
        } else if (arg instanceof Supplier || arg instanceof InterruptibleSupplier) {
            sb.append("<supplier>");
        } else if (arg instanceof CharSequence) {
            sb.append('"').append(StringUtils.escapeJava((CharSequence)arg)).append('"');
        } else if (arg instanceof Class) {
            sb.append(((Class<?>)arg).getSimpleName());
        } else {
            //noinspection UnnecessaryToStringCall
            sb.append(arg.toString());
        }
    }

    /**
     * Specifies the associativity of a strategy.
     */
    enum Associativity {
        /** Not associative. */
        None,
        /** Left associative. */
        Left,
        /** Right associative. */
        Right
    }

    /**
     * Writes a left strategy expression argument to the specified buffer, optionally wrapping it in parentheses.
     *
     * @param buffer the buffer to write to
     * @param strategy the strategy to write
     * @param precedence the precedence of the parent strategy; a higher value indicates a higher precedence
     * @param associativity the associativity of the parent strategy
     * @return the buffer
     */
    static StringBuilder writeLeft(StringBuilder buffer, PrintableStrategy strategy, int precedence, Associativity associativity) {
        boolean parenthesize = !strategy.isAtom() && (strategy.getPrecedence() < precedence
            || (strategy.getPrecedence() == precedence && associativity == Associativity.Right));
        if (parenthesize) buffer.append('(');
        strategy.writeTo(buffer);
        if (parenthesize) buffer.append(')');
        return buffer;
    }

    /**
     * Writes a middle (or only) strategy expression argument to the specified buffer, optionally wrapping it in parentheses.
     *
     * @param buffer the buffer to write to
     * @param strategy the strategy to write
     * @param precedence the precedence of the parent strategy; a higher value indicates a higher precedence
     * @return the buffer
     */
    static StringBuilder writeMiddle(StringBuilder buffer, PrintableStrategy strategy, int precedence) {
        boolean parenthesize = !strategy.isAtom() && strategy.getPrecedence() < precedence;
        if (parenthesize) buffer.append('(');
        strategy.writeTo(buffer);
        if (parenthesize) buffer.append(')');
        return buffer;
    }

    /**
     * Writes a right strategy expression argument to the specified buffer, optionally wrapping it in parentheses.
     *
     * @param buffer the buffer to write to
     * @param strategy the strategy to write
     * @param precedence the precedence of the parent strategy; a higher value indicates a higher precedence
     * @param associativity the associativity of the parent strategy
     * @return the buffer
     */
    static StringBuilder writeRight(StringBuilder buffer, PrintableStrategy strategy, int precedence, Associativity associativity) {
        boolean parenthesize = !strategy.isAtom() && (strategy.getPrecedence() < precedence
            || (strategy.getPrecedence() == precedence && associativity == Associativity.Left));
        if (parenthesize) buffer.append('(');
        strategy.writeTo(buffer);
        if (parenthesize) buffer.append(')');
        return buffer;
    }

}
