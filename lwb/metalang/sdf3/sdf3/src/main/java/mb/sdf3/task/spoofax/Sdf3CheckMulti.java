package mb.sdf3.task.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.Sdf3SpecConfigFunctionWrapper;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.util.Sdf3Util;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

@Sdf3Scope
public class Sdf3CheckMulti implements TaskDef<Sdf3CheckMulti.Input, KeyedMessages> {
    public static class Input implements Serializable {
        public final ResourcePath root;
        public final ResourceWalker walker;
        public final ResourceMatcher matcher;

        public Input(
            ResourcePath root,
            ResourceWalker walker,
            ResourceMatcher matcher
        ) {
            this.root = root;
            this.walker = walker;
            this.matcher = matcher;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!root.equals(input.root)) return false;
            if(!walker.equals(input.walker)) return false;
            return matcher.equals(input.matcher);
        }

        @Override public int hashCode() {
            int result = root.hashCode();
            result = 31 * result + walker.hashCode();
            result = 31 * result + matcher.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Input{" +
                "root=" + root +
                ", walker=" + walker +
                ", matcher=" + matcher +
                '}';
        }
    }


    private final Sdf3ClassLoaderResources classLoaderResources;
    private final Sdf3SpecConfigFunctionWrapper configFunction;
    private final Sdf3Parse parse;
    private final Sdf3AnalyzeMulti analyze;


    @Inject public Sdf3CheckMulti(
        Sdf3ClassLoaderResources classLoaderResources,
        Sdf3SpecConfigFunctionWrapper configFunction,
        Sdf3Parse parse,
        Sdf3AnalyzeMulti analyze
    ) {
        this.classLoaderResources = classLoaderResources;
        this.configFunction = configFunction;
        this.parse = parse;
        this.analyze = analyze;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws IOException {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        return configFunction.get().apply(context, input.root).mapThrowingOrElse(
            o -> o.mapThrowingOrElse(
                c -> check(context, c),
                KeyedMessages::of // SDF3 is not configured, do not need to check.
            ),
            // TODO: should we propagate configuration errors here? Every task that requires some configuration will
            //       propagate the same configuration errors, which would lead to duplicates.
            e -> KeyedMessages.ofTryExtractMessagesFrom(e, input.root).orElse(KeyedMessages.of())
        );
    }

    private KeyedMessages check(ExecContext context, Sdf3SpecConfig config) throws IOException {
        final ResourceWalker walker = Sdf3Util.createResourceWalker();
        final ResourceMatcher matcher = Sdf3Util.createResourceMatcher();

        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final HierarchicalResource root = context.require(config.rootDirectory, ResourceStampers.modifiedDirRec(walker, matcher));

        try {
            try(Stream<? extends HierarchicalResource> stream = root.walk(walker, matcher)) {
                stream.forEach(file -> {
                    final ResourcePath filePath = file.getPath();
                    final Messages messages = context.require(parse.createMessagesSupplier(filePath));
                    messagesBuilder.addMessages(filePath, messages);
                });
            }
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final Sdf3AnalyzeMulti.Input analyzeInput = new mb.sdf3.task.Sdf3AnalyzeMulti.Input(config.rootDirectory, walker, matcher, parse.createRecoverableAstFunction());
        final Result<Sdf3AnalyzeMulti.Output, ?> analysisResult = context.require(analyze, analyzeInput);
        analysisResult
            .ifOk(output -> {
                messagesBuilder.addMessages(output.result.messages);
                messagesBuilder.addMessages(output.messagesFromAstProviders);
            })
            .ifErr(e -> messagesBuilder.addMessage("SDF3 project-wide analysis failed unexpectedly", e, Severity.Error, config.rootDirectory));

        return messagesBuilder.build();
    }
}
