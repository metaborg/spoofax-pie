package mb.tiger;

import mb.common.result.Result;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.resource.ResourceService;
import mb.spoofax.compiler.interfaces.spoofaxcore.CodeCompleter;
import mb.spoofax.compiler.interfaces.spoofaxcore.CodeCompleterFactory;
import mb.spoofax.compiler.interfaces.spoofaxcore.ConstraintAnalyzerFactory;
import mb.statix.spec.Spec;
import mb.statix.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.function.Function;

public class TigerCodeCompleterFactory implements CodeCompleterFactory {
    private final Spec spec;
    private final StrategoTerms strategoTerms;
    private final ITermFactory termFactory;
    private final TegoRuntime tegoRuntime;
    private final LoggerFactory loggerFactory;

    public TigerCodeCompleterFactory(
        Spec spec,
        StrategoTerms strategoTerms,
        ITermFactory termFactory,
        TegoRuntime tegoRuntime,
        LoggerFactory loggerFactory
    ) {
        this.spec = spec;
        this.strategoTerms = strategoTerms;
        this.termFactory = termFactory;
        this.tegoRuntime = tegoRuntime;
        this.loggerFactory = loggerFactory;
    }


    @Override
    public TigerCodeCompleter create(
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> explicate,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> implicate,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> upgrade,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> downgrade,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> isInjection,
        Function<IStrategoTerm, Result<String, ?>> prettyPrint
    ) {
        return new TigerCodeCompleter(
            spec,
            strategoTerms,
            termFactory,
            tegoRuntime,
            loggerFactory,
            explicate,
            implicate,
            upgrade,
            downgrade,
            isInjection,
            prettyPrint
        );
    }
}
