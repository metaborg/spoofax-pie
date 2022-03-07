package mb.sdf3_ext_dynamix.task;

import mb.common.result.Result;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.sdf3_ext_dynamix.Sdf3ExtDynamixScope;
import mb.stratego.pie.AstStrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.util.Set;

@Sdf3ExtDynamixScope
public class Sdf3ExtDynamixGenerateDynamix extends AstStrategoTransformTaskDef {
    @Inject public Sdf3ExtDynamixGenerateDynamix(Sdf3ExtDynamixGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "generate-dynamix-signature");
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public boolean shouldExecWhenAffected(Supplier<? extends Result<IStrategoTerm, ?>> input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
