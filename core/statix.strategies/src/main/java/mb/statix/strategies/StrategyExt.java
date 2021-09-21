package mb.statix.strategies;

import mb.statix.functions.Function3;
import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Extension functions for strategies.
 */
@SuppressWarnings("unused")
public final class StrategyExt {
    private StrategyExt() { /* Cannot be instantiated. */ }

    /**
     * Creates a lambda strategy.
     *
     * This is a convenience method for using a {@link LambdaStrategy1} in place of a {@link Strategy1}.
     *
     * @param s the lambda strategy definition
     * @param <CTX> the context
     * @param <A1> the type of the first argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the strategy
     */
    public static <CTX, A1, I, O> LambdaStrategy1<CTX, A1, I, O> lam(LambdaStrategy1<CTX, A1, I, O> s) {
        return s;
    }

    /**
     * Creates a lambda strategy.
     *
     * This is a convenience method for using a {@link LambdaStrategy2} in place of a {@link Strategy2}.
     *
     * @param s the lambda strategy definition
     * @param <CTX> the context
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the strategy
     */
    public static <CTX, A1, A2, I, O> LambdaStrategy2<CTX, A1, A2, I, O> lam(LambdaStrategy2<CTX, A1, A2, I, O> s) {
        return s;
    }

    /**
     * Creates a lambda strategy.
     *
     * This is a convenience method for using a {@link LambdaStrategy3} in place of a {@link Strategy3}.
     *
     * @param s the lambda strategy definition
     * @param <CTX> the context
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the strategy
     */
    public static <CTX, A1, A2, A3, I, O> LambdaStrategy3<CTX, A1, A2, A3, I, O> lam(LambdaStrategy3<CTX, A1, A2, A3, I, O> s) {
        return s;
    }

    /**
     * Creates a predicate strategy.
     *
     * This is a convenience method for using a {@link PredicateStrategy} in place of a {@link Strategy}.
     *
     * @param s the predicate strategy definition
     * @param <CTX> the context
     * @param <T> the type of input/output (invariant)
     * @return the strategy
     */
    public static <CTX, T> PredicateStrategy<CTX, T> pred(PredicateStrategy<CTX, T> s) {
        return s;
    }

    /**
     * Creates a predicate strategy.
     *
     * This is a convenience method for using a {@link PredicateStrategy1} in place of a {@link Strategy1}.
     *
     * @param s the predicate strategy definition
     * @param <CTX> the context
     * @param <A1> the type of the first argument (contravariant)
     * @param <T> the type of input/output (invariant)
     * @return the strategy
     */
    public static <CTX, A1, T> PredicateStrategy1<CTX, A1, T> pred(PredicateStrategy1<CTX, A1, T> s) {
        return s;
    }

    /**
     * Creates a predicate strategy.
     *
     * This is a convenience method for using a {@link PredicateStrategy2} in place of a {@link Strategy2}.
     *
     * @param s the predicate strategy definition
     * @param <CTX> the context
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <T> the type of input/output (invariant)
     * @return the strategy
     */
    public static <CTX, A1, A2, T> PredicateStrategy2<CTX, A1, A2, T> pred(PredicateStrategy2<CTX, A1, A2, T> s) {
        return s;
    }

    /**
     * Creates a predicate strategy.
     *
     * This is a convenience method for using a {@link PredicateStrategy3} in place of a {@link Strategy3}.
     *
     * @param s the predicate strategy definition
     * @param <CTX> the context
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <T> the type of input/output (invariant)
     * @return the strategy
     */
    public static <CTX, A1, A2, A3, T> PredicateStrategy3<CTX, A1, A2, A3, T> pred(PredicateStrategy3<CTX, A1, A2, A3, T> s) {
        return s;
    }

    /**
     * Creates a function call strategy.
     *
     * This is a convenience method for using a {@link FunctionStrategy} in place of a {@link Strategy}.
     *
     * @param s the predicate strategy definition
     * @param <CTX> the context
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the strategy
     */
    public static <CTX, T, R> FunctionStrategy<CTX, T, R> fun(FunctionStrategy<CTX, T, R> s) {
        return s;
    }

