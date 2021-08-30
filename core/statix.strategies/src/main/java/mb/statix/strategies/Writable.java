package mb.statix.strategies;

/**
 * An object whose string representation can be written to a {@link StringBuilder}.
 */
public interface Writable {

    /**
     * Writes this object to the given {@link StringBuilder}.
     *
     * @param sb the {@link StringBuilder} to write to
     * @return the buffer
     */
    @SuppressWarnings("UnnecessaryToStringCall")
    default StringBuilder writeTo(StringBuilder sb) {
        sb.append(this.toString());
        return sb;
    }

}
