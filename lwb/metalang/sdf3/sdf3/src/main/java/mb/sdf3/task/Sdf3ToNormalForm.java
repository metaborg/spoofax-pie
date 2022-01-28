package mb.sdf3.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.sdf3.Sdf3Scope;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@Sdf3Scope
public class Sdf3ToNormalForm extends Sdf3AstStrategoTransformTaskDef<Sdf3AstStrategoTransformTaskDef.Input> {
    @Inject public Sdf3ToNormalForm(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider);
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override
    protected IStrategoTerm doExec(ExecContext context, Input input, StrategoRuntime strategoRuntime, IStrategoTerm ast) throws StrategoException, Exception {
        return strategoRuntime.invoke("module-to-normal-form", ast);
    }
}
