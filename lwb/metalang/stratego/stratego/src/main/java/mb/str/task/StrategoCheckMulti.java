package mb.str.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.str.StrategoScope;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.config.StrategoConfigurator;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Objects;

@StrategoScope
public class StrategoCheckMulti implements TaskDef<StrategoCheckMulti.Input, KeyedMessages> {
    public static class Input implements Serializable {
        public final ResourcePath root;
        public final ResourceWalker walker;
        public final ResourceMatcher matcher;
        public final ArrayList<STask<?>> originTasks;

        public Input(
            ResourcePath root,
            ResourceWalker walker,
            ResourceMatcher matcher,
            ArrayList<STask<?>> originTasks
        ) {
            this.root = root;
            this.walker = walker;
            this.matcher = matcher;
            this.originTasks = originTasks;
        }

        public Input(
            ResourcePath root,
            ResourceWalker walker,
            ResourceMatcher matcher
        ) {
            this(root, walker, matcher, new ArrayList<>());
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return root.equals(input.root) &&
                walker.equals(input.walker) &&
                matcher.equals(input.matcher) &&
                originTasks.equals(input.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(root, walker, matcher, originTasks);
        }

        @Override public String toString() {
            return "Input{" +
                "root=" + root +
                ", walker=" + walker +
                ", matcher=" + matcher +
                ", originTasks=" + originTasks +
                '}';
        }
    }


    private final StrategoParse parse;
    private final StrategoAnalyze analyze;
    private final StrategoConfigurator strategoConfigurator;


    @Inject public StrategoCheckMulti(
        StrategoParse parse,
        StrategoAnalyze analyze,
        StrategoConfigurator strategoConfigurator
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.strategoConfigurator = strategoConfigurator;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws Exception {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final HierarchicalResource root = context.require(input.root, ResourceStampers.modifiedDirRec(input.walker, input.matcher));
        try {
            root.walk(input.walker, input.matcher).forEach(file -> {
                final ResourcePath filePath = file.getPath();
                // TODO: instead of requiring all suppliers for each file to parse, only require the supplier that corresponds to a certain file.
                final Messages messages = context.require(parse.createMessagesSupplier(new ResourceStringSupplierWithOriginTasks(filePath, input.originTasks)));
                messagesBuilder.addMessages(filePath, messages);
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final StrategoAnalyzeConfig config = strategoConfigurator.getAnalyzeConfig(input.root);
        final HierarchicalResource mainFile = context.require(config.mainFile, ResourceStampers.<HierarchicalResource>exists());
        if(mainFile.exists()) {
            final KeyedMessages analysisMessages = context.require(analyze, new StrategoAnalyze.Input(config, input.originTasks));
            messagesBuilder.addMessages(analysisMessages);
        }

        return messagesBuilder.build();
    }
}

class ResourceStringSupplierWithOriginTasks implements Supplier<String> {
    private final ResourcePath resource;
    private final ArrayList<STask<?>> originTasks;

    ResourceStringSupplierWithOriginTasks(ResourcePath resource, ArrayList<STask<?>> originTasks) {
        this.resource = resource;
        this.originTasks = originTasks;
    }

    @Override public String get(ExecContext context) {
        originTasks.forEach(t -> context.require(t, OutputStampers.inconsequential()));
        try {
            return context.require(resource).readString();
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ResourceStringSupplierWithOriginTasks that = (ResourceStringSupplierWithOriginTasks)o;
        if(!resource.equals(that.resource)) return false;
        return originTasks.equals(that.originTasks);
    }

    @Override public int hashCode() {
        int result = resource.hashCode();
        result = 31 * result + originTasks.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ResourceStringSupplierWithOriginTasks{" +
            "resource=" + resource +
            ", originTasks=" + originTasks +
            '}';
    }
}
