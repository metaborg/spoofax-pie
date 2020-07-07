package mb.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    private static final String exceptionMessage = "Test";

    private static ExpectException ex() {
        return new ExpectException(exceptionMessage, cause());
    }

    private static void assertEx(Throwable ex) {
        assertEquals(ExpectException.class, ex.getClass());
        assertEx((ExpectException)ex);
    }

    private static void assertEx(ExpectException ex) {
        assertEquals(exceptionMessage, ex.getMessage());
        assertEquals(ExpectException.class, ex.getCause().getClass());
        assertCause((ExpectException)ex.getCause());
    }

    private static void assertExWithoutCause(ExpectException ex) {
        assertEquals(exceptionMessage, ex.getMessage());
    }

    private static final String causeMessage = "Cause";

    private static ExpectException cause() {
        return new ExpectException(causeMessage);
    }

    private static void assertCause(ExpectException cause) {
        assertEquals(causeMessage, cause.getMessage());
    }


    @Test void ofOk() {
        final Result<Integer, ExpectException> ok = Result.ofOk(1);
        assertEquals(1, ok.unwrap());
    }

    @Test void ofErr() {
        final Result<Integer, ExpectException> err = Result.ofErr(ex());
        assertThrows(ExpectException.class, err::unwrap);
    }

    @Test void ofNullableOrElse() {
        final Result<Integer, ExpectException> ok = Result.ofNullableOrElse(1, ResultTest::ex);
        assertEquals(1, ok.unwrap());
        final Result<Integer, ExpectException> err = Result.ofNullableOrElse(null, ResultTest::ex);
        assertThrows(ExpectException.class, err::unwrap);
        assertEx(err.unwrapErr());
    }

    @Test void ofNullableOrExpect1() {
        final Result<Integer, ExpectException> ok = Result.ofNullableOrExpect(1, exceptionMessage);
        assertEquals(1, ok.unwrap());
        final Result<Integer, ExpectException> err = Result.ofNullableOrExpect(null, exceptionMessage);
        assertThrows(ExpectException.class, err::unwrap);
        assertExWithoutCause(err.unwrapErr());
    }

    @Test void ofNullableOrExpect2() {
        final Result<Integer, ExpectException> ok = Result.ofNullableOrExpect(1, exceptionMessage, cause());
        assertEquals(1, ok.unwrap());
        final Result<Integer, ExpectException> err = Result.ofNullableOrExpect(null, exceptionMessage, cause());
        assertThrows(ExpectException.class, err::unwrap);
        assertEx(err.unwrapErr());
    }

    @Test void ofNullableOrElseExpect1() {
        final Result<Integer, ExpectException> ok = Result.ofNullableOrElseExpect(1, () -> exceptionMessage);
        assertEquals(1, ok.unwrap());
        final Result<Integer, ExpectException> err = Result.ofNullableOrElseExpect(null, () -> exceptionMessage);
        assertThrows(ExpectException.class, err::unwrap);
        assertExWithoutCause(err.unwrapErr());
    }

    @Test void ofNullableOrElseExpect2() {
        final Result<Integer, ExpectException> ok = Result.ofNullableOrElseExpect(1, () -> exceptionMessage, ResultTest::cause);
        assertEquals(1, ok.unwrap());
        final Result<Integer, ExpectException> err = Result.ofNullableOrElseExpect(null, () -> exceptionMessage, ResultTest::cause);
        assertThrows(ExpectException.class, err::unwrap);
        assertEx(err.unwrapErr());
    }

    @Test
    void ofOkOrCatching1() {
        final Result<Integer, ?> ok = Result.ofOkOrCatching(() -> 1);
        assertEquals(1, ok.unwrapUnchecked());
        final Result<Integer, ?> err = Result.ofOkOrCatching(() -> {
            throw ex();
        });
        assertThrows(ExpectException.class, err::unwrap);
        assertEx(err.unwrapErr());
    }

//    @Test
//    void ofOkOrCatching2() {
//    }
//
//    @Test
//    void isOk() {
//    }
//
//    @Test
//    void ok() {
//    }
//
//    @Test
//    void ifOk() {
//    }
//
//    @Test
//    void isErr() {
//    }
//
//    @Test
//    void err() {
//    }
//
//    @Test
//    void ifErr() {
//    }
//
//    @Test
//    void throwIfError() {
//    }
//
//    @Test
//    void throwUncheckedIfError() {
//    }
//
//    @Test
//    void ifElse() {
//    }
//
//    @Test
//    void testIfElse() {
//    }
//
//    @Test
//    void map() {
//    }
//
//    @Test
//    void mapThrowing() {
//    }
//
//    @Test
//    void mapCatching() {
//    }
//
//    @Test
//    void testMapCatching() {
//    }
//
//    @Test
//    void mapOr() {
//    }
//
//    @Test
//    void mapOrNull() {
//    }
//
//    @Test
//    void mapOrThrow() {
//    }
//
//    @Test
//    void mapOrElse() {
//    }
//
//    @Test
//    void testMapOrElse() {
//    }
//
//    @Test
//    void mapOrElseThrow() {
//    }
//
//    @Test
//    void mapErr() {
//    }
//
//    @Test
//    void mapErrOr() {
//    }
//
//    @Test
//    void mapErrOrNull() {
//    }
//
//    @Test
//    void mapErrOrElse() {
//    }
//
//    @Test
//    void testMapErrOrElse() {
//    }
//
//    @Test
//    void flatMap() {
//    }
//
//    @Test
//    void flatMapOrElse() {
//    }
//
//    @Test
//    void and() {
//    }
//
//    @Test
//    void or() {
//    }
//
//    @Test
//    void unwrap() {
//    }
//
//    @Test
//    void unwrapUnchecked() {
//    }
//
//    @Test
//    void unwrapOr() {
//    }
//
//    @Test
//    void unwrapOrElse() {
//    }
//
//    @Test
//    void expect() {
//    }
//
//    @Test
//    void testExpect() {
//    }
//
//    @Test
//    void testExpect1() {
//    }
//
//    @Test
//    void unwrapErr() {
//    }
//
//    @Test
//    void unwrapErrOr() {
//    }
//
//    @Test
//    void unwrapErrOrElse() {
//    }
//
//    @Test
//    void expectErr() {
//    }
//
//    @Test
//    void testExpectErr() {
//    }
//
//    @Test
//    void get() {
//    }
//
//    @Test
//    void getOr() {
//    }
//
//    @Test
//    void getOrElse() {
//    }
//
//    @Test
//    void getErr() {
//    }
//
//    @Test
//    void getErrOr() {
//    }
//
//    @Test
//    void getErrOrElse() {
//    }
}
