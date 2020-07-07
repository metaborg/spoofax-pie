package mb.common.result;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    static final String customExceptionMessage = "This is a custom exception";

    static class CustomException extends Exception {
        CustomException(Throwable cause) {
            super(cause);
        }

        @Override public String getMessage() {
            return customExceptionMessage;
        }
    }

    static CustomException ex() {
        return new CustomException(cause());
    }

    static void assertThrowsEx(Executable executable) {
        assertThrows(CustomException.class, executable);
    }

    static void assertEx(Throwable ex) {
        assertEquals(CustomException.class, ex.getClass());
        assertEx((CustomException)ex);
    }

    static void assertEx(CustomException ex) {
        assertEquals(customExceptionMessage, ex.getMessage());
        assertCause(ex.getCause());
    }


    static final String causeExceptionMessage = "This is a cause exception";

    static class CauseException extends Exception {
        @Override public String getMessage() {
            return causeExceptionMessage;
        }
    }

    static CauseException cause() {
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

    static CustomRuntimeException rex() {
        return new CustomRuntimeException();
    }

    static void assertThrowsRex(Executable executable) {
        assertThrows(CustomRuntimeException.class, executable);
    }

    static void assertRex(Throwable ex) {
        assertEquals(CustomRuntimeException.class, ex.getClass());
        assertRex((CustomRuntimeException)ex);
    }

    static void assertRex(CustomRuntimeException ex) {
        assertEquals(runtimeExceptionMessage, ex.getMessage());
    }


    @Test void ofOk() {
        final Result<Integer, ?> ok = Result.ofOk(1);
        assertEquals(1, ok.unwrapUnchecked());
    }

    @Test void ofErr() {
        final Result<Integer, CustomException> err = Result.ofErr(ex());
        assertThrows(CustomException.class, err::unwrap);
    }

    @Test void ofNullableOrElse() {
        final Result<Integer, Exception> ok = Result.ofNullableOrElse(1, ResultTest::ex);
        assertEquals(1, ok.unwrapUnchecked());
        final Result<Integer, Exception> err = Result.ofNullableOrElse(null, ResultTest::ex);
        assertThrowsEx(err::unwrap);
        assertEx(err.unwrapErr());
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

    @Test
    void ofOkOrCatching1() {
        final Result<Integer, ?> ok = Result.ofOkOrCatching(() -> 1);
        assertEquals(1, ok.unwrapUnchecked());
        final Result<Integer, ?> err = Result.ofOkOrCatching(() -> {
            throw ex();
        });
        assertThrowsEx(err::unwrap);
        assertEx(err.unwrapErr());

        // Assert that RuntimeException is not caught by ofOkOrCatching.
        assertThrows(RuntimeException.class, () -> Result.ofOkOrCatching(() -> {
            throw rex();
        }));

        // TODO: Assert that Error is not caught by ofOkOrCatching.
        // TODO: Assert that Throwable is not caught by ofOkOrCatching.
    }

    @Test
    void ofOkOrCatching2() {
        final Result<Integer, CustomException> ok = Result.ofOkOrCatching(() -> 1, CustomException.class);
        assertEquals(1, ok.unwrapUnchecked());
        final Result<Integer, CustomException> err = Result.ofOkOrCatching(() -> {
            throw ex();
        }, CustomException.class);
        assertThrows(CustomException.class, err::unwrap);
        assertEx(err.unwrapErr());

        // Assert that RuntimeException is not caught by ofOkOrCatching.
        assertThrows(CustomRuntimeException.class, () -> Result.ofOkOrCatching(() -> {
            throw rex();
        }, CustomException.class));

        // TODO: Assert that Error is not caught by ofOkOrCatching.
        // TODO: Assert that Throwable is not caught by ofOkOrCatching.
    }

    @Test
    void isOk() {
    }

    @Test
    void ok() {
    }

    @Test
    void ifOk() {
    }

    @Test
    void isErr() {
    }

    @Test
    void err() {
    }

    @Test
    void ifErr() {
    }

    @Test
    void throwIfError() {
    }

    @Test
    void throwUncheckedIfError() {
    }

    @Test
    void ifElse() {
    }

    @Test
    void testIfElse() {
    }

    @Test
    void map() {
    }

    @Test
    void mapThrowing() {
    }

    @Test
    void mapCatching() {
    }

    @Test
    void testMapCatching() {
    }

    @Test
    void mapOr() {
    }

    @Test
    void mapOrNull() {
    }

    @Test
    void mapOrThrow() {
    }

    @Test
    void mapOrElse() {
    }

    @Test
    void testMapOrElse() {
    }

    @Test
    void mapOrElseThrow() {
    }

    @Test
    void mapErr() {
    }

    @Test
    void mapErrOr() {
    }

    @Test
    void mapErrOrNull() {
    }

    @Test
    void mapErrOrElse() {
    }

    @Test
    void testMapErrOrElse() {
    }

    @Test
    void flatMap() {
    }

    @Test
    void flatMapOrElse() {
    }

    @Test
    void and() {
    }

    @Test
    void or() {
    }

    @Test
    void unwrap() {
    }

    @Test
    void unwrapUnchecked() {
    }

    @Test
    void unwrapOr() {
    }

    @Test
    void unwrapOrElse() {
    }

    @Test
    void expect() {
    }

    @Test
    void testExpect() {
    }

    @Test
    void testExpect1() {
    }

    @Test
    void unwrapErr() {
    }

    @Test
    void unwrapErrOr() {
    }

    @Test
    void unwrapErrOrElse() {
    }

    @Test
    void expectErr() {
    }

    @Test
    void testExpectErr() {
    }

    @Test
    void get() {
    }

    @Test
    void getOr() {
    }

    @Test
    void getOrElse() {
    }

    @Test
    void getErr() {
    }

    @Test
    void getErrOr() {
    }

    @Test
    void getErrOrElse() {
    }
}
