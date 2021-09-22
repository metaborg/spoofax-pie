package mb.tiger;

import mb.common.codecompletion.CodeCompletionResult;
import mb.common.region.Region;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.resource.ResourceKey;
import mb.spoofax.compiler.interfaces.spoofaxcore.CodeCompleter;
import mb.statix.StatixCodeCompleter;
import mb.statix.spec.Spec;
import mb.statix.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.util.function.Function;

public class TigerCodeCompleter implements CodeCompleter {
    private final StatixCodeCompleter implementation;
    private Spec spec;

    @Inject
    public TigerCodeCompleter(
        Spec spec,
        StrategoTerms strategoTerms,
        ITermFactory termFactory,
        TegoRuntime tegoRuntime,
        LoggerFactory loggerFactory,
        Function<IStrategoTerm, @Nullable IStrategoTerm> explicateFunction,
        Function<IStrategoTerm, @Nullable IStrategoTerm> implicateFunction,
        Function<IStrategoTerm, @Nullable IStrategoTerm> upgradeFunction,
        Function<IStrategoTerm, @Nullable IStrategoTerm> downgradeFunction,
        Function<IStrategoTerm, @Nullable IStrategoTerm> isInjectionFunction,
        Function<IStrategoTerm, @Nullable String> prettyPrintFunction
    ) {
        this.spec = spec;
        this.implementation = new StatixCodeCompleter(
            strategoTerms,
            termFactory,
            tegoRuntime,
            loggerFactory
        ) {

            @Override
            protected @Nullable String prettyPrint(IStrategoTerm term) {
                return prettyPrintFunction.apply(term);
            }

            @Override
            protected @Nullable IStrategoTerm explicate(IStrategoTerm term) {
                return explicateFunction.apply(term);
            }

            @Override
            protected @Nullable IStrategoTerm implicate(IStrategoTerm term) {
                return implicateFunction.apply(term);
            }

            @Override
            protected @Nullable IStrategoTerm upgrade(IStrategoTerm term) {
                return upgradeFunction.apply(term);
            }

            @Override
            protected @Nullable IStrategoTerm downgrade(IStrategoTerm term) {
                return downgradeFunction.apply(term);
            }

            @Override
            protected @Nullable IStrategoTerm isInj(IStrategoTerm term) {
                return isInjectionFunction.apply(term);
            }
        };
    }

    @Override
    public @Nullable CodeCompletionResult complete(IStrategoTerm ast, IStrategoTerm analysisResult, Region primarySelection, ResourceKey resource) throws InterruptedException {
        return implementation.complete(spec, ast, primarySelection, resource);
    }

}
