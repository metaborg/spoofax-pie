package mb.tiger;

import mb.common.codecompletion.CodeCompletionResult;
import mb.common.region.Region;
import mb.common.result.Result;
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
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> explicateFunction,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> implicateFunction,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> upgradeFunction,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> downgradeFunction,
        Function<IStrategoTerm, Result< IStrategoTerm, ?>> isInjectionFunction,
        Function<IStrategoTerm, Result<String, ?>> prettyPrintFunction
    ) {
        this.spec = spec;
        this.implementation = new StatixCodeCompleter(
            strategoTerms,
            termFactory,
            tegoRuntime,
            loggerFactory
        ) {

            @Override
            protected Result<IStrategoTerm, ?> explicate(IStrategoTerm term) {
                return explicateFunction.apply(term);
            }

            @Override
            protected Result<IStrategoTerm, ?> implicate(IStrategoTerm term) {
                return implicateFunction.apply(term);
            }

            @Override
            protected Result<IStrategoTerm, ?> upgrade(IStrategoTerm term) {
                return upgradeFunction.apply(term);
            }

            @Override
            protected Result<IStrategoTerm, ?> downgrade(IStrategoTerm term) {
                return downgradeFunction.apply(term);
            }

            @Override
            protected Result<IStrategoTerm, ?> isInj(IStrategoTerm term) {
                return isInjectionFunction.apply(term);
            }

            @Override
            protected Result<String, ?> prettyPrint(IStrategoTerm term) {
                return prettyPrintFunction.apply(term);
            }
        };
    }

    @Override
    public @Nullable CodeCompletionResult complete(IStrategoTerm ast, IStrategoTerm analysisResult, Region primarySelection, ResourceKey resource) throws InterruptedException {
        return implementation.complete(spec, ast, primarySelection, resource);
    }

}
