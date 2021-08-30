package mb.statix.strategies;

import mb.statix.sequences.InterruptibleBiConsumer;
import mb.statix.sequences.InterruptibleBiFunction;
import mb.statix.sequences.InterruptibleBiPredicate;
import mb.statix.sequences.InterruptibleConsumer;
import mb.statix.sequences.InterruptibleFunction;
import mb.statix.sequences.InterruptiblePredicate;
import mb.statix.sequences.InterruptibleSupplier;
import mb.statix.utils.CaseFormat;
import mb.statix.utils.StringUtils;

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
public interface PrintableStrategy extends Writable {

    /**
     * Gets the name of the strategy.
     *
     * When the strategy is anonymous,
     * the return value of {@link #getName()} may not be human-readable.
     *
     * @return the name of the strategy
     */
    default String getName() {
        // Makes a best-effort to guess a printable name for the strategy.
        String strategyName = this.getClass().getSimpleName();
        // If the class name ends with Strategy (e.g., IdStrategy), remove this suffix.
        if (strategyName.endsWith("Strategy")) {
            strategyName = strategyName.substring(0, strategyName.length() - "Strategy".length());
        }
        // Translate the "CamelCase" name into a "kebab-case" name.
        return CaseFormat.combineKebabCase(CaseFormat.splitCamelCase(strategyName));
    }

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
     * Gets whether this strategy is anonymous.
     *
     * A strategy is anonymous when it was created from a lambda or closure,
     * or when it is the application of a strategy.
     *
     * @return {@code true} when this strategy is anonymous;
     * otherwise, {@code false}
     */
    default boolean isAnonymous() { return true; }

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
