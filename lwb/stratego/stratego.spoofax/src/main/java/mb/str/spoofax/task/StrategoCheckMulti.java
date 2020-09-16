package mb.str.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.core.language.LanguageScope;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Objects;

@LanguageScope
public class StrategoCheckMulti implements TaskDef<StrategoCheckMulti.Input, KeyedMessages> {
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

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final StrategoCheckMulti.Input input = (StrategoCheckMulti.Input)o;
            return root.equals(input.root) && walker.equals(input.walker) && matcher.equals(input.matcher);
        }

        @Override public int hashCode() {
            return Objects.hash(root, walker, matcher);
        }

        @Override public String toString() {
            return "Input{" +
                "root=" + root +
                ", walker=" + walker +
                ", matcher=" + matcher +
                '}';
        }
    }


    private final StrategoParse parse;
    private final StrategoAnalyze analyze;


    @Inject public StrategoCheckMulti(StrategoParse parse, StrategoAnalyze analyze) {
        this.parse = parse;
        this.analyze = analyze;
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
                try {
                    final Messages messages = context.require(parse.createMessagesSupplier(filePath));
                    messagesBuilder.addMessages(filePath, messages);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final ResourcePath mainFilePath = input.root.appendRelativePath("main.str");
        final HierarchicalResource mainFile = context.require(mainFilePath, ResourceStampers.<HierarchicalResource>exists());
        if(mainFile.exists()) {
            // TODO: do not hardcode this
            final ArrayList<ResourcePath> includeDirs = new ArrayList<>();
            includeDirs.add(input.root);
            final ArrayList<String> builtinLibs = new ArrayList<>();
            builtinLibs.add("stratego-lib");
            final KeyedMessages analysisMessages = context.require(analyze, new StrategoAnalyze.Args(input.root, mainFilePath, includeDirs, builtinLibs, new ArrayList<>()));
            messagesBuilder.addMessages(analysisMessages);
        }

        return messagesBuilder.build();
    }
}
