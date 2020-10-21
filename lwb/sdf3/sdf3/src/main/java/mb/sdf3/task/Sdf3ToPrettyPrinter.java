package mb.sdf3.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.sdf3.Sdf3Context;
import mb.sdf3.Sdf3Scope;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;

@Sdf3Scope
public class Sdf3ToPrettyPrinter implements TaskDef<Sdf3ToPrettyPrinter.Input, Result<IStrategoTerm, ?>> {
    public static class Input implements Serializable {
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;
        public final String strategoQualifier;

        public Input(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, String strategoQualifier) {
            this.astSupplier = astSupplier;
            this.strategoQualifier = strategoQualifier;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return astSupplier.equals(input.astSupplier) && strategoQualifier.equals(input.strategoQualifier);
        }

        @Override public int hashCode() {
            return Objects.hash(astSupplier, strategoQualifier);
        }

        @Override public String toString() {
            return "Input{" +
                "astSupplier=" + astSupplier +
                ", strategoQualifier='" + strategoQualifier + '\'' +
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
        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(new Sdf3Context(input.strategoQualifier));
        return context.require(input.astSupplier).flatMapOrElse((ast) -> {
            try {
                ast = strategoRuntime.invoke("module-to-pp", ast);
                return Result.ofOk(ast);
            } catch(StrategoException e) {
                return Result.ofErr(e);
            }
        }, Result::ofErr);
    }
}
