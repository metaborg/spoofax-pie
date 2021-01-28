package mb.constraint.pie;

import mb.common.result.Result;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public abstract class ConstraintAnalyzeTaskDef implements TaskDef<ConstraintAnalyzeTaskDef.Input, Result<ConstraintAnalyzeTaskDef.Output, ?>> {
    public static class Input implements Serializable {
        public final ResourceKey resource;
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;

        public Input(ResourceKey resource, Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) {
            this.resource = resource;
            this.astSupplier = astSupplier;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return resource.equals(input.resource) && astSupplier.equals(input.astSupplier);
        }

        @Override public int hashCode() {
            return Objects.hash(resource, astSupplier);
        }

        @Override public String toString() {
            return "Input(root=" + resource + ", astProvider=" + astSupplier + ')';
        }
    }

    public static class Output implements Serializable {
        public final ConstraintAnalyzerContext context;
        public final SingleFileResult result;

        public Output(ConstraintAnalyzerContext context, SingleFileResult result) {
            this.result = result;
            this.context = context;
        }

        @Override public boolean equals(@Nullable Object o) {
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

    protected abstract SingleFileResult analyze(ExecContext context, ResourceKey resource, IStrategoTerm ast, ConstraintAnalyzerContext constraintAnalyzerContext) throws Exception;

    @Override
    public Result<Output, ?> exec(ExecContext context, Input input) throws Exception {
        return context.require(input.astSupplier)
            .mapCatchingOrRethrow((ast) -> {
                final ConstraintAnalyzerContext constraintAnalyzerContext = new ConstraintAnalyzerContext(false, input.resource);
                final SingleFileResult result = analyze(context, input.resource, ast, constraintAnalyzerContext);
                return new Output(constraintAnalyzerContext, result);
            }, ConstraintAnalyzerException.class);
    }
}
