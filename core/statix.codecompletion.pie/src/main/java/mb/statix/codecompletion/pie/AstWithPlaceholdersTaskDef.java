package mb.statix.codecompletion.pie;

import mb.common.region.Region;
import mb.common.result.Result;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoPlaceholder;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Parses the input and returns an AST with placeholders around the caret position.
 */
public class AstWithPlaceholdersTaskDef implements TaskDef<AstWithPlaceholdersTaskDef.Input, Result<AstWithPlaceholdersTaskDef.Output, ?>> {

    private final Logger log;
    private final GetStrategoRuntimeProvider getStrategoRuntimeProviderTask;

    /**
     * Initializes a new instance of the {@link AstWithPlaceholdersTaskDef} class.
     *
     * @param getStrategoRuntimeProviderTask the Stratego runtime provider task
     * @param loggerFactory the logger factory
     */
    public AstWithPlaceholdersTaskDef(
        GetStrategoRuntimeProvider getStrategoRuntimeProviderTask,
        LoggerFactory loggerFactory
    ) {
        this.log = loggerFactory.create(getClass());
//        this.parseTask = parseTask;
        this.getStrategoRuntimeProviderTask = getStrategoRuntimeProviderTask;
    }

    @Override public String getId() {
        return this.getClass().getName();
    }

    @Override public Result<Output, ?> exec(ExecContext context, Input input) throws Exception {
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProviderTask, None.instance).getValue().get();

        final Result<IStrategoTerm, ?> parsedAstResult = context.require(input.astSupplier);
        if (parsedAstResult.isErr()) return parsedAstResult.ignoreValueIfErr();
        final IStrategoTerm parsedAst = parsedAstResult.unwrapUnchecked();

        final IStrategoTerm newAst = new Execution(context, strategoRuntime).insertPlaceholdersNearSelection(
            parsedAst, input.primarySelection
        );
        // Transform any placeholder constructors into real placeholders.
        // TODO ?

        // Insert placeholders around the caret position.
        // TODO

        // Gather all the placeholders around the caret position
        // TODO

