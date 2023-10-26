package mb.tim_runtime.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.tim_runtime.TimExecutionException;
import mb.tim_runtime.TimRuntimeScope;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@TimRuntimeScope
public class TimRuntimeExecuteProgram implements TaskDef<IStrategoTerm, Result<String, TimExecutionException>> {
    // TODO: should use the task instead of the provider directly, but currently
    // this seems to cause issues in PIE where it cannot resolve the resource
    // registry for the stratego ctree and other assets on subsequent runs
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public TimRuntimeExecuteProgram(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<String, TimExecutionException> exec(ExecContext context, IStrategoTerm input) {
        StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();

        try {
            IStrategoTerm result = strategoRuntime.invoke("tim-eval-to-string", input);
            if(result.getType() != TermType.STRING) {
                return Result.ofErr(TimExecutionException.timExecutionResultNotString(result.getType()));
            }

            String asString = TermUtils.toJavaString(result);
            return Result.ofOk(asString);
        } catch(StrategoException ex) {
            return Result.ofErr(TimExecutionException.timExecutionFail(ex));
        }
    }

    @Override public boolean shouldExecWhenAffected(IStrategoTerm input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
