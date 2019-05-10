package mb.spoofax.intellij.editor;

public final class NullCancellationToken implements ICancellationToken {

    public static final NullCancellationToken INSTANCE = new NullCancellationToken();

    private NullCancellationToken() { }

    @Override
    public boolean isCancelled() {
        return false;
    }

}
