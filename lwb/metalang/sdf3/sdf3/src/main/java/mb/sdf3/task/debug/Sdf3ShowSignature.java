package mb.sdf3.task.debug;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToSignature;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowSignature extends ShowAnalyzedTaskDef {
    @Inject public Sdf3ShowSignature(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3AnalyzeMulti analyze,
        Sdf3ToSignature operation,
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(parse, desugar.createFunction(), analyze, operation.createFunction(), getStrategoRuntimeProvider, "pp-stratego-string", "Stratego signatures");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
