package mb.tiger;

import mb.spoofax.compiler.interfaces.spoofaxcore.ConstraintAnalyzerFactory;
import mb.stratego.common.StrategoRuntime;

public class TigerConstraintAnalyzerFactory implements ConstraintAnalyzerFactory {
    private final StrategoRuntime strategoRuntime;

    public TigerConstraintAnalyzerFactory(StrategoRuntime strategoRuntime) {
        this.strategoRuntime = strategoRuntime;
    }

    @Override public TigerConstraintAnalyzer create() {
        return new TigerConstraintAnalyzer(strategoRuntime);
    }
}
