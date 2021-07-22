package mb.sdf3_ext_statix.task;

import mb.common.result.Result;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.sdf3_ext_statix.Sdf3ExtStatixScope;
import mb.stratego.pie.AstStrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.util.Set;

@Sdf3ExtStatixScope
public class Sdf3ExtStatixGenerateStatix extends AstStrategoTransformTaskDef {
    @Inject public Sdf3ExtStatixGenerateStatix(Sdf3ExtStatixGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "geninj-generate-statix");
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public boolean shouldExecWhenAffected(Supplier<? extends Result<IStrategoTerm, ?>> input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
