package mb.sdf3_ext_statix.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.sdf3_ext_statix.Sdf3ExtStatixScope;
import mb.sdf3_ext_statix.stratego.Sdf3ExtStatixContext;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Sdf3ExtStatixScope
public class Sdf3ExtStatixGenerateStratego implements TaskDef<Sdf3ExtStatixGenerateStratego.Input, Result<IStrategoTerm, ?>> {
    public static class Input implements Serializable {
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;
        public final String strategyAffix;

        public Input(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, String strategyAffix) {
            this.astSupplier = astSupplier;
            this.strategyAffix = strategyAffix;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return astSupplier.equals(input.astSupplier) && strategyAffix.equals(input.strategyAffix);
        }

        @Override public int hashCode() {
            return Objects.hash(astSupplier, strategyAffix);
        }

        @Override public String toString() {
            return "Sdf3ExtStatixGenerateStratego$Input{" +
                "astSupplier=" + astSupplier +
                ", strategoQualifier='" + strategyAffix + '\'' +
                '}';
        }
    }

    private final Sdf3ExtStatixGetStrategoRuntimeProvider getStrategoRuntimeProvider;

    @Inject public Sdf3ExtStatixGenerateStratego(Sdf3ExtStatixGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<IStrategoTerm, ?> exec(ExecContext context, Input input) throws Exception {
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get()
            .addContextObject(new Sdf3ExtStatixContext(input.strategyAffix));
        return context.require(input.astSupplier).flatMapOrElse((ast) -> {
            try {
                ast = strategoRuntime.invoke("geninj-generate-stratego", ast, strategoRuntime.getTermFactory().makeString("2"));
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