        // FIXME: Return the newAst and the placeholders instead
        return Result.ofOk(new Output(newAst, Collections.emptyList()));
//        return Result.ofOk(new Output(parsedAst, Collections.emptyList()));
    }


    /**
     * Contains the state necessary for executing this task.
     */
    private final class Execution {
        /** The execution context. */
        private final ExecContext context;
        /** The Stratego runtime. */
        private final StrategoRuntime strategoRuntime;
        /** The term factory. */
        private final ITermFactory termFactory;

        /**
         * Initializes a new instance of the {@link Execution} class.
         *
         * @param context         the execution context
         * @param strategoRuntime the Stratego runtime
         */
        public Execution(
            ExecContext context,
            StrategoRuntime strategoRuntime
        ) {
            this.context = context;
            this.strategoRuntime = strategoRuntime;
            this.termFactory = strategoRuntime.getTermFactory();
        }

        private IStrategoTerm insertPlaceholdersNearSelection(IStrategoTerm term, Region selection) {
            // Recursively visit each term that contains the selection.
            // Depending on the type of term, we insert placeholders
            final TermTransformation transformation = new TermTransformation(termFactory) {
                @Override protected boolean traverse(IStrategoTerm fragmentTerm) {
                    final @Nullable StrategoTermFragment fragment = StrategoTermFragment.fromTerm(fragmentTerm);
                    if (fragment == null) {
                        log.error("Could not determine the region of the fragment: {}", fragmentTerm);
                        return false;
                    }
                    fragment.setLeftRecursive(CodeCompletionUtils.isLeftRecursive(fragment, selection, strategoRuntime));
                    fragment.setRightRecursive(CodeCompletionUtils.isRightRecursive(fragment, selection, strategoRuntime));
                    return isInSelectionAdjacentRegion(fragment, selection);
                }

                @Override protected IStrategoTerm transform(IStrategoTerm fragmentTerm) {
                    final @Nullable StrategoTermFragment fragment = StrategoTermFragment.fromTerm(fragmentTerm);
                    if (fragment == null) {
                        log.error("Could not determine the region of the fragment: {}", fragmentTerm);
                        return fragmentTerm;
                    }
                    if (fragment.isError()) {
                        // Replace the term with a placeholder
                        return createPlaceholderForFragment(fragment);
                    } else if (fragmentTerm.getType() == TermType.LIST) {
                        // Insert a placeholder near/in the selection in the list
                        // FIXME: This kinda works, but we don't know the Sort and that fails.
                        // Actually, an additional problem is that we don't have a placeholder to represent lists.
//                        final IStrategoTerm[] newSubterms = new IStrategoTerm[fragmentTerm.getSubtermCount() + 1];
//                        // FIXME: We only insert at the end now, we should insert near the selection
//                        int insertAt = fragmentTerm.getSubtermCount();
//                        for (int i = 0; i < insertAt; i++) {
//                            newSubterms[i] = fragmentTerm.getSubterm(i);
//                        }
//                        // FIXME: Sort is wrong. Also, no position imploder attachments.
//                        newSubterms[insertAt] = createPlaceholderForSort("X");
//                        for (int i = insertAt + 1; i < newSubterms.length; i++) {
//                            newSubterms[i] = fragmentTerm.getSubterm(i - 1);
//                        }
//                        return withSubterms(fragmentTerm, newSubterms);
                    }
                    return fragmentTerm;
                }
            };
            return transformation.transformRecursive(term);
        }

        /**
         * Creates a placeholder for the specified fragment.
         *
         * @param sort the sort
         * @return the placeholder constructor application term
         */
        private IStrategoAppl createPlaceholderForSort(String sort) {
            return termFactory.makeAppl(sort + "-Plhdr");
        }

        /**
         * Creates a placeholder for the specified fragment.
         *
         * @param fragment the fragment
         * @return the placeholder constructor application term
         */
        private IStrategoAppl createPlaceholderForFragment(StrategoTermFragment fragment) {
            final IStrategoAppl term = termFactory.makeAppl(fragment.getSort() + "-Plhdr");
            termFactory.copyAttachments(fragment.getTerm(), term);
            return term;
        }

        /**
         * Determines whether the selection intersects with the region of the specified term fragment.
         *
         * @param fragment the fragment
         * @param selection the primary selection
         * @return {@code true} when the term intersects with the selection region;
         * otherwise, {@code false}
         */
        private boolean isInSelectionAdjacentRegion(Fragment fragment, Region selection) {
            final @Nullable Region region = CodeCompletionUtils.getAdjacentRegionOf(fragment);
            return region != null && region.intersectsWith(selection);
        }
    }


    /**
     * Input arguments for the {@link AstWithPlaceholdersTaskDef} task.
     */
    public static final class Input implements Serializable {
        /** Supplies the AST to transform. */
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;
        /** The primary selection at which to complete. */
        public final Region primarySelection;

        /**
         * Initializes a new instance of the {@link Input} class.
         *
         * @param primarySelection  the primary selection at which to complete
         * @param astSupplier supplies the AST to transform
         */
        public Input(
            Region primarySelection,
            Supplier<? extends Result<IStrategoTerm, ?>> astSupplier
        ) {
            this.primarySelection = primarySelection;
            this.astSupplier = astSupplier;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            return innerEquals((Input)o);
        }

        /**
         * Determines whether this object is equal to the specified object.
         *
         * Note: this method does not check whether the type of the argument is exactly the same.
         *
         * @param that the object to compare to
         * @return {@code true} when this object is equal to the specified object;
         * otherwise, {@code false}
         */
        protected boolean innerEquals(Input that) {
            // @formatter:off
            return this.primarySelection.equals(that.primarySelection)
                && this.astSupplier.equals(that.astSupplier);
            // @formatter:on
        }

        @Override public int hashCode() {
            return Objects.hash(
                this.primarySelection,
                this.astSupplier
            );
        }

        @Override public String toString() {
            return "PlaceholderAstTaskDef$Input{" +
                "primarySelection=" + primarySelection + ", " +
                "astSupplier=" + astSupplier +
                "}";
        }
    }


    /**
     * Output values for the {@link AstWithPlaceholdersTaskDef} task.
     */
    public static final class Output implements Serializable {
        /** The AST, with placeholders inserted around the caret location. */
        public final IStrategoTerm ast;
        /** The placeholders around the caret location. */
        public final List<? extends IStrategoPlaceholder> placeholders;

        /**
         * Initializes a new instance of the {@link Output} class.
         *
         * @param ast the AST, with placeholders inserted around the caret location
         * @param placeholders the placeholders around the caret location
         */
        public Output(
            IStrategoTerm ast,
            List<? extends IStrategoPlaceholder> placeholders
        ) {
            this.ast = ast;
            this.placeholders = placeholders;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            return innerEquals((Output)o);
        }

        /**
         * Determines whether this object is equal to the specified object.
         *
         * Note: this method does not check whether the type of the argument is exactly the same.
         *
         * @param that the object to compare to
         * @return {@code true} when this object is equal to the specified object;
         * otherwise, {@code false}
         */
        protected boolean innerEquals(Output that) {
            // @formatter:off
            return this.ast.equals(that.ast)
                && this.placeholders.equals(that.placeholders);
            // @formatter:on
        }

        @Override public int hashCode() {
            return Objects.hash(
                ast,
                placeholders
            );
        }

        @Override public String toString() {
            return "PlaceholderAstTaskDef$Output{" +
                "ast=" + ast + ", " +
                "placeholders=" + placeholders +
                '}';
        }
    }
}
