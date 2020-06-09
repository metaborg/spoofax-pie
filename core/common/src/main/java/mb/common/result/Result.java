package mb.common.result;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<T extends Serializable, E extends Error> extends Serializable {
    class Ok<T extends Serializable, E extends Error> implements Result<T, E>, Serializable {
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

    class Err<T extends Serializable, E extends Error> implements Result<T, E>, Serializable {
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


    static <T extends Serializable, E extends Error> Result<T, E> ofOk(T value) {
        return new Ok<>(value);
    }

    static <T extends Serializable, E extends Error> Result<T, E> ofErr(E error) {
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

    default <F extends Error> Result<T, F> mapErr(Function<E, F> mapper) {
        // noinspection unchecked (cast is safe because it is impossible to get a value of type F in the ok case)
        return err().map(e -> Result.<T, F>ofErr(mapper.apply(e))).orElse((Result<T, F>)this);
    }

    default <F extends Error> F mapErrOr(F def, Function<E, F> mapper) {
        return err().map(mapper).orElse(def);
    }

    default <F extends Error> F mapErrOrElse(Supplier<F> def, Function<E, F> mapper) {
        return err().map(mapper).orElseGet(def);
    }

    default <R extends Serializable> R mapRes(Function<T, R> valueMapper, Function<E, R> errorMapper) {
        //noinspection OptionalGetWithoutIsPresent (get is safe because error is present if not ok case)
        return ok().map(valueMapper).orElse(errorMapper.apply(err().get()));
    }


    default <U extends Serializable> Result<U, E> andThen(Function<T, Result<U, E>> mapper) {
        // noinspection unchecked (cast is safe because it is impossible to get a value of type U in the err case)
        return ok().map(mapper).orElse((Result<U, E>)this);
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


//    default @Nullable T unwrap() {
//        return unwrapOr(null);
//    }
//
//    default @Nullable T unwrapOr(@Nullable T def) {
//        return ok().orElse(def);
//    }
//
//    default @Nullable T unwrapOrElse(Supplier<@Nullable T> def) {
//        return ok().orElseGet(def);
//    }
//
//    default @Nullable E unwrapErr() {
//        return unwrapErrOr(null);
//    }
//
//    default @Nullable E unwrapErrOr(@Nullable E def) {
//        return err().orElse(def);
//    }
//
//    default @Nullable E unwrapErrOrElse(Supplier<@Nullable E> def) {
//        return err().orElseGet(def);
//    }
}
