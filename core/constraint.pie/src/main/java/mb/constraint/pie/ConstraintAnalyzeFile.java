package mb.constraint.pie;

import mb.common.result.Result;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

public abstract class ConstraintAnalyzeFile implements TaskDef<ConstraintAnalyzeFile.Input, Result<ConstraintAnalyzeFile.Output, ?>> {
    public static final class Input implements Serializable {
        public final ResourcePath rootDirectory;
        public final ResourceKey file;

        public Input(ResourcePath rootDirectory, ResourceKey file) {
            this.rootDirectory = rootDirectory;
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            if(!rootDirectory.equals(input.rootDirectory)) return false;
            return file.equals(input.file);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + file.hashCode();
            return result;
        }

        @Override public String toString() {
            return "ConstraintAnalyzeFile.Input{" +
                "rootDirectory=" + rootDirectory +
                ", file=" + file +
                '}';
        }
    }

    public static final class Output implements Serializable {
        public final ConstraintAnalyzerContext context;
        public final IStrategoTerm ast;
        public final IStrategoTerm analysis;

        public Output(ConstraintAnalyzerContext context, IStrategoTerm ast, IStrategoTerm analysis) {
            this.context = context;
            this.ast = ast;
            this.analysis = analysis;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Output output = (Output)o;
            if(!context.equals(output.context)) return false;
            if(!ast.equals(output.ast)) return false;
            return analysis.equals(output.analysis);
        }

        @Override public int hashCode() {
            int result = context.hashCode();
            result = 31 * result + ast.hashCode();
            result = 31 * result + analysis.hashCode();
            return result;
        }
    }
}
