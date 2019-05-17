package mb.spoofax.intellij;

import com.intellij.openapi.progress.ProgressIndicator;


/**
 * A cancellation token built from an IntelliJ progress indicator.
 */
public final class ProgressCancellationToken implements ICancellationToken {

    private final ProgressIndicator indicator;

    /**
     * Initializes a new instance of the {@link ProgressCancellationToken} class.
     *
     * @param indicator The IntelliJ progress indicator to wrap.
     */
    public ProgressCancellationToken(ProgressIndicator indicator) {
        this.indicator = indicator;
    }

    /**
     * Gets the IntelliJ progress indicator.
     *
     * @return The progress indicator.
     */
    public ProgressIndicator getIndicator() { return this.indicator; }

    @Override
    public boolean isCancelled() {
        return this.indicator.isCanceled();
    }

}
