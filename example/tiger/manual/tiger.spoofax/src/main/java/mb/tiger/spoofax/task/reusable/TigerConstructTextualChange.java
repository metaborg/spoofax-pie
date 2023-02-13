package mb.tiger.spoofax.task.reusable;

import mb.common.result.Result;
import mb.stratego.common.Strategy;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import mb.transform.pie.ConstructTextualChange;

import javax.inject.Inject;

public class TigerConstructTextualChange extends ConstructTextualChange {

    @Inject
    public TigerConstructTextualChange(GetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(
            getStrategoRuntimeProvider,
            (ctx, ast) -> Result.ofOk(ast),
            "construct-textual-change",
            Strategy.strategy("pp-partial-Tiger-string"),
            Strategy.strategy("parenthesize"),
            Strategy.strategy("override-reconstruction"),
            Strategy.strategy("resugar")
        );
    }

    @Override
    public String getId() {
        return TigerConstructTextualChange.class.getSimpleName();
    }

}
