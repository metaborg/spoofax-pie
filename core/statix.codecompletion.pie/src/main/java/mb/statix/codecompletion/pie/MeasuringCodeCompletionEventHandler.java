package mb.statix.codecompletion.pie;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Measures the time between code completion events.
 */
public final class MeasuringCodeCompletionEventHandler extends CodeCompletionEventHandlerBase {

    private long parseStartTime = Long.MIN_VALUE;
    private long parseEndTime = Long.MIN_VALUE;
    private long preparationStartTime = Long.MIN_VALUE;
    private long preparationEndTime = Long.MIN_VALUE;
    private long analysisStartTime = Long.MIN_VALUE;
    private long analysisEndTime = Long.MIN_VALUE;
    private long codeCompletionStartTime = Long.MIN_VALUE;
    private long codeCompletionEndTime = Long.MIN_VALUE;
    private long finishingStartTime = Long.MIN_VALUE;
    private long finishingEndTime = Long.MIN_VALUE;
    private long startTime = Long.MIN_VALUE;
    private long endTime = Long.MIN_VALUE;

    /** The parsing time, in nanoseconds. */
    public @Nullable Long getParseTime() {
        if (this.parseEndTime != Long.MIN_VALUE && this.parseStartTime != Long.MIN_VALUE) return this.parseEndTime - this.parseStartTime; else return null;
    }
    /** The preparation time, in nanoseconds. */
    public @Nullable Long getPreparationTime() {
        if (this.preparationEndTime != Long.MIN_VALUE && this.preparationStartTime != Long.MIN_VALUE) return this.preparationEndTime - this.preparationStartTime; else return null;
    }
    /** The analysis time, in nanoseconds. */
    public @Nullable Long getAnalysisTime() {
        if (this.analysisEndTime != Long.MIN_VALUE && this.analysisStartTime != Long.MIN_VALUE) return this.analysisEndTime - this.analysisStartTime; else return null;
    }
    /** The completion time, in nanoseconds. */
    public @Nullable Long getCodeCompletionTime() {
        if (this.codeCompletionEndTime != Long.MIN_VALUE && this.codeCompletionStartTime != Long.MIN_VALUE) return this.codeCompletionEndTime - this.codeCompletionStartTime; else return null;
    }
    /** The finishing time, in nanoseconds. */
    public @Nullable Long getFinishingTime() {
        if (this.finishingEndTime != Long.MIN_VALUE && this.finishingStartTime != Long.MIN_VALUE) return this.finishingEndTime - this.finishingStartTime; else return null;
    }
    /** The total time, in nanoseconds. */
    public @Nullable Long getTotalTime() {
        if (this.endTime != Long.MIN_VALUE && this.startTime != Long.MIN_VALUE) return this.endTime - this.startTime; else return null;
    }

    @Override
    public void begin() {
        super.begin();
        if (this.startTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.startTime = System.nanoTime();
    }

    @Override
    public void beginParse() {
        super.beginParse();
        if (this.parseStartTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.parseStartTime = System.nanoTime();
    }

    @Override
    public void endParse() {
        super.endParse();
        if (this.parseEndTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.parseEndTime = System.nanoTime();
    }

    @Override
    public void beginPreparation() {
        super.beginPreparation();
        if (this.preparationStartTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.preparationStartTime = System.nanoTime();
    }

    @Override
    public void endPreparation() {
        super.endPreparation();
        if (this.preparationEndTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.preparationEndTime = System.nanoTime();
    }

    @Override
    public void beginAnalysis() {
        super.beginAnalysis();
        if (this.analysisStartTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.analysisStartTime = System.nanoTime();
    }

    @Override
    public void endAnalysis() {
        super.endAnalysis();
        if (this.analysisEndTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.analysisEndTime = System.nanoTime();
    }

    @Override
    public void beginCodeCompletion() {
        super.beginCodeCompletion();
        if (this.codeCompletionStartTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.codeCompletionStartTime = System.nanoTime();
    }

    @Override
    public void endCodeCompletion() {
        super.endCodeCompletion();
        if (this.codeCompletionEndTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.codeCompletionEndTime = System.nanoTime();
    }

    @Override
    public void beginFinishing() {
        super.beginFinishing();
        if (this.finishingStartTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.finishingStartTime = System.nanoTime();
    }

    @Override
    public void endFinishing() {
        super.endFinishing();
        if (this.finishingEndTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.finishingEndTime = System.nanoTime();
    }

    @Override
    public void end() {
        super.end();
        if (this.endTime != Long.MIN_VALUE) throw new IllegalStateException("Measuring same event twice.");
        this.endTime = System.nanoTime();
    }
}
