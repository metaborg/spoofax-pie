package mb.tego.strategies3;


import mb.tego.sequences.Seq;
import mb.tego.strategies3.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A strategy.
 *
 * @param <A1> the type of the first argument (contravariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@SuppressWarnings("Convert2Diamond") @FunctionalInterface
public interface Strategy1<A1, T, R> extends StrategyDecl, PrintableStrategy {

    @Override
    default int getArity() { return 1; }

    /**
     * Evaluates the strategy.
     *
     * @param engine the Tego engine
     * @param arg1 the first argument
     * @param input the input argument
     * @return the lazy computation
     */
    Seq<R> evalInternal(TegoEngine engine, A1 arg1, T input);

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default Seq evalInternal(TegoEngine engine, Object[] args, Object input) {
        assert args.length == 1 : "Expected 1 arguments, got " + args.length + ".";
        return evalInternal(engine, (A1)args[0], (T)input);
    }

    /**
     * Partially applies the strategy, providing the first argument.
     *
     * @param arg1 the first argument
     * @return the partially applied strategy
     */
    default Strategy<T, R> apply(A1 arg1) { return new NamedStrategy<T, R>() {
        @Override
        public String getName() {
            return Strategy1.this.getName();
        }

        @Override
        public String getParamName(int index) {
            return Strategy1.this.getParamName(index + 1);
        }

        @Override
        public void writeArg(StringBuilder sb, int index, Object arg) {
            Strategy1.this.writeArg(sb, index + 1, arg);
        }

        @Override
        public boolean isAnonymous() {
            return Strategy1.this.isAnonymous();
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
            return Strategy1.this.getPrecedence();
        }

        @Override
        public Seq<R> evalInternal(TegoEngine engine, T input) {
            return Strategy1.this.evalInternal(engine, arg1, input);
        }

        @Override
        public StringBuilder writeTo(StringBuilder sb) {
            sb.append(getName());
            sb.append('(');
            Strategy1.this.writeArg(sb, 0, arg1);
            sb.append(')');
            return sb;
        }
    }; }


}
