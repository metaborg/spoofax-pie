package mb.tiger.spoofax.task;

import mb.common.codecompletion.CodeCompletionResult;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.tiger.TigerCodeCompleter;
import mb.tiger.TigerCodeCompleterFactory;
import mb.tiger.spoofax.TigerScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;

// TODO: Make this a template
@TigerScope
public class TigerCodeComplete implements TaskDef<TigerCodeComplete.Input, @Nullable CodeCompletionResult> {

    public static class Input implements Serializable {
        public final ResourceKey resourceKey;
        public final int caretLocation;
        public final Supplier<@Nullable IStrategoTerm> astSupplier;
        public final Function<IStrategoTerm, Result<IStrategoTerm, ?>> preAnalyzeFunction;
        public final Function<IStrategoTerm, Result<IStrategoTerm, ?>> postAnalyzeFunction;
        public final Function<IStrategoTerm, Result<IStrategoTerm, ?>> upgradePlaceholdersFunction;
        public final Function<IStrategoTerm, Result<IStrategoTerm, ?>> downgradePlaceholdersFunction;
        public final Function<IStrategoTerm, Result<IStrategoTerm, ?>> isInjectionFunction;     // Should be a predicate
        public final Function<IStrategoTerm, Result<String, ?>> prettyPrinterFunction;
//        public final Function<IStrategoTerm, @Nullable String> prettyPrintFunction;

        public Input(Supplier<IStrategoTerm> supplier) {
            // This constructor is only here to satisfy the compiler
            // because of the generated createCompletionTask() method
            // in GeneratedTigerInstance
            throw new UnsupportedOperationException();
        }

        public Input(
            ResourceKey resourceKey,
            int caretLocation,
            Supplier<IStrategoTerm> astSupplier,
            Function<IStrategoTerm, Result<IStrategoTerm, ?>> preAnalyzeFunction,
            Function<IStrategoTerm, Result<IStrategoTerm, ?>> postAnalyzeFunction,
            Function<IStrategoTerm, Result<IStrategoTerm, ?>> upgradePlaceholdersFunction,
            Function<IStrategoTerm, Result<IStrategoTerm, ?>> downgradePlaceholdersFunction,
            Function<IStrategoTerm, Result<IStrategoTerm, ?>> isInjectionFunction,     // Should be a predicate
            Function<IStrategoTerm, Result<String, ?>> prettyPrinterFunction
//            Function<IStrategoTerm, @Nullable String> prettyPrintFunction
        ) {
            this.resourceKey = resourceKey;
            this.caretLocation = caretLocation;
            this.astSupplier = astSupplier;
            this.preAnalyzeFunction = preAnalyzeFunction;
            this.postAnalyzeFunction = postAnalyzeFunction;
            this.upgradePlaceholdersFunction = upgradePlaceholdersFunction;
            this.downgradePlaceholdersFunction = downgradePlaceholdersFunction;
            this.isInjectionFunction = isInjectionFunction;
            this.prettyPrinterFunction = prettyPrinterFunction;
//            this.prettyPrintFunction = prettyPrintFunction;
        }
    }

    private final Logger log;
    private final TigerCodeCompleterFactory codeCompleterFactory;

    @Inject public TigerCodeComplete(
        LoggerFactory loggerFactory,
        TigerCodeCompleterFactory codeCompleterFactory
    ) {
        this.log = loggerFactory.create(TigerCodeComplete.class);
        this.codeCompleterFactory = codeCompleterFactory;
    }

    @Override
    public @Nullable CodeCompletionResult exec(ExecContext context, Input input) throws Exception {
        final TigerCodeCompleter codeCompleter = codeCompleterFactory.create(
            t -> explicate(context, input, t),
            t -> implicate(context, input, t),
            t -> upgrade(context, input, t),
            t -> downgrade(context, input, t),
            t -> isInjection(context, input, t),
            t -> prettyPrint(context, input, t)
        );

        // Get the file in which code completion is invoked & parse the file with syntactic completions enabled,
        // resulting in an AST with placeholders
        // ==> This should be done by specifying the correct astProvider
        // TODO: get the ast in 'completion mode', with placeholders (use placeholder recovery or inference)
        @Nullable IStrategoTerm ast = input.astSupplier.get(context);
        if (ast == null){
            log.error("Completion failed: we didn't get an AST.");
            return null;   // Cannot complete when we don't get an AST.
        }

        return codeCompleter.complete(ast, Region.atOffset(input.caretLocation), input.resourceKey);
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    private Result<IStrategoTerm, ?> explicate(ExecContext context, Input input, IStrategoTerm term) {
        return input.preAnalyzeFunction.apply(context, term);
    }

    private Result<IStrategoTerm, ?> implicate(ExecContext context, Input input, IStrategoTerm term) {
        return input.postAnalyzeFunction.apply(context, term);
    }

    private Result<IStrategoTerm, ?> upgrade(ExecContext context, Input input, IStrategoTerm term) {
        return input.upgradePlaceholdersFunction.apply(context, term);
    }

    private Result<IStrategoTerm, ?> downgrade(ExecContext context, Input input, IStrategoTerm term) {
        return input.downgradePlaceholdersFunction.apply(context, term);
    }

    private Result<IStrategoTerm, ?> isInjection(ExecContext context, Input input, IStrategoTerm term) {
        return input.isInjectionFunction.apply(context, term);
    }

    private Result<String, ?> prettyPrint(ExecContext context, Input input, IStrategoTerm term) {
        return input.prettyPrinterFunction.apply(context, term);
    }

}
