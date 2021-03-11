package mb.sdf3.task.debug;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToDynsemSignature;

import javax.inject.Inject;

@Sdf3Scope
public class Sdf3ShowDynsemSignature extends ShowAnalyzedTaskDef {
    @Inject public Sdf3ShowDynsemSignature(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3AnalyzeMulti analyzeMulti,
        Sdf3ToDynsemSignature operation,
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(parse, desugar.createFunction(), analyzeMulti, operation.createFunction(), getStrategoRuntimeProvider, "pp-ds-to-string", "DynSem signatures");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
