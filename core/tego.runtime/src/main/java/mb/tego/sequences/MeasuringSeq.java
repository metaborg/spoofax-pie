package mb.tego.sequences;

import java.util.function.BiConsumer;

/**
 * Sequence that reports the time taken by each computation.
 *
 * @param <T> the type of elements in the sequence
 */
public final class MeasuringSeq<T> extends SeqBase<T> {

    private final Seq<T> wrappedSeq;
    private final BiConsumer<Long, Boolean> timeReporter;

    /**
     * Initializes a new instance of the {@link MeasuringSeq} class.
     *
     * @param wrappedSeq the sequence being wrapped
     * @param timeReporter function to which each computation is reported:
     *                     the elapsed time, in nanoseconds; and whether we reached the end of the sequence {@code true}
     *                     or not {@code false}
     */
    public MeasuringSeq(Seq<T> wrappedSeq, BiConsumer<Long, Boolean> timeReporter) {
        this.wrappedSeq = wrappedSeq;
        this.timeReporter = timeReporter;
    }

    @Override
    protected void computeNext() throws InterruptedException {
        boolean finished = false;
        long startTime = System.nanoTime();
        try {
            if(wrappedSeq.next()) {
                this.yield(wrappedSeq.getCurrent());
            } else {
                this.yieldBreak();
                finished = true;
            }
        } catch (Throwable ex) {
            finished = true;
            throw ex;
        } finally {
            long endTime = System.nanoTime();
            timeReporter.accept(endTime - startTime, finished);
        }
    }

    @Override
    public void close() throws Exception {
        wrappedSeq.close();
    }
}
