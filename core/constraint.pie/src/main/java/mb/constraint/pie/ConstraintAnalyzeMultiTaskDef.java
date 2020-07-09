package mb.constraint.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

public abstract class ConstraintAnalyzeMultiTaskDef implements TaskDef<ConstraintAnalyzeMultiTaskDef.Input, Result<ConstraintAnalyzeMultiTaskDef.Output, ?>> {
    public static class Input implements Serializable {
        public final ResourcePath root;
        public final ResourceWalker walker;
        public final ResourceMatcher matcher;
        public final Function<Supplier<String>, ? extends Result<IStrategoTerm, ?>> astFunction;

        public Input(
            ResourcePath root,
            ResourceWalker walker,
            ResourceMatcher matcher,
            Function<Supplier<String>, ? extends Result<IStrategoTerm, ?>> astFunction
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
        public final KeyedMessages messagesFromAstProviders;
        public final ConstraintAnalyzerContext context;
        public final ConstraintAnalyzer.MultiFileResult result;

        public Output(KeyedMessages messagesFromAstProviders, ConstraintAnalyzerContext context, ConstraintAnalyzer.MultiFileResult result) {
            this.messagesFromAstProviders = messagesFromAstProviders;
            this.result = result;
            this.context = context;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Output output = (Output)o;
            return messagesFromAstProviders.equals(output.messagesFromAstProviders) &&
                context.equals(output.context) &&
                result.equals(output.result);
        }

        @Override public int hashCode() {
            return Objects.hash(messagesFromAstProviders, context, result);
        }

        @Override public String toString() {
            return "Output{" +
                "messagesFromAstProviders=" + messagesFromAstProviders +
                ", context=" + context +
                ", result=" + result +
                '}';
        }
    }

    public static class SingleFileOutput implements Serializable {
        public final ConstraintAnalyzerContext context;
        public final ConstraintAnalyzer.SingleFileResult result;

        public SingleFileOutput(ConstraintAnalyzerContext context, ConstraintAnalyzer.SingleFileResult result) {
            this.result = result;
            this.context = context;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final SingleFileOutput output = (SingleFileOutput)o;
            return context.equals(output.context) && result.equals(output.result);
        }

        @Override public int hashCode() {
            return Objects.hash(context, result);
        }

        @Override public String toString() {
            return "SingleFileOutput{" +
                "context=" + context +
                ", result=" + result +
                '}';
        }
    }

    protected abstract ConstraintAnalyzer.MultiFileResult analyze(ResourceKey root, HashMap<ResourceKey, IStrategoTerm> asts, ConstraintAnalyzerContext context) throws ConstraintAnalyzerException;

    @Override public Result<Output, ?> exec(ExecContext context, Input input) throws IOException {
        final HierarchicalResource root = context.require(input.root, ResourceStampers.modifiedDirRec(input.walker, input.matcher));
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        root.walk(input.walker, input.matcher).forEach(file -> context.require(input.astFunction, new ResourceStringSupplier(file.getPath()))
            .ifOk((ast) -> asts.put(file.getKey(), ast))
            .ifErr((e) -> messagesBuilder.addMessage("Getting AST for analysis failed", e, Severity.Error, file.getKey()))
        );
        try {
            final ConstraintAnalyzerContext constraintAnalyzerContext = new ConstraintAnalyzerContext();
            final ConstraintAnalyzer.MultiFileResult result = analyze(input.root, asts, constraintAnalyzerContext);
            return Result.ofOk(new Output(messagesBuilder.build(), constraintAnalyzerContext, result));
        } catch(ConstraintAnalyzerException e) {
            return Result.ofErr(e);
        }
    }


    public Supplier<Result<SingleFileOutput, ?>> createSingleFileOutputSupplier(Input input, ResourceKey resource) {
        return createSupplier(input).map(new SingleFileMapper(resource));
    }
}


class SingleFileMapper implements java.util.function.Function<Result<ConstraintAnalyzeMultiTaskDef.Output, ?>, Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>>, Serializable {
    private final ResourceKey resource;

    SingleFileMapper(ResourceKey resource) {
        this.resource = resource;
    }

    @Override
    public Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?> apply(Result<ConstraintAnalyzeMultiTaskDef.Output, ?> outputResult) {
        return outputResult.map(output -> {
            final ConstraintAnalyzer.@Nullable Result result = output.result.getResult(resource);
            if(result != null) {
                final Messages messages = new Messages(output.result.messages.getMessagesOfKey(resource));
                return new ConstraintAnalyzeMultiTaskDef.SingleFileOutput(output.context, new ConstraintAnalyzer.SingleFileResult(output.result.projectResult, result.ast, result.analysis, messages));
            } else {
                throw new RuntimeException("BUG: multi file result is missing a result for resource '" + resource + "' that was part of the input");
            }
        });
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SingleFileMapper that = (SingleFileMapper)o;
        return resource.equals(that.resource);
    }

    @Override public int hashCode() {
        return Objects.hash(resource);
    }

    @Override public String toString() {
        return "SingleFileMapper{" +
            "resource=" + resource +
            '}';
    }
}
