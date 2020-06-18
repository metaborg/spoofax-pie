package mb.common.result;

final class SneakyThrow {
    private SneakyThrow() { }

    @SuppressWarnings("unchecked") static <T extends Throwable> void doThrow(Throwable t) throws T {
        throw (T)t;
    }
}
