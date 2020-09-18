package mb.sdf3.spoofax.task.debug;

import mb.sdf3.spoofax.Sdf3Scope;
import mb.sdf3.spoofax.task.Sdf3AnalyzeMulti;
import mb.sdf3.spoofax.task.Sdf3Desugar;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToDynsemSignature;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowDynsemSignature extends ShowAnalyzedTaskDef {
    @Inject public Sdf3ShowDynsemSignature(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3AnalyzeMulti analyzeMulti,
        Sdf3ToDynsemSignature operation,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        super(parse, desugar.createFunction(), analyzeMulti, operation.createFunction(), strategoRuntimeProvider, "pp-ds-to-string", "DynSem signatures");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
