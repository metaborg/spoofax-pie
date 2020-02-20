package mb.tiger.spoofax.task.reusable;

import mb.common.util.UncheckedException;
import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerConstraintAnalyzer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/**
 * @implNote Although Tiger is a single-file language, we implement the multi-file analysis variant here as well for
 * development/testing purposes.
 */
@LanguageScope
public class TigerAnalyzeMulti implements TaskDef<TigerAnalyzeMulti.Input, TigerAnalyzeMulti.@Nullable Output> {
    public static class Input implements Serializable {
        public final ResourcePath root;
        public final ResourceWalker walker;
        public final ResourceMatcher matcher;
        public final Function<Supplier<String>, @Nullable IStrategoTerm> astFunction;

        public Input(
            ResourcePath root,
            ResourceWalker walker,
            ResourceMatcher matcher,
            Function<Supplier<String>, @Nullable IStrategoTerm> astFunction
        ) {
            this.root = root;
            this.walker = walker;
            this.matcher = matcher;
            this.astFunction = astFunction;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return root.equals(input.root) &&
                walker.equals(input.walker) &&
                matcher.equals(input.matcher) &&
                astFunction.equals(input.astFunction);
        }

        @Override public int hashCode() {
            return Objects.hash(root, walker, matcher, astFunction);
        }

        @Override public String toString() {
            return "Input{" +
                "root=" + root +
                ", walker=" + walker +
                ", matcher=" + matcher +
                ", astTaskDef=" + astFunction +
                '}';
        }
    }

    public static class Output implements Serializable {
        public final ConstraintAnalyzerContext context;
        public final MultiFileResult result;

        public Output(ConstraintAnalyzerContext context, MultiFileResult result) {
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
            return "Output{" +
                "context=" + context +
                ", result=" + result +
                '}';
        }
    }

    private final TigerConstraintAnalyzer constraintAnalyzer;

    @Inject
    public TigerAnalyzeMulti(TigerConstraintAnalyzer constraintAnalyzer) {
        this.constraintAnalyzer = constraintAnalyzer;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerAnalyzeMulti";
    }

    @Override public @Nullable Output exec(ExecContext context, Input input) throws Exception {
        final HierarchicalResource root = context.require(input.root, ResourceStampers.modifiedDirRec(input.walker, input.matcher));

        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        try {
            root.walk(input.walker, input.matcher).forEach(file -> {
                try {
                    final @Nullable IStrategoTerm ast = context.require(input.astFunction, new ResourceStringSupplier(file.getPath()));
                    if(ast != null) {
                        asts.put(file.getKey(), ast);
                    }
                } catch(Exception e) {
                    throw new UncheckedException(e);
                }
            });
        } catch(UncheckedException e) {
            throw e.getCause();
        }

        final ConstraintAnalyzerContext constraintAnalyzerContext = new ConstraintAnalyzerContext();
        final MultiFileResult result = constraintAnalyzer.analyze(input.root, asts, constraintAnalyzerContext);
        return new Output(constraintAnalyzerContext, result);
    }
}
