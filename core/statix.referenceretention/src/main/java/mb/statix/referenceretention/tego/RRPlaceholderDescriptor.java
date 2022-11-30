package mb.statix.referenceretention.tego;

import mb.nabl2.terms.ITerm;
import mb.statix.referenceretention.statix.LockedReference;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * A reference retention descriptor for a placeholder.
 */
public final class RRPlaceholderDescriptor {
    private final ITerm term;
    private final @Nullable ITerm context;

    /**
     * Gets the term inside the placeholder.
     * @return the term, which may include {@link LockedReference} terms.
     */
    public ITerm getTerm() {
        return term;
    }

    /**
     * Gets the context for the references inside the placeholder's AST.
     * @return the context, which is a term that describes the context reference,
     * such as {@code Var("x")} or {@code Member("x", Var("y"))}; or {@code null}
     */
    public @Nullable ITerm getContext() {
        return context;
    }

    /**
     * Initializes a new instance of the {@link RRPlaceholderDescriptor} class.
     * @param term the AST inside the placeholder
     * @param context the context for reference in the placeholder's AST; or {@code null}
     */
    public RRPlaceholderDescriptor(ITerm term, @Nullable ITerm context) {
        this.term = term;
        this.context = context;
    }

    @Override public boolean equals(Object other) {
        if(this == other) return true;
        if(!(other instanceof RRPlaceholderDescriptor)) return false;
        RRPlaceholderDescriptor that = (RRPlaceholderDescriptor)other;
        // @formatter:off
        return Objects.equals(this.getTerm(), that.getTerm())
            && Objects.equals(this.getContext(), that.getContext());
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, context);
    }

    @Override
    public String toString() {
        return "[[" + getTerm() + "|" + (getContext() != null ? getContext() : "") + "]]";
    }
}
