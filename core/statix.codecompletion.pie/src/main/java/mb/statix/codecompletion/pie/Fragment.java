package mb.statix.codecompletion.pie;


import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.terms.util.TermUtils;

/**
 * A fragment.
 */
public abstract class Fragment {

    /** The sort of the fragment. */
    private final String sort;
    /** The left-most token of the fragment. */
    private final IToken leftToken;
    /** The right-most token of the fragment. */
    private final IToken rightToken;
    /** The tokenizer of the fragment. */
    private final ITokenizer tokenizer;
    /** Whether the fragment's sort is left-recursive. */
    private @Nullable Boolean isLeftRecursive = null;
    /** Whether the fragment's sort is right-recursive. */
    private @Nullable Boolean isRightRecursive = null;

    /**
     * Initializes a new instance of the {@link Fragment} class.
     *
     * @param sort the sort of the fragment
     * @param leftToken the left-most token of the fragment
     * @param rightToken the right-most token of the fragment
     * @param tokenizer the tokenizer of the fragment
     */
    protected Fragment(
        String sort,
        IToken leftToken,
        IToken rightToken,
        ITokenizer tokenizer
    ) {
        this.sort = sort;
        this.leftToken = leftToken;
        this.rightToken = rightToken;
        this.tokenizer = tokenizer;
    }

    /** The sort of the fragment. */
    public String getSort() {
        return this.sort;
    }

    /** The left-most token of the fragment. */
    public IToken getLeftToken() {
        return this.leftToken;
    }

    /** The right-most token of the fragment. */
    public IToken getRightToken() {
        return this.rightToken;
    }

    /** The tokenizer of the fragment. */
    public ITokenizer getTokenizer() {
        return this.tokenizer;
    }

    /** The left offset of the left-most token. */
    public int getLeftOffset() {
        return leftToken.getStartOffset();
    }

    /** The right offset of the right-most token. */
    public int getRightOffset() {
        return rightToken.getEndOffset();
    }

    /** Whether the fragment contains no actual source characters. */
    public boolean isEmpty() {
        return getRightOffset() < getLeftOffset();
    }

    /** Whether the fragment is a list term. */
    public abstract boolean isList();

    /** Whether the fragment is an optional term. */
    public boolean isOptional() {
        return !isList() && isEmpty() && getLeftToken() == getRightToken();
    }

    /** Whether the fragment is nullable. */
    public boolean isNullable() {
        return isList() || isOptional() || isLeftRecursive() || isRightRecursive();
    }

    /** Whether the fragment indicates an error. */
    public boolean isError() { return leftToken.getKind() == IToken.Kind.TK_ERROR && rightToken.getKind() == IToken.Kind.TK_ERROR; }

    /** Gets whether the fragment's sort is left-recursive. */
    public boolean isLeftRecursive() {
        assert(isLeftRecursive != null);
        return isLeftRecursive;
    }

    /** Gets whether the fragment's sort is right-recursive. */
    public boolean isRightRecursive() {
        assert(isRightRecursive != null);
        return isRightRecursive;
    }

    /**
     * Sets whether the fragment's sort is left-recursive.
     *
     * @param value the new value
     */
    public void setLeftRecursive(boolean value) {
        isLeftRecursive = value;
    }

    /**
     * Sets whether the fragment's sort is right-recursive.
     *
     * @param value the new value
     */
    public void setRightRecursive(boolean value) {
        isRightRecursive = value;
    }

}
