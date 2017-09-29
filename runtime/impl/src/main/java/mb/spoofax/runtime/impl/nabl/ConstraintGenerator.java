package mb.spoofax.runtime.impl.nabl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.metaborg.meta.nabl2.spoofax.analysis.Actions;
import org.metaborg.meta.nabl2.spoofax.analysis.Args;
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableInitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableUnitResult;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;
import org.metaborg.meta.nabl2.stratego.StrategoTerms;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.spoofax.runtime.impl.stratego.StrategoRuntime;
import mb.spoofax.runtime.impl.stratego.StrategoRuntimeBuilder;
import mb.spoofax.runtime.impl.stratego.primitive.ScopeGraphPrimitiveLibrary;
import mb.spoofax.runtime.model.SpoofaxEx;
import mb.spoofax.runtime.model.SpoofaxRunEx;
import mb.vfs.path.PPath;

public class ConstraintGenerator implements Serializable {
    private static final long serialVersionUID = 1L;


    private final PPath strategoCtree;
    private final String strategyName;


    public ConstraintGenerator(PPath strategoCtree, String strategyName) {
        this.strategoCtree = strategoCtree;
        this.strategyName = strategyName;
    }


    public PPath strategoCtree() {
        return strategoCtree;
    }

    public String strategyName() {
        return strategyName;
    }


    public StrategoRuntime createSuitableRuntime(StrategoRuntimeBuilder strategoRuntimeBuilder,
        ScopeGraphPrimitiveLibrary primitiveLibrary) throws SpoofaxEx {
        strategoRuntimeBuilder.addCtree(strategoCtree);
        strategoRuntimeBuilder.addLibrary(primitiveLibrary);
        final StrategoRuntime runtime = strategoRuntimeBuilder.build();
        return runtime;
    }

    public ImmutableInitialResult initialResult(StrategoRuntime runtime) throws SpoofaxEx {
        final IOAgent ioAgent = new IOAgent();
        final ITermFactory termFactory = runtime.termFactory();
        final StrategoTerms termConverter = new StrategoTerms(termFactory);
        final ITerm initialResultTerm =
            doAction(termConverter, runtime, ioAgent, Actions.analyzeInitial(ConstraintSolver.globalSource))
                .orElseThrow(() -> new SpoofaxEx("No initial result"));
        final ImmutableInitialResult initialResult = (ImmutableInitialResult) InitialResult.matcher()
            .match(initialResultTerm).orElseThrow(() -> new SpoofaxRunEx("Invalid initial result"));
        return initialResult;
    }

    public ImmutableUnitResult unitResult(InitialResult initialResult, IStrategoTerm ast, String source, StrategoRuntime runtime)
        throws SpoofaxEx {
        final IOAgent ioAgent = new IOAgent();
        final ITermFactory termFactory = runtime.termFactory();
        final StrategoTerms termConverter = new StrategoTerms(termFactory);
        final ITerm unitResultTerm = doAction(termConverter, runtime, ioAgent,
            Actions.analyzeUnit(source, termConverter.fromStratego(ast), initialResult.getArgs()))
                .orElseThrow(() -> new SpoofaxEx("No unit result"));
        final ImmutableUnitResult unitResult = (ImmutableUnitResult) UnitResult.matcher().match(unitResultTerm)
            .orElseThrow(() -> new SpoofaxEx("Invalid unit result"));
        return unitResult;
    }

    public HashMap<String, ImmutableUnitResult> unitResults(StrategoRuntime runtime, Map<String, IStrategoTerm> astPerPaths,
        Args args) throws SpoofaxEx {
        final IOAgent ioAgent = new IOAgent();
        final ITermFactory termFactory = runtime.termFactory();
        final StrategoTerms termConverter = new StrategoTerms(termFactory);
        final HashMap<String, ImmutableUnitResult> results = new HashMap<>();
        for(Map.Entry<String, IStrategoTerm> entry : astPerPaths.entrySet()) {
            final String source = entry.getKey();
            final IStrategoTerm ast = entry.getValue();
            final ITerm unitResultTerm = doAction(termConverter, runtime, ioAgent,
                Actions.analyzeUnit(source, termConverter.fromStratego(ast), args))
                    .orElseThrow(() -> new SpoofaxEx("No unit result"));
            final ImmutableUnitResult unitResult =
                (ImmutableUnitResult) UnitResult.matcher().match(unitResultTerm)
                    .orElseThrow(() -> new SpoofaxEx("Invalid unit result"));
            results.put(source, unitResult);
        }
        return results;
    }


    private Optional<ITerm> doAction(StrategoTerms termConverter, StrategoRuntime runtime, IOAgent ioAgent,
        ITerm action) throws SpoofaxEx {
        try {
            final IStrategoTerm inputTerm = termConverter.toStratego(action);
            return Optional.ofNullable(runtime.invoke(strategyName, inputTerm, ioAgent, new DummyScopeGraphContext()))
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

        ConstraintGenerator that = (ConstraintGenerator) o;

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
