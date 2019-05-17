package mb.spoofax.intellij;

/**
 * A cancellation token.
 *
 * An instance of this interface is passed to functions whose operation can be cancelled.
 */
public interface ICancellationToken {

    /**
     * Gets whether the operation has been cancelled.
     *
     * @return {@code true} when the operation has been cancelled;
     * otherwise, {@code false}.
     */
    boolean isCancelled();
}
