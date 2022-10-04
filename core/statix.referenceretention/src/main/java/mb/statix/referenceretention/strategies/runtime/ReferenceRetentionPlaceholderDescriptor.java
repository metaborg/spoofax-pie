package mb.statix.referenceretention.strategies.runtime;

import mb.nabl2.terms.ITerm;

import java.util.Objects;

/**
 * A reference retention descriptor for a placeholder.
 */
public final class ReferenceRetentionPlaceholderDescriptor {
    private final ITerm ast;
    private final ITerm context;

    /**
     * Gets the AST inside the placeholder.
     * @return the AST, which may include {@link LockedReference} terms.
     */
    public ITerm getAst() {
        return ast;
    }

    /**
     * Gets the context for the references inside the placeholder's AST.
     * @return the context, which is a term that describes the context reference,
     * such as {@code Var("x")} or {@code Member("x", Var("y"))}.
     */
    public ITerm getContext() {
        return context;
    }

    /**
     * Initializes a new instance of the {@link ReferenceRetentionPlaceholderDescriptor} class.
     * @param ast the AST inside the placeholder
     * @param context the context for reference in the placeholder's AST
     */
    public ReferenceRetentionPlaceholderDescriptor(ITerm ast, ITerm context) {
        this.ast = ast;
        this.context = context;
    }

    @Override public boolean equals(Object other) {
        if(this == other) return true;
        if(!(other instanceof ReferenceRetentionPlaceholderDescriptor)) return false;
        ReferenceRetentionPlaceholderDescriptor that = (ReferenceRetentionPlaceholderDescriptor)other;
        // @formatter:off
        return Objects.equals(this.getAst(), that.getAst())
            && Objects.equals(this.getContext(), that.getContext());
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(ast, context);
    }

    @Override
    public String toString() {
        return "[[" + getAst() + "|" + getContext() + "]]";
    }
}
