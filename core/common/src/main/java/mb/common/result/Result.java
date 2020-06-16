package mb.common.result;

import mb.common.option.Option;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T extends Serializable, E extends Throwable> extends Serializable {
    static <T extends Serializable, E extends Throwable> Result<T, E> ofOk(T value) {
        return new Ok<>(value);
    }

    static <T extends Serializable, E extends Throwable> Result<T, E> ofErr(E error) {
        return new Err<>(error);
    }


    boolean isOk();

    Option<T> ok();

    default void ifOk(Consumer<? super T> consumer) {
        ok().ifSome(consumer);
    }


    boolean isErr();

    Option<E> err();

    default void ifErr(Consumer<? super E> consumer) {
        err().ifSome(consumer);
    }

    default void throwIfError() throws E {
        if(isErr()) {
            // noinspection ConstantConditions (get is safe because error is present if isErr returns true)
            throw err().get();
        }
    }

    default void throwUncheckedIfError() {
        if(isErr()) {
            // Get is safe because error is present if isErr returns true.
            throw new RuntimeException(err().get());
        }
    }


    default void ifElse(Consumer<? super T> okConsumer, Consumer<? super E> errConsumer) {
        ok().ifSome(okConsumer);
        err().ifSome(errConsumer);
    }


    default <U extends Serializable> Result<U, E> map(Function<? super T, ? extends U> mapper) {
        // noinspection unchecked (cast is safe because it is impossible to get a value of type U in the err case)
        return ok().map(v -> Result.<U, E>ofOk(mapper.apply(v))).unwrapOrElse(() -> (Result<U, E>)this);
    }

    default <U extends Serializable> U mapOr(U def, Function<? super T, ? extends U> mapper) {
        return ok().mapOr(def, mapper);
    }

    default <U extends @Nullable Serializable> @Nullable U mapOrNull(Function<? super T, ? extends U> mapper) {
        return ok().mapOrNull(mapper);
    }

    default <U extends Serializable> U mapOrThrow(Function<? super T, ? extends U> mapper) throws E {
        // Get is safe because error is present if not ok case.
        return ok().mapOrElseThrow(() -> err().get(), mapper);
    }

    default <U extends Serializable> U mapOrElse(Supplier<? extends U> def, Function<? super T, ? extends U> mapper) {
        return ok().mapOrElse(def, mapper);
    }

    default <U extends Serializable> U mapOrElse(Function<? super E, ? extends U> def, Function<? super T, ? extends U> mapper) {
        // Get is safe because error is present if not ok case.
        return ok().mapOrElse(() -> def.apply(err().get()), mapper);
    }

    default <U extends Serializable, F extends Throwable> U mapOrElseThrow(Function<? super E, ? extends F> def, Function<? super T, ? extends U> mapper) throws F {
        // Get is safe because error is present if not ok case.
        return ok().mapOrElseThrow(() -> def.apply(err().get()), mapper);
    }


    default <F extends Throwable> Result<T, F> mapErr(Function<? super E, ? extends F> mapper) {
        // noinspection unchecked (cast is safe because it is impossible to get a value of type F in the ok case)
        return err().map(e -> Result.<T, F>ofErr(mapper.apply(e))).unwrapOrElse(() -> (Result<T, F>)this);
    }

    default <F extends Throwable> F mapErrOr(F def, Function<? super E, ? extends F> mapper) {
        return err().mapOr(def, mapper);
    }

    default <F extends Throwable> @Nullable F mapErrOrNull(Function<? super E, ? extends F> mapper) {
        return err().mapOrNull(mapper);
    }

    default <F extends Throwable> F mapErrOrElse(Supplier<? extends F> def, Function<? super E, ? extends F> mapper) {
        return err().mapOrElse(def, mapper);
    }

    default <F extends Throwable> F mapErrOrElse(Function<? super T, ? extends F> def, Function<? super E, ? extends F> mapper) {
        // Get is safe because value is present if not err case.
        return err().mapOrElse(() -> def.apply(ok().get()), mapper);
    }


    default <U extends Serializable> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
        // noinspection unchecked (cast is safe because it is impossible to get a value of type U in the err case)
        return ok().map(mapper).unwrapOrElse(() -> (Result<U, E>)this);
    }


    default <U extends Serializable> Result<U, E> and(Result<U, E> other) {
        if(isErr()) {
            // noinspection unchecked (cast is safe because it is impossible to get a value of type U in the err case)
            return (Result<U, E>)this;
        }
        return other;
    }

    default <F extends Throwable> Result<T, F> or(Result<T, F> other) {
        if(isOk()) {
            // noinspection unchecked (cast is safe because it is impossible to get a value of type F in the ok case)
            return (Result<T, F>)this;
        }
        return other;
    }


    default T unwrap() throws E {
        // get is safe because error is present if not ok case
        return ok().unwrapOrElseThrow(() -> err().get());
    }

    default T unwrapUnchecked() {
        // get is safe because error is present if not ok case
        return ok().unwrapOrElseThrow(() -> new RuntimeException(err().get()));
    }

    default T unwrapOr(T def) {
        return ok().unwrapOr(def);
    }

    default T unwrapOrElse(Supplier<? extends T> def) {
        return ok().unwrapOrElse(def);
    }


    default E unwrapErr() {
        return err().unwrapOrElseThrow(() -> new RuntimeException("Called `unwrapErr` on an `Ok` result"));
    }

    default E unwrapErrOr(E def) {
        return err().unwrapOr(def);
    }

    default E unwrapErrOrElse(Supplier<? extends E> def) {
        return err().unwrapOrElse(def);
    }


    default @Nullable T get() {
        return ok().get();
    }

    default @Nullable T getOr(@Nullable T def) {
        return ok().getOr(def);
    }

    default @Nullable T getOrElse(Supplier<? extends @Nullable T> def) {
        return ok().getOrElse(def);
    }


    default @Nullable E getErr() {
        return err().get();
    }

    default @Nullable E getErrOr(@Nullable E def) {
        return err().getOr(def);
    }

    default @Nullable E getErrOrElse(Supplier<? extends @Nullable E> def) {
        return err().getOrElse(def);
    }


    class Ok<T extends Serializable, E extends Throwable> implements Result<T, E>, Serializable {
        public final T value;

        public Ok(T value) {
            this.value = value;
        }


        @Override public boolean isOk() {
            return true;
        }

        @Override public Option<T> ok() {
            return Option.ofSome(value);
        }

        @Override public boolean isErr() {
            return false;
        }

        @Override public Option<E> err() {
            return Option.ofNone();
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

        @Override public Option<T> ok() {
            return Option.ofNone();
        }

        @Override public boolean isErr() {
            return true;
        }

        @Override public Option<E> err() {
            return Option.ofSome(error);
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
}
