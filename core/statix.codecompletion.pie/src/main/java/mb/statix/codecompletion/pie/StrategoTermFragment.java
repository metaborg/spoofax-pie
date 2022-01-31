package mb.statix.codecompletion.pie;


import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.util.TermUtils;

/**
 * A Stratego term fragment.
 */
public final class StrategoTermFragment extends Fragment {

    /**
     * Creates a {@link StrategoTermFragment} from a Stratego term.
     *
     * @param term the Stratego term
     * @return the {@link StrategoTermFragment} instance
     */
    public static @Nullable StrategoTermFragment fromTerm(IStrategoTerm term) {
        final @Nullable String sort = ImploderAttachment.getSort(term);
        if (sort == null) return null;

        final @Nullable IToken leftToken = ImploderAttachment.getLeftToken(term);
        if (leftToken == null) return null;

        final @Nullable IToken rightToken = ImploderAttachment.getRightToken(term);
        if (rightToken == null) return null;

        final @Nullable ITokenizer tokenizer = (ITokenizer)ImploderAttachment.getTokenizer(term);
        if (tokenizer == null) return null;

        return new StrategoTermFragment(
            term,
            sort,
            leftToken,
            rightToken,
            tokenizer
        );
    }

    /** The term of the fragment. */
    public final IStrategoTerm term;

    /**
     * Initializes a new instance of the {@link StrategoTermFragment} class.
     *
     * @param term the term of the fragment
     * @param sort the sort of the fragment
     * @param leftToken the left-most token of the fragment
     * @param rightToken the right-most token of the fragment
     * @param tokenizer the tokenizer of the fragment
     */
    private StrategoTermFragment(
        IStrategoTerm term,
        String sort,
        IToken leftToken,
        IToken rightToken,
        ITokenizer tokenizer
    ) {
        super(sort, leftToken, rightToken, tokenizer);
        this.term = term;
    }

    /** The term of the fragment. */
    public IStrategoTerm getTerm() {
        return this.term;
    }

    /** Whether the fragment is a list term. */
    @Override public boolean isList() {
        return TermUtils.isList(term);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoTermFragment that = (StrategoTermFragment)o;
        return this.term.equals(that.term);
    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }

    @Override public String toString() {
        return term.toString();
    }
}
