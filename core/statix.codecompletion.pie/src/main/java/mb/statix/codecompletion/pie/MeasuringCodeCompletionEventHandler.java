package mb.statix.codecompletion.pie;

/**
 * Measures the time between code completion events.
 */
public final class MeasuringCodeCompletionEventHandler extends CodeCompletionEventHandlerBase {

    private Long parseStartTime = Long.MIN_VALUE;
    private Long parseEndTime = Long.MIN_VALUE;
    private Long preparationStartTime = Long.MIN_VALUE;
    private Long preparationEndTime = Long.MIN_VALUE;
    private Long analysisStartTime = Long.MIN_VALUE;
    private Long analysisEndTime = Long.MIN_VALUE;
    private Long codeCompletionStartTime = Long.MIN_VALUE;
    private Long codeCompletionEndTime = Long.MIN_VALUE;
    private Long finishingStartTime = Long.MIN_VALUE;
    private Long finishingEndTime = Long.MIN_VALUE;
    private Long startTime = Long.MIN_VALUE;
    private Long endTime = Long.MIN_VALUE;

    public Long getParseTime() { return this.parseEndTime - this.parseStartTime; }
    public Long getPreparationTime() { return this.preparationEndTime - this.preparationStartTime; }
    public Long getAnalysisTime() { return this.analysisEndTime - this.analysisStartTime; }
    public Long getCodeCompletionTime() { return this.codeCompletionEndTime - this.codeCompletionStartTime; }
    public Long getFinishingTime() { return this.finishingEndTime - this.finishingStartTime; }
    public Long getTotalTime() { return this.endTime - this.startTime; }

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
