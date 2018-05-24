package mb.spoofax.runtime.nabl;

import mb.nabl2.spoofax.analysis.*;
import mb.nabl2.stratego.StrategoTerms;
import mb.nabl2.terms.ITerm;
import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.stratego.StrategoRuntime;
import mb.spoofax.runtime.stratego.StrategoRuntimeBuilder;
import mb.spoofax.runtime.stratego.primitive.ScopeGraphPrimitiveLibrary;
import mb.spoofax.api.SpoofaxEx;
import mb.spoofax.api.SpoofaxRunEx;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.io.Serializable;
import java.util.Optional;

import static mb.nabl2.terms.build.TermBuild.B;

public class CGen implements Serializable {
    private static final long serialVersionUID = 1L;


    private final PPath strategoCtree;
    private final String strategyName;


    public CGen(PPath strategoCtree, String strategyName) {
        this.strategoCtree = strategoCtree;
        this.strategyName = strategyName;
    }


    public PPath strategoCtree() {
        return strategoCtree;
    }

    public String strategyName() {
        return strategyName;
    }


    public StrategoRuntime createSuitableRuntime(StrategoRuntimeBuilder strategoRuntimeBuilder, ScopeGraphPrimitiveLibrary primitiveLibrary) throws SpoofaxEx {
        strategoRuntimeBuilder.addCtree(strategoCtree);
        strategoRuntimeBuilder.addLibrary(primitiveLibrary);
        final StrategoRuntime runtime = strategoRuntimeBuilder.build();
        return runtime;
    }

    public ImmutableInitialResult cgenGlobal(StrategoRuntime runtime) throws SpoofaxEx {
        final IOAgent ioAgent = new IOAgent();
        final ITermFactory termFactory = runtime.termFactory();
        final StrategoTerms termConverter = new StrategoTerms(termFactory);
        final ITerm globalAST = Actions.sourceTerm(ConstraintSolver.globalSource, B.EMPTY_TUPLE);
        final ITerm initialResultTerm =
            doAction(termConverter, runtime, ioAgent, Actions.analyzeInitial(ConstraintSolver.globalSource, globalAST))
                .orElseThrow(() -> new SpoofaxEx("No initial result"));
        final ImmutableInitialResult initialResult = (ImmutableInitialResult) InitialResult.matcher()
            .match(initialResultTerm).orElseThrow(() -> new SpoofaxRunEx("Invalid initial result"));
        return initialResult;
    }

    public ImmutableUnitResult cgenDocument(InitialResult globalConstraints, IStrategoTerm ast, String source, StrategoRuntime runtime)
        throws SpoofaxEx {
        final IOAgent ioAgent = new IOAgent();
        final ITermFactory termFactory = runtime.termFactory();
        final StrategoTerms termConverter = new StrategoTerms(termFactory);
        final ITerm unitResultTerm = doAction(termConverter, runtime, ioAgent,
            Actions.analyzeUnit(source, termConverter.fromStratego(ast), globalConstraints.getArgs()))
            .orElseThrow(() -> new SpoofaxEx("No unit result"));
        final ImmutableUnitResult unitResult = (ImmutableUnitResult) UnitResult.matcher().match(unitResultTerm)
            .orElseThrow(() -> new SpoofaxEx("Invalid unit result"));
        return unitResult;
    }


    private Optional<ITerm> doAction(StrategoTerms termConverter, StrategoRuntime runtime, IOAgent ioAgent,
        ITerm action) throws SpoofaxEx {
        try {
            final IStrategoTerm inputTerm = termConverter.toStratego(action);
            return Optional.ofNullable(runtime.invoke(strategyName, inputTerm, ioAgent, null))
                .map(termConverter::fromStratego);
        } catch(SpoofaxEx e) {
            throw new SpoofaxEx("Constraint generator action failed", e);
        }
    }


    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CGen that = (CGen) o;

        if(!strategoCtree.equals(that.strategoCtree))
            return false;
        return strategyName.equals(that.strategyName);
    }

    @Override public int hashCode() {
        int result = strategoCtree.hashCode();
        result = 31 * result + strategyName.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ConstraintGenerator(" + "strategoCtree=" + strategoCtree + ", strategyName='" + strategyName + '\''
            + ')';
    }
}
