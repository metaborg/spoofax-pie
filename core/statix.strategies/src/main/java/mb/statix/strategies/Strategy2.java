package mb.statix.strategies;

import mb.statix.sequences.Seq;
import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A strategy.
 *
 * @param <A1> the type of the first argument (contravariant)
 * @param <A2> the type of the second argument (contravariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@SuppressWarnings("Convert2Diamond") @FunctionalInterface
public interface Strategy2<A1, A2, T, R> extends StrategyDecl, PrintableStrategy {

    @Override
    default int getArity() { return 2; }

    /**
     * Applies the strategy to the given arguments.
     *
     * @param engine the Tego engine
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param input the input argument
     * @return the result; or {@code null} if the strategy failed
     */
    @Nullable R evalInternal(TegoEngine engine, A1 arg1, A2 arg2, T input);

    @SuppressWarnings("unchecked")
    @Override
    default @Nullable Object evalInternal(TegoEngine engine, Object[] args, Object input) {
        assert args.length == 2 : "Expected 2 arguments, got " + args.length + ".";
        return evalInternal(engine, (A1)args[0], (A2)args[1], (T)input);
    }

    /**
     * Partially applies the strategy, providing the first argument.
     *
     * As an optimization, partially applying the returned strategy
     * will not wrap the strategy twice.
     *
     * @param arg1 the first argument
     * @return the partially applied strategy
     */
    default Strategy1<A2, T, R> apply(A1 arg1) { return new NamedStrategy1<A2, T, R>() {
        @Override
        public String getName() {
            return Strategy2.this.getName();
        }

        @Override
        public String getParamName(int index) {
            return Strategy2.this.getParamName(index + 1);
        }

        @Override
        public void writeArg(StringBuilder sb, int index, Object arg) {
            Strategy2.this.writeArg(sb, index + 1, arg);
        }

        @Override
        public boolean isAnonymous() {
            return Strategy2.this.isAnonymous();
        }

        @Override
        public boolean isAtom() {
            // This is an atom, because it is pretty-printed as a atomic strategy application
            // e.g., "foo(arg1, arg2, ..)"
            return true;
        }

        @Override
        public int getPrecedence() {
            // Precedence doesn't matter when isAtom == true,
            // but in case an overriding class wants isAtom == false,
            // the precedence is the same as for the applied strategy.
            return Strategy2.this.getPrecedence();
        }

        @Override
        public @Nullable R evalInternal(TegoEngine engine, A2 arg2, T input) {
            return Strategy2.this.evalInternal(engine, arg1, arg2, input);
        }

        @Override
        public Strategy<T, R> apply(A2 arg2) {
            return Strategy2.this.apply(arg1, arg2);
        }

        @Override
        public StringBuilder writeTo(StringBuilder sb) {
            sb.append(getName());
            sb.append('(');
            Strategy2.this.writeArg(sb, 0, arg1);
            sb.append(", ..");
            sb.append(')');
            return sb;
        }
    }; }

    /**
     * Partially applies the strategy, providing the first two arguments.
     *
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the partially applied strategy
     */
    default Strategy<T, R> apply(A1 arg1, A2 arg2) { return new NamedStrategy<T, R>() {
        @Override
        public String getName() {
            return Strategy2.this.getName();
        }

        @Override
        public String getParamName(int index) {
            return Strategy2.this.getParamName(index + 2);
        }


        @Override
        public void writeArg(StringBuilder sb, int index, Object arg) {
            Strategy2.this.writeArg(sb, index + 2, arg);
        }

        @Override
        public boolean isAnonymous() {
            return Strategy2.this.isAnonymous();
        }

        @Override
        public boolean isAtom() {
            // This is an atom, because it is pretty-printed as a atomic strategy application
            // e.g., "foo(arg1, arg2, ..)"
            return true;
        }

        @Override
        public int getPrecedence() {
            // Precedence doesn't matter when isAtom == true,
            // but in case an overriding class wants isAtom == false,
            // the precedence is the same as for the applied strategy.
            return Strategy2.this.getPrecedence();
        }

        @Override
        public @Nullable R evalInternal(TegoEngine engine, T input) {
            return Strategy2.this.evalInternal(engine, arg1, arg2, input);
        }

        @Override
        public StringBuilder writeTo(StringBuilder sb) {
            sb.append(getName());
            sb.append('(');
            Strategy2.this.writeArg(sb, 0, arg1);
            sb.append(", ");
            Strategy2.this.writeArg(sb, 1, arg2);
            sb.append(')');
            return sb;
        }
    }; }

}
