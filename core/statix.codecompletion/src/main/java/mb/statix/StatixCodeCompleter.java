package mb.statix;

import mb.common.result.Result;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.statix.spec.Spec;
import mb.tego.strategies.runtime.TegoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.function.Function;

public class StatixCodeCompleter extends StatixCodeCompleterBase {

    private final Function<IStrategoTerm, Result<IStrategoTerm, ?>> explicateFunction;
    private final Function<IStrategoTerm, Result<IStrategoTerm, ?>> implicateFunction;
    private final Function<IStrategoTerm, Result<IStrategoTerm, ?>> upgradeFunction;
    private final Function<IStrategoTerm, Result<IStrategoTerm, ?>> downgradeFunction;
    private final Function<IStrategoTerm, Result<IStrategoTerm, ?>> isInjectionFunction;
    private final Function<IStrategoTerm, Result<String, ?>> prettyPrintFunction;

    public StatixCodeCompleter(
        Spec spec,
        StrategoTerms strategoTerms,
        ITermFactory termFactory,
        TegoRuntime tegoRuntime,
        LoggerFactory loggerFactory,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> explicateFunction,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> implicateFunction,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> upgradeFunction,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> downgradeFunction,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> isInjectionFunction,
        Function<IStrategoTerm, Result<String, ?>> prettyPrintFunction
    ) {
        super(
            spec,
            strategoTerms,
            termFactory,
            tegoRuntime,
            loggerFactory
        );
        this.explicateFunction = explicateFunction;
        this.implicateFunction = implicateFunction;
        this.upgradeFunction = upgradeFunction;
        this.downgradeFunction = downgradeFunction;
        this.isInjectionFunction = isInjectionFunction;
        this.prettyPrintFunction = prettyPrintFunction;
    }

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
}
