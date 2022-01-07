package mb.statix.codecompletion.pie;


import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.TermOrigin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

/**
 * A NaBL term fragment.
 */
public final class NablTermFragment extends Fragment {

    /**
     * Creates a {@link NablTermFragment} from a Stratego term.
     *
     * @param term the Stratego term
     * @return the {@link NablTermFragment} instance
     */
    public static @Nullable NablTermFragment fromTerm(ITerm term) {
        @Nullable final TermOrigin origin = TermOrigin.get(term).orElse(null);
        if (origin == null) return null;

        final ImploderAttachment imploderAttachment = origin.getImploderAttachment();
        if (imploderAttachment == null) return null;

        final @Nullable String sort = imploderAttachment.getSort();
        if (sort == null) return null;

        final @Nullable IToken leftToken = imploderAttachment.getLeftToken();
        if (leftToken == null) return null;

        final @Nullable IToken rightToken = imploderAttachment.getRightToken();
        if (rightToken == null) return null;

        final @Nullable ITokenizer tokenizer = (ITokenizer)leftToken.getTokenizer();
        if (tokenizer == null) return null;

        return new NablTermFragment(
            term,
            sort,
            leftToken,
            rightToken,
            tokenizer
        );
    }

    /** The term of the fragment. */
    public final ITerm term;

    /**
     * Initializes a new instance of the {@link NablTermFragment} class.
     *
     * @param term the term of the fragment
     * @param sort the sort of the fragment
     * @param leftToken the left-most token of the fragment
     * @param rightToken the right-most token of the fragment
     * @param tokenizer the tokenizer of the fragment
     */
    private NablTermFragment(
        ITerm term,
        String sort,
        IToken leftToken,
        IToken rightToken,
        ITokenizer tokenizer
    ) {
        super(sort, leftToken, rightToken, tokenizer);
        this.term = term;
    }

    /** The term of the fragment. */
    public ITerm getTerm() {
        return this.term;
    }

    /** Whether the fragment is a list term. */
    @Override public boolean isList() {
        return term instanceof IListTerm;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final NablTermFragment that = (NablTermFragment)o;
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
