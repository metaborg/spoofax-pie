package mb.tiger.spoofax.task.reusable;

import mb.common.region.Region;
import mb.common.style.StyleName;
import mb.common.util.ListView;
import mb.completions.common.CompletionProposal;
import mb.completions.common.CompletionResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.tiger.spoofax.TigerScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.NotImplementedException;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;
//import org.spoofax.terms.util.TermUtils;

@TigerScope
@Deprecated
public class TigerCompleteTaskDef implements TaskDef<TigerCompleteTaskDef.Input, @Nullable CompletionResult> {

    public static class Input implements Serializable {
        public final Supplier<@Nullable IStrategoTerm> astProvider;

        public Input(Supplier<IStrategoTerm> astProvider) {
            this.astProvider = astProvider;
        }
    }

    public static class Output implements Serializable {

    }

    @Inject public TigerCompleteTaskDef() {}

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public @Nullable CompletionResult exec(ExecContext context, Input input) throws Exception {

        // 1) Get the file in which code completion is invoked & parse the file with syntactic completions enabled, resulting in an AST with placeholders
        //    ==> This should be done by specifying the correct astProvider
        // TODO: get the ast in 'completion mode', with placeholders
        @Nullable IStrategoTerm ast = input.astProvider.get(context);
        if (ast == null) return null;   // Cannot complete when we don't get an AST.

        // 3) Find the placeholder closest to the caret <- that's the one we want to complete
        //    TODO: What do we do when there are no placeholders? E.g., invoking code completion on a complete file?
        // 4) Get the solver state of the program (whole project), which should have some remaining constraints
        //    on the placeholder.
        //    TODO: What to do when the file is semantically incorrect? Recovery?
        // 5) Invoke the completer on the solver state, indicating the placeholder for which we want completions
        // 6) Get the possible completions back, as a list of ASTs with new solver states
        // 7) Format each completion as a proposal, with pretty-printed text
        // 8) Insert the selected completion: insert the pretty-printed text in the code,
        //    and (maybe?) add the solver state to the current solver state


        return new CompletionResult(ListView.of(
            new CompletionProposal("mypackage", "description", "", "", "mypackage", Objects.requireNonNull(StyleName.fromString("meta.package")), ListView.of(), false),
            new CompletionProposal("myclass", "description", "", "T", "mypackage", Objects.requireNonNull(StyleName.fromString("meta.class")), ListView.of(), false)
        ), true);
    }

    /**
     * Finds the placeholder near the caret location in the specified AST.
     *
     * This method assumes all terms in the AST are uniquely identifiable,
     * for example through a term index or unique tree path.
     *
     * @param ast the AST, with placeholders
     * @param caretOffset the caret location
     * @return the placeholder; or {@code null} if not found
     */
    private @Nullable IStrategoTerm findNearbyPlaceholder(IStrategoTerm ast, int caretOffset) {
        if (!termContainsCaret(ast, caretOffset)) return null;
//        TermUtils
//        if ()
        throw new NotImplementedException();
    }

    /**
     * Determines whether the specified term contains the specified caret offset.
     *
     * @param term the term
     * @param caretOffset the caret offset to find
     * @return {@code true} when the term contains the caret offset;
     * otherwise, {@code false}.
     */
    private boolean termContainsCaret(IStrategoTerm term, int caretOffset) {
        @Nullable ImploderAttachment imploder = getImploderAttachment(term);
        if(imploder == null) return false;

        Region termRegion = Region.fromOffsets(
            imploder.getLeftToken().getStartOffset(),
            imploder.getRightToken().getEndOffset()
        );
        return termRegion.contains(caretOffset);
    }

    /**
     * Gets the imploder attachment of the specified term.
     *
     * @param term the term for which to get the imploder attachment
     * @return the imploder attachment; or {@code null} if not found
     */
    private @Nullable ImploderAttachment getImploderAttachment(IStrategoTerm term) {
        @Nullable ImploderAttachment imploder = ImploderAttachment.get(term);
        if(imploder == null) {
            @Nullable IStrategoTerm originTerm = OriginAttachment.getOrigin(term);
            imploder = originTerm != null ? ImploderAttachment.get(originTerm) : null;
        }
        return imploder;
    }
}
