package mb.sdf3.task;

import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.sdf3.Sdf3Scope;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@Sdf3Scope
public class Sdf3ToPrettyPrinter extends Sdf3AstStrategoTransformTaskDef<Sdf3AstStrategoTransformTaskDef.Input> {

    @Inject public Sdf3ToPrettyPrinter(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider);
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    protected IStrategoTerm doExec(ExecContext context, Input input, StrategoRuntime strategoRuntime, IStrategoTerm ast) throws StrategoException, Exception {
        return strategoRuntime.invoke("module-to-pp", ast, strategoRuntime.getTermFactory().makeString("2"));
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
