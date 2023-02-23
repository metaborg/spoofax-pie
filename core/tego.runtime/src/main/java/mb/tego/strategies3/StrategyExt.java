package mb.tego.strategies3;


import mb.tego.functions.Function3;
import mb.tego.sequences.Seq;
import mb.tego.strategies3.runtime.TegoEngine;
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
     * Defines a variable with the result of a strategy in the scope of another strategy.
     *
     * @param vs the strategy providing the values
     * @param f the function accepting the values and providing the strategy
     * @param <A> the type of the value (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the resulting strategy
     */
    public static <A, I, O> Strategy<I, O> let(Strategy<I, A> vs, Function<Seq<A>, Strategy<I, O>> f) {
        return (engine, input) -> {
            final Seq<A> a = engine.eval(vs, input);
            final Strategy<I, O> s = f.apply(a);
            return engine.eval(s, input);
        };
    }

    /**
     * Creates a lambda strategy.
     * <p>
     * This is a convenience method for using a {@link LambdaStrategy1} in place of a {@link Strategy1}.
     *
     * @param s the lambda strategy definition
     * @param <A1> the type of the first argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the strategy
     */
    public static <A1, I, O> LambdaStrategy1<A1, I, O> lam(LambdaStrategy1<A1, I, O> s) {
        return s;
    }

    /**
     * Creates a lambda strategy.
     * <p>
     * This is a convenience method for using a {@link LambdaStrategy2} in place of a {@link Strategy2}.
     *
     * @param s the lambda strategy definition
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the strategy
     */
    public static <A1, A2, I, O> LambdaStrategy2<A1, A2, I, O> lam(LambdaStrategy2<A1, A2, I, O> s) {
        return s;
    }

    /**
     * Creates a lambda strategy.
     * <p>
     * This is a convenience method for using a {@link LambdaStrategy3} in place of a {@link Strategy3}.
     *
     * @param s the lambda strategy definition
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <I> the type of input (contravariant)
     * @param <O> the type of output (covariant)
     * @return the strategy
     */
    public static <A1, A2, A3, I, O> LambdaStrategy3<A1, A2, A3, I, O> lam(LambdaStrategy3<A1, A2, A3, I, O> s) {
        return s;
    }

    /**
     * Creates a predicate strategy.
     * <p>
     * This is a convenience method for using a {@link PredicateStrategy} in place of a {@link Strategy}.
     *
     * @param s the predicate strategy definition
     * @param <T> the type of input/output (invariant)
     * @return the strategy
     */
    public static <T> PredicateStrategy<T> pred(PredicateStrategy<T> s) {
        return s;
    }

    /**
     * Creates a predicate strategy.
     * <p>
     * This is a convenience method for using a {@link PredicateStrategy1} in place of a {@link Strategy1}.
     *
     * @param s the predicate strategy definition
     * @param <A1> the type of the first argument (contravariant)
     * @param <T> the type of input/output (invariant)
     * @return the strategy
     */
    public static <A1, T> PredicateStrategy1<A1, T> pred(PredicateStrategy1<A1, T> s) {
        return s;
    }

    /**
     * Creates a predicate strategy.
     * <p>
     * This is a convenience method for using a {@link PredicateStrategy2} in place of a {@link Strategy2}.
     *
     * @param s the predicate strategy definition
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <T> the type of input/output (invariant)
     * @return the strategy
     */
    public static <A1, A2, T> PredicateStrategy2<A1, A2, T> pred(PredicateStrategy2<A1, A2, T> s) {
        return s;
    }

    /**
     * Creates a predicate strategy.
     * <p>
     * This is a convenience method for using a {@link PredicateStrategy3} in place of a {@link Strategy3}.
     *
     * @param s the predicate strategy definition
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <T> the type of input/output (invariant)
     * @return the strategy
     */
    public static <A1, A2, A3, T> PredicateStrategy3<A1, A2, A3, T> pred(PredicateStrategy3<A1, A2, A3, T> s) {
        return s;
    }

    /**
     * Creates a function call strategy.
     * <p>
     * This is a convenience method for using a {@link FunctionStrategy} in place of a {@link Strategy}.
     *
     * @param s the predicate strategy definition
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the strategy
     */
    public static <T, R> FunctionStrategy<T, R> fun(FunctionStrategy<T, R> s) {
        return s;
    }

    /**
     * Creates a function call strategy.
     * <p>
     * This is a convenience method for using a {@link FunctionStrategy1} in place of a {@link Strategy1}.
     *
     * @param s the predicate strategy definition
     * @param <A1> the type of the first argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the strategy
     */
    public static <A1, T, R> FunctionStrategy1<A1, T, R> fun(FunctionStrategy1<A1, T, R> s) {
        return s;
    }

    /**
     * Creates a function call strategy.
     * <p>
     * This is a convenience method for using a {@link FunctionStrategy2} in place of a {@link Strategy2}.
     *
     * @param s the predicate strategy definition
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the strategy
     */
    public static <A1, A2, T, R> FunctionStrategy2<A1, A2, T, R> fun(FunctionStrategy2<A1, A2, T, R> s) {
        return s;
    }

    /**
     * Creates a function call strategy.
     * <p>
     * This is a convenience method for using a {@link FunctionStrategy3} in place of a {@link Strategy3}.
     *
     * @param s the predicate strategy definition
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the strategy
     */
    public static <A1, A2, A3, T, R> FunctionStrategy3<A1, A2, A3, T, R> fun(FunctionStrategy3<A1, A2, A3, T, R> s) {
        return s;
    }

    /**
     * Defines a named strategy with no arguments.
     *
     * @param name the name of the strategy
     * @param builder the strategy builder, which takes no arguments
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the built strategy
     */
    public static <T, R> Strategy<T, R> def(String name, Supplier<Strategy<T, R>> builder) {
        // Wraps a strategy builder, and gives it a name.
        return def(name, builder.get());
    }

    /**
     * Defines a named strategy with no arguments.
     *
     * @param name the strategy name
     * @param strategy the strategy, which takes no arguments
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the named strategy
     */
    public static <T, R> Strategy<T, R> def(String name, Strategy<T, R> strategy) {
        // Wraps a strategy, and gives it a name.
        return new NamedStrategy<T, R>() {
            @Override
            public Seq<R> evalInternal(TegoEngine engine, T input) {
                return strategy.evalInternal(engine, input);
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
     * @param <A1> the type of the first argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the built strategy
     */
    public static <A1, T, R> Strategy1<A1, T, R> def(String name, String param1, Function<A1, Strategy<T, R>> builder) {
        // Wraps a strategy builder, and gives it a name.
        return new NamedStrategy1<A1, T, R>() {
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
            public Strategy<T, R> apply(A1 arg1) {
                return def(name, builder.apply(arg1));
            }

            @Override
            public Seq<R> evalInternal(TegoEngine engine, A1 arg1, T input) {
                return apply(arg1).evalInternal(engine, input);
            }
        };
    }

    /**
     * Defines a named strategy with one argument.
     *
     * @param name the name of the strategy
     * @param strategy the strategy, which takes one argument
     * @param <A1> the type of the first argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the built strategy
     */
    public static <A1, T, R> Strategy1<A1, T, R> def(String name, String param1, Strategy1<A1, T, R> strategy) {
        // Wraps a strategy, and gives it a name.
        return new NamedStrategy1<A1, T, R>() {
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
            public Seq<R> evalInternal(TegoEngine engine, A1 arg1, T input) {
                return strategy.evalInternal(engine, arg1, input);
            }
        };
    }

    // -- //

    /**
     * Defines a named strategy with two arguments.
     *
     * @param name the name of the strategy
     * @param builder the strategy builder, which takes two arguments
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the built strategy
     */
    public static <A1, A2, T, R> Strategy2<A1, A2, T, R> def(String name, String param1, String param2, BiFunction<A1, A2, Strategy<T, R>> builder) {
        // Wraps a strategy builder, and gives it a name.
        return new NamedStrategy2<A1, A2, T, R>() {
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
            public Strategy<T, R> apply(A1 arg1, A2 arg2) {
                return def(name, builder.apply(arg1, arg2));
            }

            @Override
            public Seq<R> evalInternal(TegoEngine engine, A1 arg1, A2 arg2, T input) {
                return apply(arg1, arg2).evalInternal(engine, input);
            }
        };
    }

    /**
     * Defines a named strategy with two arguments.
     *
     * @param name the name of the strategy
     * @param strategy the strategy, which takes two arguments
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the built strategy
     */
    public static <A1, A2, T, R> Strategy2<A1, A2, T, R> def(String name, String param1, String param2, Strategy2<A1, A2, T, R> strategy) {
        // Wraps a strategy, and gives it a name.
        return new NamedStrategy2<A1, A2, T, R>() {
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
            public Seq<R> evalInternal(TegoEngine engine, A1 arg1, A2 arg2, T input) {
                return strategy.evalInternal(engine, arg1, arg2, input);
            }
        };
    }

    /**
     * Defines a named strategy with three arguments.
     *
     * @param name the name of the strategy
     * @param builder the strategy builder, which takes three arguments
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the built strategy
     */
    public static <A1, A2, A3, T, R> Strategy3<A1, A2, A3, T, R> def(String name, String param1, String param2, String param3, Function3<A1, A2, A3, Strategy<T, R>> builder) {
        // Wraps a strategy builder, and gives it a name.
        return new NamedStrategy3<A1, A2, A3, T, R>() {
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
            public Strategy<T, R> apply(A1 arg1, A2 arg2, A3 arg3) {
                return def(name, builder.apply(arg1, arg2, arg3));
            }

            @Override
            public Seq<R> evalInternal(TegoEngine engine, A1 arg1, A2 arg2, A3 arg3, T input) {
                return apply(arg1, arg2, arg3).evalInternal(engine, input);
            }
        };
    }

    /**
     * Defines a named strategy with three arguments.
     *
     * @param name the name of the strategy
     * @param strategy the strategy, which takes three arguments
     * @param <A1> the type of the first argument (contravariant)
     * @param <A2> the type of the second argument (contravariant)
     * @param <A3> the type of the third argument (contravariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the built strategy
     */
    public static <A1, A2, A3, T, R> Strategy3<A1, A2, A3, T, R> def(String name, String param1, String param2, String param3, Strategy3<A1, A2, A3, T, R> strategy) {
        // Wraps a strategy and gives it a name.
        return new NamedStrategy3<A1, A2, A3, T, R>() {
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
            public Seq<R> evalInternal(TegoEngine engine, A1 arg1, A2 arg2, A3 arg3, T input) {
                return strategy.evalInternal(engine, arg1, arg2, arg3, input);
            }
        };
    }

    /**
     * Builds a recursive strategy.
     *
     * @param f the strategy builder function, which takes a reference to the built strategy itself
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the resulting strategy
     */
    public static <T, R> Strategy<T, R> rec(Function<Strategy<T, R>, Strategy<T, R>> f) {
        return new Strategy<T, R>() {
            @Override
            public Seq<R> evalInternal(TegoEngine engine, T input) {
                return engine.eval(f.apply(this), input);
            }
        };
    }

    /**
     * Asserts that the value is not null.
     *
     * @param r the return value
     * @param <R> the type of return value
     * @return the return value
     */
    public static <R> R nn(@Nullable R r) {
        assert r != null : "Value is not supposed to be null";
        return r;
    }
}
