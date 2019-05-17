package mb.spoofax.intellij;

/**
 * A null cancellation token.
 *
 * The null cancellation token is never cancelled,
 * and can be used as a cancellation token wherever cancellation is not possible.
 */
public final class NullCancellationToken implements ICancellationToken {

    /**
     * The singleton instance of this class.
     */
    public static final NullCancellationToken INSTANCE = new NullCancellationToken();

    /**
     * Instantiates a new instance of the {@link NullCancellationToken} class.
     */
    private NullCancellationToken() { }

    @Override
    public boolean isCancelled() {
        return false;
    }

}
