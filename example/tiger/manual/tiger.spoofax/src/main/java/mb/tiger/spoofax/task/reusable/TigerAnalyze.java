package mb.tiger.spoofax.task.reusable;

import mb.common.result.Result;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerConstraintAnalyzer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

@LanguageScope
public class TigerAnalyze implements TaskDef<TigerAnalyze.Input, Result<TigerAnalyze.Output, ?>> {
    public static class Input implements Serializable {
        public final ResourceKey resourceKey;
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;

        public Input(ResourceKey resourceKey, Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) {
            this.resourceKey = resourceKey;
            this.astSupplier = astSupplier;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return resourceKey.equals(input.resourceKey) && astSupplier.equals(input.astSupplier);
        }

        @Override public int hashCode() {
            return Objects.hash(resourceKey, astSupplier);
        }

        @Override public String toString() {
            return "Input(resourceKey=" + resourceKey + ", astProvider=" + astSupplier + ')';
        }
    }

    public static class Output implements Serializable {
        public final ConstraintAnalyzerContext context;
        public final SingleFileResult result;

        public Output(ConstraintAnalyzerContext context, SingleFileResult result) {
            this.result = result;
            this.context = context;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Output output = (Output)o;
            return context.equals(output.context) && result.equals(output.result);
        }

        @Override public int hashCode() {
            return Objects.hash(context, result);
        }

        @Override public String toString() {
            return "Output(context=" + context + ", result=" + result + ')';
        }
    }

    private final TigerConstraintAnalyzer constraintAnalyzer;

    @Inject
    public TigerAnalyze(TigerConstraintAnalyzer constraintAnalyzer) {
        this.constraintAnalyzer = constraintAnalyzer;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerAnalyzeSingle";
    }

    @Override
    public @Nullable Result<TigerAnalyze.Output, ?> exec(ExecContext context, Input input) throws IOException {
        return context.require(input.astSupplier)
            .mapCatching((ast) -> {
                final ConstraintAnalyzerContext constraintAnalyzerContext = new ConstraintAnalyzerContext();
                final SingleFileResult result = constraintAnalyzer.analyze(input.resourceKey, ast, constraintAnalyzerContext);
                return new Output(constraintAnalyzerContext, result);
            });
    }
}
