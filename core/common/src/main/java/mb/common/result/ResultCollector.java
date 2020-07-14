package mb.common.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ResultCollector<V, C extends Collection<V>, E extends Exception, A extends Exception> implements Collector<Result<V, E>, ResultCollector.ResultAccumulator<V,C,E>, Result<C, A>> {

    private final Supplier<C> collectionSupplier;
    private final BiFunction<C, Collection<E>, A> exceptionBuilder;

    private ResultCollector(Supplier<C> collectionSupplier, BiFunction<C, Collection<E>, A> exceptionBuilder) {
        this.collectionSupplier = collectionSupplier;
        this.exceptionBuilder = exceptionBuilder;
    }

    @Override
    public Supplier<ResultAccumulator<V,C,E>> supplier() {
        return () -> new ResultAccumulatorImpl<>(collectionSupplier.get());
    }

    @Override
    public BiConsumer<ResultAccumulator<V,C,E>, Result<V, E>> accumulator() {
        return ResultAccumulator::add;
    }

    @Override
    public BinaryOperator<ResultAccumulator<V,C,E>> combiner() {
        return ResultAccumulator::combine;
    }

    @Override
    public Function<ResultAccumulator<V,C,E>, Result<C, A>> finisher() {
        return acc -> acc.build(exceptionBuilder);
    }

    @Override
    public Set<Characteristics> characteristics() {
        Set<Characteristics> characteristics = new HashSet<>();
        characteristics.add(Characteristics.CONCURRENT);
        characteristics.add(Characteristics.UNORDERED);
        return characteristics;
    }

    // Static factory methods

    private static <V> Supplier<Set<V>> getDefaultCollectionSupplier() {
        return HashSet::new;
    }

    private static <V, C extends Collection<V>, E extends Exception> BiFunction<C, Collection<E>, AggregateException> getDefaultExceptionBuilder() {
        return AggregateException::new;
    }

    public static <V, E extends Exception> ResultCollector<V, Set<V>, E, AggregateException> getDefault() {
        return new ResultCollector<>(getDefaultCollectionSupplier(), getDefaultExceptionBuilder());
    }

    public static <V, C extends Collection<V>, E extends Exception> ResultCollector<V, C, E, AggregateException> getWithCollection(Supplier<C> collectionSupplier) {
        return new ResultCollector<>(collectionSupplier, getDefaultExceptionBuilder());
    }

    public static <V, E extends Exception, A extends Exception> ResultCollector<V, Set<V>, E, A> getWithExceptionBuilder(BiFunction<Set<V>, Collection<E>, A> exceptionBuilder) {
        return new ResultCollector<>(getDefaultCollectionSupplier(), exceptionBuilder);
    }

    public static <V, C extends Collection<V>, E extends Exception, A extends Exception> ResultCollector<V, C, E, A> getWithCollectionAndExceptionBuilder(Supplier<C> collectionSupplier, BiFunction<C, Collection<E>, A> exceptionBuilder) {
        return new ResultCollector<>(collectionSupplier, exceptionBuilder);
    }

    public static <V, E extends Exception, A extends Exception> ResultCollector<V, Set<V>, E, A> getWithBaseException(A baseException) {
        return getWithCollectionAndBaseException(getDefaultCollectionSupplier(), baseException);
    }

    public static <V, C extends Collection<V>, E extends Exception, A extends Exception> ResultCollector<V, C, E, A> getWithCollectionAndBaseException(Supplier<C> collectionSupplier, A baseException) {
        return new ResultCollector<>(collectionSupplier, (v, es) -> {
            es.forEach(baseException::addSuppressed);
            return baseException;
        });
    }

    // Accumulator interface and implementation

    public interface ResultAccumulator<V, C extends Collection<V>, E extends Exception> {
        void add(Result<V, E> result);

        ResultAccumulator<V, C, E> combine(ResultAccumulator<V, C, E> other);

        <A extends Exception> Result<C, A> build(BiFunction<C, Collection<E>, A> exceptionBuilder);
    }

    private static class ResultAccumulatorImpl<V, C extends Collection<V>, E extends Exception> implements ResultAccumulator<V, C, E> {
        private final C values;
        private final List<E> exceptions = new ArrayList<>();

        private ResultAccumulatorImpl(C initialValues) {
            values = initialValues;
        }

        @Override public void add(Result<V, E> result) {
            result.ifElse(values::add, exceptions::add);
        }

        @SuppressWarnings("unchecked") /* other has type parameter E, so cast to E is safe after build */
        @Override public ResultAccumulator<V, C, E> combine(ResultAccumulator<V, C, E> other) {
            other.build(AggregateException::new)
                .ifElse(values::addAll, exc -> exc.getInnerExceptions().stream()
                    .map(e -> (E) e)
                    .forEach(exceptions::add));
            return this;
        }

        @Override public <A extends Exception> Result<C, A> build(BiFunction<C, Collection<E>, A> exceptionBuilder) {
            return exceptions.isEmpty() ? Result.ofOk(values) : Result.ofErr(exceptionBuilder.apply(values, exceptions));
        }
    }
}
