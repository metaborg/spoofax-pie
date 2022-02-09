package mb.spoofax.core.component;

public interface ParticipantCloseable extends AutoCloseable {
    @Override void close(); // Override with `throws Exception`.
}
