package mb.tiger;

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
        Function<IStrategoTerm, @Nullable IStrategoTerm> explicate,
        Function<IStrategoTerm, @Nullable IStrategoTerm> implicate,
        Function<IStrategoTerm, @Nullable IStrategoTerm> upgrade,
        Function<IStrategoTerm, @Nullable IStrategoTerm> downgrade,
        Function<IStrategoTerm, @Nullable IStrategoTerm> isInjection,
        Function<IStrategoTerm, @Nullable String> prettyPrint
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
