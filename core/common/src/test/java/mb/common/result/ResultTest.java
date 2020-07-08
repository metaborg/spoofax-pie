package mb.common.result;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    @Test void ofOk() {
        final Result<Integer, ?> ok = Result.ofOk(1);
        assertEquals(1, ok.unwrapUnchecked());
    }

    @Test void ofErr() {
        final Result<Integer, CustomException> err = Result.ofErr(newCustomException());
        assertThrows(CustomException.class, err::unwrap);
    }

    @Test void ofNullableOrElse() {
        final Result<Integer, Exception> ok = Result.ofNullableOrElse(1, ResultTest::newCustomException);
        assertEquals(1, ok.unwrapUnchecked());
        final Result<Integer, Exception> err = Result.ofNullableOrElse(null, ResultTest::newCustomException);
        assertThrowsCustomException(err::unwrap);
        assertIsCustomException(err.unwrapErr());
    }

    @Test void ofNullableOrExpect1() {
        final String expectMessage = "Expected 1 got null";
        final Result<Integer, ExpectException> ok = Result.ofNullableOrExpect(1, expectMessage);
        assertEquals(1, ok.unwrap());
        final Result<Integer, ExpectException> err = Result.ofNullableOrExpect(null, expectMessage);
        assertThrows(ExpectException.class, err::unwrap);
        assertEquals(expectMessage, err.unwrapErr().getMessage());
    }

    @Test void ofNullableOrExpect2() {
        final String expectMessage = "Expected 1 got null";
        final Throwable cause = new Throwable("Cause");
        final Result<Integer, ExpectException> ok = Result.ofNullableOrExpect(1, expectMessage, cause);
        assertEquals(1, ok.unwrap());
        final Result<Integer, ExpectException> err = Result.ofNullableOrExpect(null, expectMessage, cause);
        assertThrows(ExpectException.class, err::unwrap);
        assertEquals(expectMessage, err.unwrapErr().getMessage());
        assertEquals(cause.getMessage(), err.unwrapErr().getCause().getMessage());
    }

    @Test void ofNullableOrElseExpect1() {
        final String expectMessage = "Expected 1 got null";
        final Result<Integer, ExpectException> ok = Result.ofNullableOrElseExpect(1, () -> expectMessage);
        assertEquals(1, ok.unwrap());
        final Result<Integer, ExpectException> err = Result.ofNullableOrElseExpect(null, () -> expectMessage);
        assertThrows(ExpectException.class, err::unwrap);
        assertEquals(expectMessage, err.unwrapErr().getMessage());
    }

    @Test void ofNullableOrElseExpect2() {
        final String expectMessage = "Expected 1 got null";
        final Throwable cause = new Throwable("Cause");
        final Result<Integer, ExpectException> ok = Result.ofNullableOrElseExpect(1, () -> expectMessage, () -> cause);
        assertEquals(1, ok.unwrap());
        final Result<Integer, ExpectException> err = Result.ofNullableOrElseExpect(null, () -> expectMessage, () -> cause);
        assertThrows(ExpectException.class, err::unwrap);
        assertEquals(expectMessage, err.unwrapErr().getMessage());
        assertEquals(cause.getMessage(), err.unwrapErr().getCause().getMessage());
    }

    @Test void ofOkOrCatching1() {
        final Result<Integer, ?> ok = Result.ofOkOrCatching(() -> 1);
        assertEquals(1, ok.unwrapUnchecked());
        final Result<Integer, ?> err = Result.ofOkOrCatching(() -> {
            throw newCustomException();
        });
        assertThrowsCustomException(err::unwrap);
        assertIsCustomException(err.unwrapErr());

        // Do not catch RuntimeExceptions, Errors, nor sneakily thrown Throwables.
        assertThrowsCustomRuntimeException(() -> Result.ofOkOrCatching(() -> {
            throw newCustomRuntimeException();
        }));
        assertThrowsError(() -> Result.ofOkOrCatching(() -> {
            throw newError();
        }));
        assertThrowsCustomThrowable(() -> Result.ofOkOrCatching(() -> {
            sneakyThrow(newCustomThrowable());
            return 0;
        }));
    }

    @Test void ofOkOrCatching2() {
        final Result<Integer, CustomException> ok = Result.ofOkOrCatching(() -> 1, CustomException.class);
        assertEquals(1, ok.unwrapUnchecked());
        final Result<Integer, CustomException> err = Result.ofOkOrCatching(() -> {
            throw newCustomException();
        }, CustomException.class);
        assertThrows(CustomException.class, err::unwrap);
        assertIsCustomException(err.unwrapErr());

        // Do not catch RuntimeExceptions, Errors, sneakily thrown Throwables, nor sneakily thrown Exceptions of a different type than given to ofOkOrCatching.
        assertThrowsCustomRuntimeException(() -> Result.ofOkOrCatching(() -> {
            throw newCustomRuntimeException();
        }, CustomException.class));
        assertThrowsError(() -> Result.ofOkOrCatching(() -> {
            throw newError();
        }, CustomException.class));
        assertThrowsCustomThrowable(() -> Result.ofOkOrCatching(() -> {
            sneakyThrow(newCustomThrowable());
            return 0;
        }, CustomException.class));
        assertThrows(AnotherCustomException.class, () -> Result.ofOkOrCatching(() -> {
            sneakyThrow(newAnotherCustomException());
            return 0;
        }, CustomException.class));
    }

    @Test void isOk() {
        final Result<Integer, ?> ok = Result.ofOk(1);
        assertTrue(ok.isOk());
        final Result<Integer, ?> err = Result.ofErr(newCustomException());
        assertFalse(err.isOk());
    }

    @Test void ok() {
        final Result<Integer, ?> ok = Result.ofOk(1);
        assertTrue(ok.ok().isSome());
        assertEquals(1, ok.ok().get());
        final Result<Integer, ?> err = Result.ofErr(newCustomException());
        assertFalse(err.ok().isSome());
    }

    @Test void ifOk() {
        final Result<Integer, ?> ok = Result.ofOk(1);
        {
            final AtomicInteger value = new AtomicInteger(-1);
            ok.ifOk(value::set);
            assertEquals(1, value.intValue());
        }
        final Result<Integer, ?> err = Result.ofErr(newCustomException());
        {
            final AtomicInteger value = new AtomicInteger(-1);
            err.ifOk(value::set);
            assertEquals(-1, value.intValue());
        }
    }

    @Test void isErr() {
        final Result<Integer, ?> ok = Result.ofOk(1);
        assertFalse(ok.isErr());
        final Result<Integer, ?> err = Result.ofErr(newCustomException());
        assertTrue(err.isErr());
    }

    @Test void err() {
        final Result<Integer, ?> ok = Result.ofOk(1);
        assertFalse(ok.err().isSome());
        final Result<Integer, ?> err = Result.ofErr(newCustomException());
        assertTrue(err.err().isSome());
        assertIsCustomException(err.err().get());
    }

    @Test void ifErr() {
        final Result<Integer, CustomException> ok = Result.ofOk(1);
        {
            final AtomicReference<CustomException> exception = new AtomicReference<>();
            ok.ifErr(exception::set);
            assertNull(exception.get());
        }
        final Result<Integer, CustomException> err = Result.ofErr(newCustomException());
        {
            final AtomicReference<CustomException> exception = new AtomicReference<>();
            err.ifErr(exception::set);
            assertIsCustomException(exception.get());
        }
    }

    @Test
    void throwIfError() {
        // TODO
    }

    @Test
    void throwUncheckedIfError() {
        // TODO
    }

    @Test
    void ifElse() {
        // TODO
    }

    @Test
    void testIfElse() {
        // TODO
    }

    @Test
    void map() {
        // TODO
    }

    @Test
    void mapThrowing() {
        // TODO
    }

    @Test void mapCatching1Ok() {
        final Result<Integer, CustomException> ok = Result.ofOk(1);
        final Result<Integer, ?> okToOk = ok.mapCatching((value) -> value + 1);
        assertEquals(2, okToOk.unwrapUnchecked());
        final Result<Integer, ?> okToErr = ok.mapCatching((value) -> {
            throw newCustomException();
        });
        assertThrowsCustomException(okToErr::unwrap);
        assertIsCustomException(okToErr.unwrapErr());

        // Do not catch RuntimeExceptions, Errors, nor sneakily thrown Throwables.
        assertThrowsCustomRuntimeException(() -> ok.mapCatching((value) -> {
            throw newCustomRuntimeException();
        }));
        assertThrowsError(() -> ok.mapCatching((value) -> {
            throw newError();
        }));
        assertThrowsCustomThrowable(() -> ok.mapCatching((value) -> {
            sneakyThrow(newCustomThrowable());
            return 0;
        }));
    }

    @Test void mapCatching1Err() {
        final Result<Integer, CustomException> err = Result.ofErr(newCustomException());
        final Result<Integer, ?> errToOk = err.mapCatching((value) -> value + 1);
        assertThrowsCustomException(errToOk::unwrap);
        assertIsCustomException(errToOk.unwrapErr());
        final Result<Integer, ?> errToErr = err.mapCatching((value) -> {
            throw newAnotherCustomException();
        });
        assertThrowsCustomException(errToErr::unwrap);
        assertIsCustomException(errToErr.unwrapErr());
    }

    @Test void mapCatching2Ok() {
        final Result<Integer, CustomException> ok = Result.ofOk(1);
        final Result<Integer, CustomException> okToOk = ok.mapCatching((value) -> value + 1, CustomException.class);
        assertEquals(2, okToOk.unwrapUnchecked());
        final Result<Integer, CustomException> okToErr = ok.mapCatching((value) -> {
            throw newCustomException();
        }, CustomException.class);
        assertThrowsCustomException(okToErr::unwrap);
        assertIsCustomException(okToErr.unwrapErr());

        // Do not catch RuntimeExceptions, Errors, sneakily thrown Throwables, nor sneakily thrown Exceptions of a different type than given to ofOkOrCatching.
        assertThrowsCustomRuntimeException(() -> ok.mapCatching((value) -> {
            throw newCustomRuntimeException();
        }, CustomException.class));
        assertThrowsError(() -> ok.mapCatching((value) -> {
            throw newError();
        }, CustomException.class));
        assertThrowsCustomThrowable(() -> ok.mapCatching((value) -> {
            sneakyThrow(newCustomThrowable());
            return 0;
        }, CustomException.class));
        assertThrows(AnotherCustomException.class, () -> ok.mapCatching((value) -> {
            sneakyThrow(newAnotherCustomException());
            return 0;
        }, CustomException.class));
    }

    @Test void mapCatching2Err() {
        final Result<Integer, CustomException> err = Result.ofErr(newCustomException());
        final Result<Integer, CustomException> errToOk = err.mapCatching((value) -> value + 1, CustomException.class);
        assertThrowsCustomException(errToOk::unwrap);
        assertIsCustomException(errToOk.unwrapErr());
        final Result<Integer, CustomException> errToErr = err.mapCatching((value) -> {
            throw newCustomException(2); // Different exception value.
        }, CustomException.class);
        assertThrowsCustomException(errToErr::unwrap);
        assertIsCustomException(errToErr.unwrapErr());
    }

    @Test
    void mapOr() {
        // TODO
    }

    @Test
    void mapOrNull() {
        // TODO
    }

    @Test
    void mapOrThrow() {
        // TODO
    }

    @Test
    void mapOrElse1() {
        // TODO
    }

    @Test
    void mapOrElse2() {
        // TODO
    }

    @Test
    void mapOrElseThrow() {
        // TODO
    }

    @Test
    void mapErr() {
        // TODO
    }

    @Test
    void mapErrOr() {
        // TODO
    }

    @Test
    void mapErrOrNull() {
        // TODO
    }

    @Test
    void mapErrOrElse1() {
        // TODO
    }

    @Test
    void mapErrOrElse2() {
        // TODO
    }

    @Test
    void flatMap() {
        // TODO
    }

    @Test
    void flatMapOrElse() {
        // TODO
    }

    @Test
    void and() {
        // TODO
    }

    @Test
    void or() {
        // TODO
    }

    @Test
    void unwrap() {
        // TODO
    }

    @Test
    void unwrapUnchecked() {
        // TODO
    }

    @Test
    void unwrapOr() {
        // TODO
    }

    @Test
    void unwrapOrElse() {
        // TODO
    }

    @Test
    void expect1() {
        // TODO
    }

    @Test
    void expect2() {
        // TODO
    }

    @Test
    void expect3() {
        // TODO
    }

    @Test
    void unwrapErr() {
        // TODO
    }

    @Test
    void unwrapErrOr() {
        // TODO
    }

    @Test
    void unwrapErrOrElse() {
        // TODO
    }

    @Test
    void expectErr1() {
        // TODO
    }

    @Test
    void expectErr2() {
        // TODO
    }

    @Test
    void get() {
        // TODO
    }

    @Test
    void getOr() {
        // TODO
    }

    @Test
    void getOrElse() {
        // TODO
    }

    @Test
    void getErr() {
        // TODO
    }

    @Test
    void getErrOr() {
        // TODO
    }

    @Test
    void getErrOrElse() {
        // TODO
    }


    static final String customThrowableMessage = "This is a custom throwable";

    static class CustomThrowable extends Throwable {
        CustomThrowable(Throwable cause) {
            super(cause);
        }

        @Override public String getMessage() {
            return customThrowableMessage;
        }
    }

    static CustomThrowable newCustomThrowable() {
        return new CustomThrowable(newCause());
    }

    static void assertThrowsCustomThrowable(Executable executable) {
        assertThrows(CustomThrowable.class, executable);
    }

    static void assertIsCustomThrowable(Throwable ex) {
        assertEquals(CustomThrowable.class, ex.getClass());
        assertIsCustomThrowable((CustomThrowable)ex);
    }

    static void assertIsCustomThrowable(CustomThrowable ex) {
        assertEquals(customThrowableMessage, ex.getMessage());
        assertCause(ex.getCause());
    }


    static final int customExceptionDefaultValue = Integer.MIN_VALUE;
    static final String customExceptionMessage = "This is a custom exception";

    static class CustomException extends Exception {
        final int value;

        CustomException(int value, Throwable cause) {
            super(cause);
            this.value = value;
        }

        @Override public String getMessage() {
            return customExceptionMessage;
        }
    }

    static CustomException newCustomException() {
        return newCustomException(customExceptionDefaultValue);
    }

    static CustomException newCustomException(int value) {
        return new CustomException(value, newCause());
    }

    static void assertThrowsCustomException(Executable executable) {
        assertThrowsCustomException(executable, customExceptionDefaultValue);
    }

    static void assertThrowsCustomException(Executable executable, int value) {
        final CustomException exception = assertThrows(CustomException.class, executable);
        assertIsCustomException(exception, value);
    }

    static void assertIsCustomException(@Nullable Throwable ex) {
        assertIsCustomException(ex, customExceptionDefaultValue);
    }

    static void assertIsCustomException(@Nullable Throwable ex, int value) {
        assertNotNull(ex);
        assertEquals(CustomException.class, ex.getClass());
        assertIsCustomException((CustomException)ex, value);
    }

    static void assertIsCustomException(@Nullable CustomException ex) {
        assertIsCustomException(ex, customExceptionDefaultValue);
    }

    static void assertIsCustomException(@Nullable CustomException ex, int value) {
        assertNotNull(ex);
        assertEquals(customExceptionMessage, ex.getMessage());
        assertCause(ex.getCause());
        assertEquals(value, ex.value);
    }


    static final String anotherCustomExceptionMessage = "This is another custom exception";

    static class AnotherCustomException extends Exception {
        AnotherCustomException(Throwable cause) {
            super(cause);
        }

        @Override public String getMessage() {
            return anotherCustomExceptionMessage;
        }
    }

    static AnotherCustomException newAnotherCustomException() {
        return new AnotherCustomException(newCause());
    }

    static void assertThrowsAnotherCustomException(Executable executable) {
        assertThrows(AnotherCustomException.class, executable);
    }

    static void assertIsAnotherCustomException(@Nullable Throwable ex) {
        assertNotNull(ex);
        assertEquals(AnotherCustomException.class, ex.getClass());
        assertIsAnotherCustomException((AnotherCustomException)ex);
    }

    static void assertIsAnotherCustomException(@Nullable AnotherCustomException ex) {
        assertNotNull(ex);
        assertEquals(anotherCustomExceptionMessage, ex.getMessage());
        assertCause(ex.getCause());
    }


    static final String causeExceptionMessage = "This is a cause exception";

    static class CauseException extends Exception {
        @Override public String getMessage() {
            return causeExceptionMessage;
        }
    }

    static CauseException newCause() {
        return new CauseException();
    }

    static void assertCause(Throwable ex) {
        assertEquals(CauseException.class, ex.getClass());
        assertCause((CauseException)ex);
    }

    static void assertCause(CauseException ex) {
        assertEquals(causeExceptionMessage, ex.getMessage());
    }


    static final String runtimeExceptionMessage = "This is a custom runtime exception";

    static class CustomRuntimeException extends RuntimeException {
        @Override public String getMessage() {
            return customExceptionMessage;
        }
    }

    static CustomRuntimeException newCustomRuntimeException() {
        return new CustomRuntimeException();
    }

    static void assertThrowsCustomRuntimeException(Executable executable) {
        assertThrows(CustomRuntimeException.class, executable);
    }

    static void assertIsCustomRuntimeException(Throwable ex) {
        assertEquals(CustomRuntimeException.class, ex.getClass());
        assertIsCustomRuntimeException((CustomRuntimeException)ex);
    }

    static void assertIsCustomRuntimeException(CustomRuntimeException ex) {
        assertEquals(runtimeExceptionMessage, ex.getMessage());
    }


    static final String errorMessage = "This is a custom error";

    static class CustomError extends Error {
        @Override public String getMessage() {
            return errorMessage;
        }
    }

    static CustomError newError() {
        return new CustomError();
    }

    static void assertThrowsError(Executable executable) {
        assertThrows(CustomError.class, executable);
    }

    static void assertIsError(Throwable ex) {
        assertEquals(CustomError.class, ex.getClass());
        assertIsError((CustomError)ex);
    }

    static void assertIsError(CustomError ex) {
        assertEquals(errorMessage, ex.getMessage());
    }


    @SuppressWarnings("unchecked") static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T)t;
    }
}
