package mb.tim_runtime.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.tim_runtime.TimRuntimeScope;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@TimRuntimeScope
public class TimRuntimePrettyPrint implements TaskDef<IStrategoTerm, Result<String, ?>> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject
    public TimRuntimePrettyPrint(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<String, ?> exec(ExecContext context, IStrategoTerm input) {
        StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();

        try {
            final IStrategoTerm printed = strategoRuntime.invoke("pp-partial-tim-string", input);
            return Result.ofOk(((IStrategoString)printed).stringValue());
        } catch(StrategoException e) {
            return Result.ofErr(e);
        }
    }

    @Override public boolean shouldExecWhenAffected(IStrategoTerm input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
