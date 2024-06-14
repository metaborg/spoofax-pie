package mb.sdf3.task;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.sdf3.stratego.Sdf3Context;
import mb.sdf3.Sdf3Scope;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.tuple.Tuple2;
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
        public final String strategoQualifier;
        public final Option<Tuple2<String, String>> placeholders;

        public Input(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, String strategoQualifier,
            Option<Tuple2<String, String>> placeholders) {
            this.astSupplier = astSupplier;
            this.strategoQualifier = strategoQualifier;
            this.placeholders = placeholders;
        }

        public Input(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, String strategoQualifier) {
            this(astSupplier, strategoQualifier, Option.ofNone());
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return astSupplier.equals(input.astSupplier) && strategoQualifier.equals(input.strategoQualifier) && placeholders.equals(input.placeholders);
        }

        @Override public int hashCode() {
            return Objects.hash(astSupplier, strategoQualifier, placeholders);
        }

        @Override public String toString() {
            return "Input{" +
                "astSupplier=" + astSupplier +
                ", strategoQualifier='" + strategoQualifier + '\'' +
                ", placeholders='" + placeholders + '\'' +
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
        return context.require(input.astSupplier).flatMapOrElse((ast) -> {
            final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(new Sdf3Context(Option.ofSome(input.strategoQualifier), input.placeholders));
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
