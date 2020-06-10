package mb.common.util;

public final class SneakyThrow {
    private SneakyThrow() {    }

    @SuppressWarnings("unchecked") public static <T extends Throwable> void doThrow(Throwable t) throws T {
        throw (T)t;
    }
}
