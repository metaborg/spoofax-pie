package mb.tiger;

import mb.common.codecompletion.CodeCompletionResult;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.resource.ResourceKey;
import mb.spoofax.compiler.interfaces.spoofaxcore.CodeCompleter;
import mb.statix.StatixCodeCompleterBase;
import mb.statix.StatixCodeCompleter;
import mb.statix.spec.Spec;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.util.function.Function;

public class TigerCodeCompleter implements CodeCompleter {
    private final StatixCodeCompleterBase implementation;

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
        this.implementation = new StatixCodeCompleter(
            spec,
            strategoTerms,
            termFactory,
            tegoRuntime,
            loggerFactory,
            explicateFunction,
            implicateFunction,
            upgradeFunction,
            downgradeFunction,
            isInjectionFunction,
            prettyPrintFunction
        );
    }

    @Override
    public @Nullable CodeCompletionResult complete(IStrategoTerm ast, Region primarySelection, ResourceKey resource) throws InterruptedException {
        return implementation.complete(ast, primarySelection, resource);
    }

}
