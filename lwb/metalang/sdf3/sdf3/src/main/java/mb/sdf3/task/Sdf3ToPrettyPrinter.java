package mb.sdf3.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.sdf3.stratego.Sdf3Context;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.spec.Sdf3Config;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Sdf3Scope
public class Sdf3ToPrettyPrinter implements TaskDef<Sdf3ToPrettyPrinter.Input, Result<IStrategoTerm, ?>> {
    public static class Input implements Serializable {
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;
        public final String strategyAffix;
        public final Sdf3Config sdf3Config;

        public Input(
            Supplier<? extends Result<IStrategoTerm, ?>> astSupplier,
            String strategyAffix,
            Sdf3Config sdf3Config
        ) {
            this.astSupplier = astSupplier;
            this.strategyAffix = strategyAffix;
            this.sdf3Config = sdf3Config;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return astSupplier.equals(input.astSupplier)
                && strategyAffix.equals(input.strategyAffix)
                && sdf3Config.equals(input.sdf3Config);
        }

        @Override public int hashCode() {
            return Objects.hash(astSupplier, strategyAffix, sdf3Config);
        }

        @Override public String toString() {
            return "Sdf3ToPrettyPrinter$Input{" +
                "astSupplier=" + astSupplier +
                ", strategyAffix='" + strategyAffix + '\'' +
                ", sdf3Config=" + sdf3Config +
                '}';
        }
    }

    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public Sdf3ToPrettyPrinter(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<IStrategoTerm, ?> exec(ExecContext context, Input input) throws Exception {
        final Sdf3Context sdf3Context = new Sdf3Context(
            input.strategyAffix,
            input.sdf3Config.placeholderPrefix,
            input.sdf3Config.placeholderSuffix
        );
        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(sdf3Context);
        return context.require(input.astSupplier).flatMapOrElse((ast) -> {
            try {
                ast = strategoRuntime.invoke("module-to-pp", ast, strategoRuntime.getTermFactory().makeString("2"));
                return Result.ofOk(ast);
            } catch(StrategoException e) {
                return Result.ofErr(e);
            }
        }, Result::ofErr);
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