    /**
     * Creates a function call strategy.
     *
     * This is a convenience method for using a {@link FunctionStrategy1} in place of a {@link Strategy1}.
     *
     * @param s the predicate strategy definition
     * @param <CTX> the context
     * @param <A1> the type of the first argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the strategy
     */
    public static <CTX, A1, T, R> FunctionStrategy1<CTX, A1, T, R> fun(FunctionStrategy1<CTX, A1, T, R> s) {
        return s;
    }

    /**
     * Creates a function call strategy.
     *
     * This is a convenience method for using a {@link FunctionStrategy2} in place of a {@link Strategy2}.
     *
     * @param s the predicate strategy definition
     * @param <CTX> the context
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the strategy
     */
    public static <CTX, A1, A2, T, R> FunctionStrategy2<CTX, A1, A2, T, R> fun(FunctionStrategy2<CTX, A1, A2, T, R> s) {
        return s;
    }

    /**
     * Creates a function call strategy.
     *
     * This is a convenience method for using a {@link FunctionStrategy3} in place of a {@link Strategy3}.
     *
     * @param s the predicate strategy definition
     * @param <CTX> the context
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the strategy
     */
    public static <CTX, A1, A2, A3, T, R> FunctionStrategy3<CTX, A1, A2, A3, T, R> fun(FunctionStrategy3<CTX, A1, A2, A3, T, R> s) {
        return s;
    }

    /**
     * Defines a named strategy with no arguments.
     *
     * @param name the name of the strategy
     * @param builder the strategy builder, which takes no arguments
     * @param <CTX> the type of context (invariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the built strategy
     */
    public static <CTX, I, O> Strategy<CTX, I, O> def(String name, Supplier<Strategy<CTX, I, O>> builder) {
        // Wraps a strategy builder, and gives it a name.
        return def(name, builder.get());
    }

