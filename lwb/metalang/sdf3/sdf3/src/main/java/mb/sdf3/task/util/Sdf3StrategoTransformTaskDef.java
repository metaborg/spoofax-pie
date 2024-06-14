package mb.sdf3.task.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.tuple.Tuple2;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.ValueSupplier;
import mb.sdf3.stratego.Sdf3Context;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.Strategy;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import mb.stratego.pie.StrategoTransformTaskDef;

public abstract class Sdf3StrategoTransformTaskDef extends StrategoTransformTaskDef<Sdf3StrategoTransformTaskDef.Input> {
    public static class Input implements Serializable {
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;
        public final Option<String> strategoQualifier;
        public final Option<Tuple2<String, String>> placeholders;

        public Input(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, Option<String> strategoQualifier,
            Option<Tuple2<String, String>> placeholders) {
            this.astSupplier = astSupplier;
            this.strategoQualifier = strategoQualifier;
            this.placeholders = placeholders;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Sdf3StrategoTransformTaskDef.Input input = (Sdf3StrategoTransformTaskDef.Input) o;
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

    public Sdf3StrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider,
        ListView<String> strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    public Sdf3StrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider,
        String... strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    public Sdf3StrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, Strategy... strategies) {
        super(getStrategoRuntimeProvider, strategies);
    }

    @Override protected StrategoRuntime getStrategoRuntime(ExecContext context, Input input) {
        return super.getStrategoRuntime(context, input).addContextObject(new Sdf3Context(input.strategoQualifier, input.placeholders));
    }

    @Override protected Result<IStrategoTerm, ?> getAst(ExecContext context, Input input) {
        return input.astSupplier.get(context);
    }

    @Override public boolean shouldExecWhenAffected(Supplier<? extends Result<Input, ?>> input, Set<?> tags) {
        return shouldExecWhenAffected(tags);
    }

    public boolean shouldExecWhenAffected(Set<?> tags) {
        return true;
    }

    public static Supplier<? extends Result<Input, ?>> inputSupplier(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, String strategoQualifier,
        Option<Tuple2<String, String>> placeholders) {
        return new ValueSupplier<>(Result.ofOk(new Input(astSupplier, Option.ofSome(strategoQualifier), placeholders)));
    }

    public static Supplier<? extends Result<Input, ?>> inputSupplier(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, String strategoQualifier) {
        return new ValueSupplier<>(Result.ofOk(new Input(astSupplier, Option.ofSome(strategoQualifier), Option.ofNone())));
    }

    public static Supplier<? extends Result<Input, ?>> inputSupplier(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, Option<Tuple2<String, String>> placeholders) {
        return new ValueSupplier<>(Result.ofOk(new Input(astSupplier, Option.ofNone(), placeholders)));
    }

    public static Supplier<? extends Result<Input, ?>> inputSupplier(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) {
        return new ValueSupplier<>(Result.ofOk(new Input(astSupplier, Option.ofNone(), Option.ofNone())));
    }
}
