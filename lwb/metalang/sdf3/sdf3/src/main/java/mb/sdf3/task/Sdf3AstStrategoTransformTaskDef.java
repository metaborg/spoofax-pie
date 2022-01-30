package mb.sdf3.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.stratego.Sdf3Context;
import mb.sdf3.task.spec.Sdf3Config;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract task definition similar to {@link AstStrategoTransformTaskDef}
 * but injects a {@link Sdf3Context} into the {@link StrategoRuntime}.
 */
public abstract class Sdf3AstStrategoTransformTaskDef<I extends Sdf3AstStrategoTransformTaskDef.Input> implements TaskDef<I, Result<IStrategoTerm, ?>> {
    public /* open */ static class Input implements Serializable {
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;
        public final Sdf3Config sdf3Config;
        public final String strategyAffix;

        public Input(
            Supplier<? extends Result<IStrategoTerm, ?>> astSupplier,
            Sdf3Config sdf3Config,
            String strategyAffix
        ) {
            this.astSupplier = astSupplier;
            this.sdf3Config = sdf3Config;
            this.strategyAffix = strategyAffix;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return astSupplier.equals(input.astSupplier)
                && sdf3Config.equals(input.sdf3Config)
                && strategyAffix.equals(input.strategyAffix);
        }

        @Override public int hashCode() {
            return Objects.hash(astSupplier, sdf3Config, strategyAffix);
        }

        @Override public String toString() {
            return "Sdf3AstStrategoTransformTaskDef$Input{" +
                "astSupplier=" + astSupplier +
                ", sdf3Config=" + sdf3Config +
                ", strategyAffix='" + strategyAffix + '\'' +
                '}';
        }
    }

    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    protected Sdf3AstStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public Result<IStrategoTerm, ?> exec(ExecContext context, I input) throws Exception {
        final Sdf3Context sdf3Context = new Sdf3Context(
            input.strategyAffix,
            input.sdf3Config.placeholderPrefix,
            input.sdf3Config.placeholderSuffix
        );
        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(sdf3Context);
        return context.require(input.astSupplier).flatMapOrElse((ast) -> {
            try {
                ast = doExec(context, input, strategoRuntime, ast);
                return Result.ofOk(ast);
            } catch(Exception e) {
                return Result.ofErr(e);
            }
        }, Result::ofErr);
    }

    protected abstract IStrategoTerm doExec(ExecContext context, I input, StrategoRuntime strategoRuntime, IStrategoTerm ast) throws StrategoException, Exception;
}
