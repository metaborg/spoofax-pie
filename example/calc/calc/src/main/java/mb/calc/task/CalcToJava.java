package mb.calc.task;

import mb.calc.CalcScope;
import mb.calc.util.AnalyzedStrategoTransformTaskDef;

import javax.inject.Inject;

@CalcScope
public class CalcToJava extends AnalyzedStrategoTransformTaskDef {
    @Inject public CalcToJava(CalcGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "program-to-java-fixed");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
