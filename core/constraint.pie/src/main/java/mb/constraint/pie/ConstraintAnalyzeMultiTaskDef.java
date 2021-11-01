package mb.constraint.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.common.util.MapView;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.SerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

public abstract class ConstraintAnalyzeMultiTaskDef implements TaskDef<ConstraintAnalyzeMultiTaskDef.Input, Result<ConstraintAnalyzeMultiTaskDef.Output, ?>> {
    public static class Input implements Serializable {
        public final ResourcePath root;
        public final Function<ResourcePath, ? extends MapView<ResourceKey, ? extends Supplier<? extends Result<IStrategoTerm, ?>>>> astSuppliersFunction;

        public Input(
            ResourcePath root,
            Function<ResourcePath, ? extends MapView<ResourceKey, ? extends Supplier<? extends Result<IStrategoTerm, ?>>>> astSuppliersFunction
        ) {
            this.root = root;
            this.astSuppliersFunction = astSuppliersFunction;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!root.equals(input.root)) return false;
            return astSuppliersFunction.equals(input.astSuppliersFunction);
        }

        @Override public int hashCode() {
            int result = root.hashCode();
            result = 31 * result + astSuppliersFunction.hashCode();
            return result;
        }

        @Override public String toString() {
            return "ConstraintAnalyzeMultiTaskDef$Input{" +
                "root=" + root +
                ", astsFunction=" + astSuppliersFunction +
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

        @Override public boolean equals(@Nullable Object o) {
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

        @Override public boolean equals(@Nullable Object o) {
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

    protected abstract ConstraintAnalyzer.MultiFileResult analyze(
        ExecContext context,
        ResourcePath root,
        MapView<ResourceKey, IStrategoTerm> asts,
        ConstraintAnalyzerContext constraintAnalyzerContext
    ) throws Exception;

    @Override public Result<Output, ?> exec(ExecContext context, Input input) throws Exception {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        context.require(input.astSuppliersFunction, input.root).forEach(entry -> {
            final ResourceKey file = entry.getKey();
            final Result<IStrategoTerm, ?> result = context.require(entry.getValue());
            result.ifElse(
                ast -> asts.put(file, ast),
                e -> messagesBuilder.addMessage("Getting AST for analysis failed", e, Severity.Error, file)
            );
        });
        try {
            final ConstraintAnalyzerContext constraintAnalyzerContext = getConstraintAnalyzerContext(context, input.root);
            final ConstraintAnalyzer.MultiFileResult result = analyze(context, input.root, MapView.of(asts), constraintAnalyzerContext);
            context.setInternalObject(constraintAnalyzerContext);
            return Result.ofOk(new Output(messagesBuilder.build(), constraintAnalyzerContext, result));
        } catch(ConstraintAnalyzerException e) {
            return Result.ofErr(e);
        }
    }

    private ConstraintAnalyzerContext getConstraintAnalyzerContext(ExecContext context, ResourcePath rootDirectory) {
        final @Nullable Serializable obj = context.getInternalObject();
        if(obj instanceof ConstraintAnalyzerContext) {
            return (ConstraintAnalyzerContext)obj;
        }
        return new ConstraintAnalyzerContext(true, rootDirectory);
    }


    public Supplier<Result<SingleFileOutput, ?>> createSingleFileOutputSupplier(Input input, ResourceKey resource) {
        return createSupplier(input).map(new SingleFileMapper(resource));
    }
}


class SingleFileMapper implements SerializableFunction<Result<ConstraintAnalyzeMultiTaskDef.Output, ?>, Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> {
    private final ResourceKey resource;

    SingleFileMapper(ResourceKey resource) {
        this.resource = resource;
    }

    @Override
    public Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?> apply(Result<ConstraintAnalyzeMultiTaskDef.Output, ?> outputResult) {
        return outputResult.flatMapOrElse(output -> {
            final ConstraintAnalyzer.@Nullable Result result = output.result.getResult(resource);
            if(result != null) {
                final Messages messages = new Messages(output.result.messages.getMessagesOfKey(resource));
                return Result.ofOk(new ConstraintAnalyzeMultiTaskDef.SingleFileOutput(output.context, new ConstraintAnalyzer.SingleFileResult(
                    output.result.projectResult,
                    result.resource,
                    result.parsedAst,
                    result.analyzedAst,
                    result.analysis,
                    messages
                )));
            } else {
                return Result.ofErr(new MissingResultException(resource));
            }
        }, Result::ofErr);
    }

    @Override public boolean equals(@Nullable Object o) {
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