    /**
     * Defines a named strategy with no arguments.
     *
     * @param name the strategy name
     * @param strategy the strategy, which takes no arguments
     * @param <CTX> the type of context (invariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the named strategy
     */
    public static <CTX, I, O> Strategy<CTX, I, O> def(String name, Strategy<CTX, I, O> strategy) {
        // Wraps a strategy, and gives it a name.
        return new NamedStrategy<CTX, I, O>() {
            @Override
            public @Nullable O evalInternal(TegoEngine engine, CTX ctx, I input) {
                return strategy.evalInternal(engine, ctx, input);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    /**
     * Defines a named strategy with one argument.
     *
     * @param name the name of the strategy
     * @param builder the strategy builder, which takes one argument
     * @param <CTX> the type of context (invariant)
     * @param <A1> the type of the first argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the built strategy
     */
    public static <CTX, A1, I, O> Strategy1<CTX, A1, I, O> def(String name, String param1, Function<A1, Strategy<CTX, I, O>> builder) {
        // Wraps a strategy builder, and gives it a name.
        return new NamedStrategy1<CTX, A1, I, O>() {
            @Override
            public String getName() {
                return name;
            }

            @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
            public String getParamName(int index) {
                switch (index) {
                    case 0: return param1;
                    default: return super.getParamName(index);
                }
            }

            @Override
            public Strategy<CTX, I, O> apply(A1 arg1) {
                return def(name, builder.apply(arg1));
            }

            @Override
            public @Nullable O evalInternal(TegoEngine engine, CTX ctx, A1 arg1, I input) {
                return apply(arg1).evalInternal(engine, ctx, input);
            }
        };
    }

    /**
     * Defines a named strategy with one argument.
     *
     * @param name the name of the strategy
     * @param strategy the strategy, which takes one argument
     * @param <CTX> the type of context (invariant)
     * @param <A1> the type of the first argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the built strategy
     */
    public static <CTX, A1, I, O>  Strategy1<CTX, A1, I, O> def(String name, String param1, Strategy1<CTX, A1, I, O> strategy) {
        // Wraps a strategy, and gives it a name.
        return new NamedStrategy1<CTX, A1, I, O>() {
            @Override
            public String getName() {
                return name;
            }

            @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
            public String getParamName(int index) {
                switch (index) {
                    case 0: return param1;
                    default: return super.getParamName(index);
                }
            }

            @Override
            public @Nullable O evalInternal(TegoEngine engine, CTX ctx, A1 arg1, I input) {
                return strategy.evalInternal(engine, ctx, arg1, input);
            }
        };
    }

    // -- //

    /**
     * Defines a named strategy with two arguments.
     *
     * @param name the name of the strategy
     * @param builder the strategy builder, which takes two arguments
     * @param <CTX> the type of context (invariant)
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the built strategy
     */
    public static <CTX, A1, A2, I, O> Strategy2<CTX, A1, A2, I, O> def(String name, String param1, String param2, BiFunction<A1, A2, Strategy<CTX, I, O>> builder) {
        // Wraps a strategy builder, and gives it a name.
        return new NamedStrategy2<CTX, A1, A2, I, O>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getParamName(int index) {
                switch (index) {
                    case 0: return param1;
                    case 1: return param2;
                    default: return super.getParamName(index);
                }
            }

            @Override
            public Strategy<CTX, I, O> apply(A1 arg1, A2 arg2) {
                return def(name, builder.apply(arg1, arg2));
            }

            @Override
            public @Nullable O evalInternal(TegoEngine engine, CTX ctx, A1 arg1, A2 arg2, I input) {
                return apply(arg1, arg2).evalInternal(engine, ctx, input);
            }
        };
    }

    /**
     * Defines a named strategy with two arguments.
     *
     * @param name the name of the strategy
     * @param strategy the strategy, which takes two arguments
     * @param <CTX> the type of context (invariant)
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the built strategy
     */
    public static <CTX, A1, A2, I, O> Strategy2<CTX, A1, A2, I, O> def(String name, String param1, String param2, Strategy2<CTX, A1, A2, I, O> strategy) {
        // Wraps a strategy, and gives it a name.
        return new NamedStrategy2<CTX, A1, A2, I, O>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getParamName(int index) {
                switch (index) {
                    case 0: return param1;
                    case 1: return param2;
                    default: return super.getParamName(index);
                }
            }

            @Override
            public @Nullable O evalInternal(TegoEngine engine, CTX ctx, A1 arg1, A2 arg2, I input) {
                return strategy.evalInternal(engine, ctx, arg1, arg2, input);
            }
        };
    }

    // -- //

    /**
     * Defines a named strategy with three arguments.
     *
     * @param name the name of the strategy
     * @param builder the strategy builder, which takes three arguments
     * @param <CTX> the type of context (invariant)
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the built strategy
     */
    public static <CTX, A1, A2, A3, I, O> Strategy3<CTX, A1, A2, A3, I, O> def(String name, String param1, String param2, String param3, Function3<A1, A2, A3, Strategy<CTX, I, O>> builder) {
        // Wraps a strategy builder, and gives it a name.
        return new NamedStrategy3<CTX, A1, A2, A3, I, O>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getParamName(int index) {
                switch (index) {
                    case 0: return param1;
                    case 2: return param2;
                    case 3: return param3;
                    default: return super.getParamName(index);
                }
            }

            @Override
            public Strategy<CTX, I, O> apply(A1 arg1, A2 arg2, A3 arg3) {
                return def(name, builder.apply(arg1, arg2, arg3));
            }

            @Override
            public @Nullable O evalInternal(TegoEngine engine, CTX ctx, A1 arg1, A2 arg2, A3 arg3, I input) {
                return apply(arg1, arg2, arg3).evalInternal(engine, ctx, input);
            }
        };
    }

    /**
     * Defines a named strategy with three arguments.
     *
     * @param name the name of the strategy
     * @param strategy the strategy, which takes three arguments
     * @param <CTX> the type of context (invariant)
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the built strategy
     */
    public static <CTX, A1, A2, A3, I, O> Strategy3<CTX, A1, A2, A3, I, O> def(String name, String param1, String param2, String param3, Strategy3<CTX, A1, A2, A3, I, O> strategy) {
        // Wraps a strategy and gives it a name.
        return new NamedStrategy3<CTX, A1, A2, A3, I, O>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getParamName(int index) {
                switch (index) {
                    case 0: return param1;
                    case 2: return param2;
                    case 3: return param3;
                    default: return super.getParamName(index);
                }
            }

            @Override
            public @Nullable O evalInternal(TegoEngine engine, CTX ctx, A1 arg1, A2 arg2, A3 arg3, I input) {
                return strategy.evalInternal(engine, ctx, arg1, arg2, arg3, input);
            }
        };
    }
}
