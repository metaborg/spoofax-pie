package mb.common.result;

import mb.common.util.SneakyThrow;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T extends Serializable, E extends Throwable> extends Serializable {
    class Ok<T extends Serializable, E extends Throwable> implements Result<T, E>, Serializable {
        public final T value;

        public Ok(T value) {
            this.value = value;
        }


        @Override public boolean isOk() {
            return true;
        }

        @Override public Optional<T> ok() {
            return Optional.of(value);
        }

        @Override public boolean isErr() {
            return false;
        }

        @Override public Optional<E> err() {
            return Optional.empty();
        }


        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Ok<?, ?> ok = (Ok<?, ?>)o;
            return value.equals(ok.value);
        }

        @Override public int hashCode() {
            return value.hashCode();
        }

        @Override public String toString() {
            return "Ok(" + value + ")";
        }
    }

    class Err<T extends Serializable, E extends Throwable> implements Result<T, E>, Serializable {
        public final E error;

        public Err(E error) {
            this.error = error;
        }


        @Override public boolean isOk() {
            return false;
        }

        @Override public Optional<T> ok() {
            return Optional.empty();
        }

        @Override public boolean isErr() {
            return true;
        }

        @Override public Optional<E> err() {
            return Optional.of(error);
        }


        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Err<?, ?> err = (Err<?, ?>)o;
            return error.equals(err.error);
        }

        @Override public int hashCode() {
            return error.hashCode();
        }

        @Override public String toString() {
            return "Err(" + error + ")";
        }
    }


    static <T extends Serializable, E extends Throwable> Result<T, E> ofOk(T value) {
        return new Ok<>(value);
    }

    static <T extends Serializable, E extends Throwable> Result<T, E> ofErr(E error) {
        return new Err<>(error);
    }


    boolean isOk();

    Optional<T> ok();

    default void ifOk(Consumer<? super T> consumer) {
        ok().ifPresent(consumer);
    }


    boolean isErr();

    Optional<E> err();

    default void ifErr(Consumer<? super E> consumer) {
        err().ifPresent(consumer);
    }

    default void throwIfError() throws E {
        if(isErr()) {
            // noinspection OptionalGetWithoutIsPresent (get is safe because error is present if isErr returns true)
            throw err().get();
        }
    }

    default void throwUncheckedIfError() {
        if(isErr()) {
            // noinspection OptionalGetWithoutIsPresent (get is safe because error is present if isErr returns true)
            throw new RuntimeException(err().get());
        }
    }


    default <U extends Serializable> Result<U, E> map(Function<T, U> mapper) {
        // noinspection unchecked (cast is safe because it is impossible to get a value of type U in the err case)
        return ok().map(v -> Result.<U, E>ofOk(mapper.apply(v))).orElse((Result<U, E>)this);
    }

    default <U extends Serializable> U mapOr(U def, Function<T, U> mapper) {
        return ok().map(mapper).orElse(def);
    }

    default <U extends Serializable> U mapOrElse(Supplier<U> def, Function<T, U> mapper) {
        return ok().map(mapper).orElseGet(def);
    }

    default <U extends @Nullable Serializable> @Nullable U mapOrNull(Function<T, U> mapper) {
        return ok().map(mapper).orElse(null);
    }

    default <F extends Throwable> Result<T, F> mapErr(Function<E, F> mapper) {
        // noinspection unchecked (cast is safe because it is impossible to get a value of type F in the ok case)
        return err().map(e -> Result.<T, F>ofErr(mapper.apply(e))).orElse((Result<T, F>)this);
    }

    default <F extends Throwable> F mapErrOr(F def, Function<E, F> mapper) {
        return err().map(mapper).orElse(def);
    }

    default <F extends Throwable> F mapErrOrElse(Supplier<F> def, Function<E, F> mapper) {
        return err().map(mapper).orElseGet(def);
    }

    default <F extends Throwable> @Nullable F mapErrOrNull(Function<E, F> mapper) {
        return err().map(mapper).orElse(null);
    }

    default <R extends Serializable> R mapRes(Function<T, R> valueMapper, Function<E, R> errorMapper) {
        //noinspection OptionalGetWithoutIsPresent (get is safe because error is present if not ok case)
        return ok().map(valueMapper).orElse(errorMapper.apply(err().get()));
    }


    default <U extends Serializable> Result<U, E> andThen(Function<T, Result<U, E>> mapper) {
        // noinspection unchecked (cast is safe because it is impossible to get a value of type U in the err case)
        return ok().map(mapper).orElse((Result<U, E>)this);
    }


    default T unwrap() throws E {
        // noinspection OptionalGetWithoutIsPresent (get is safe because error is present if not ok case)
        return ok().orElseThrow(() -> err().get());
    }

    default T unwrapSneaky() {
        if(isErr()) {
            // noinspection OptionalGetWithoutIsPresent (get is safe because error is present if err case)
            SneakyThrow.doThrow(err().get());
        }
        // noinspection OptionalGetWithoutIsPresent (get is safe because value is present if not err case)
        return ok().get();
    }

    default T unwrapUnchecked() {
        // noinspection OptionalGetWithoutIsPresent (get is safe because error is present if not ok case)
        return ok().orElseThrow(() -> new RuntimeException(err().get()));
    }

    default T unwrapOr(T def) {
        return ok().orElse(def);
    }

    default T unwrapOrElse(Supplier<T> def) {
        return ok().orElseGet(def);
    }

    default E unwrapErr() {
        return err().orElseThrow(() -> new RuntimeException("Called `unwrapErr` on an `Ok` result"));
    }

    default E unwrapErrOr(E def) {
        return err().orElse(def);
    }

    default E unwrapErrOrElse(Supplier<E> def) {
        return err().orElseGet(def);
    }


    default @Nullable T get() {
        return ok().orElse(null);
    }

    default @Nullable T getOr(@Nullable T def) {
        return ok().orElse(def);
    }

    default @Nullable T getOrElse(Supplier<@Nullable T> def) {
        return ok().orElseGet(def);
    }

    default @Nullable E getErr() {
        return err().orElse(null);
    }

    default @Nullable E getErrOr(@Nullable E def) {
        return err().orElse(def);
    }

    default @Nullable E getErrOrElse(Supplier<@Nullable E> def) {
        return err().orElseGet(def);
    }
}
