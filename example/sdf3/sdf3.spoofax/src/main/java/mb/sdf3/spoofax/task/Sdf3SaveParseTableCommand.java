package mb.sdf3.spoofax.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * Command to save an SDF3 parse table to a file.
 */
@LanguageScope
public class Sdf3SaveParseTableCommand implements TaskDef<Sdf3SaveParseTableCommand.Args, CommandFeedback> {

    public static class Args implements Serializable {
        public final ResourceKey input;
        public final ResourceKey output;
        public final ResourcePath includePaths;
        public final ResourceKey includeFiles;
//        public final ResourcePath projectPath;

        public Args(ResourceKey inputx, ResourceKey outputx, ResourcePath includePaths, ResourceKey includeFiles) {
            this.input = inputx;
            this.output = outputx;
//            this.projectPath = projectPath;
            this.includePaths = includePaths;
            this.includeFiles = includeFiles;
        }

        @Override public boolean equals(@Nullable Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;
            final Args other = (Args)obj;
            return this.input.equals(other.input)
                && this.output.equals(other.output)
                && this.includePaths.equals(other.includePaths)
                && this.includeFiles.equals(other.includeFiles);
//                && this.projectPath.equals(other.projectPath);
        }

        @Override public int hashCode() {
            return Objects.hash(
                input,
                output,
                includePaths,
                includeFiles
//                projectPath
            );
        }

        @Override public String toString() {
            return input + " -> " + output + " (including: " + includeFiles + ", " + includePaths + ")";
        }
    }

    private final ResourceService resourceService;
    private final Sdf3CreateSpec createSpec;
    private final Sdf3SpecToParseTable specToParseTableTaskDef;

    @Inject public Sdf3SaveParseTableCommand(ResourceService resourceService, Sdf3CreateSpec createSpec, Sdf3SpecToParseTable specToParseTableTaskDef) {
        this.resourceService = resourceService;
        this.createSpec = createSpec;
        this.specToParseTableTaskDef = specToParseTableTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        final Supplier<Sdf3Spec> specSupplier = createSpec.createSupplier(new Sdf3CreateSpec.Input(args.input,
            args.includePaths != null ? Arrays.asList(args.includePaths) : Collections.emptyList(),
            args.includeFiles != null ? Arrays.asList(args.includeFiles) : Collections.emptyList()));
        final ParseTableConfiguration parseTableConfiguration = new ParseTableConfiguration(false, false, true, false, false, false);

        final Result<ParseTable, ?> parseTableResult = context.require(specToParseTableTaskDef.createTask(new Sdf3SpecToParseTable.Args(
            specSupplier, parseTableConfiguration, false
        )));

        return parseTableResult
            .mapCatching(parseTable -> {
                final WritableResource outputResource = resourceService.getWritableResource(args.output);
                final IStrategoTerm parseTableTerm = ParseTableIO.generateATerm(parseTable);
                try (final OutputStream outputStream = outputResource.openWrite()) {
                    try (final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                        try (final BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {
                            parseTableTerm.writeAsString(writer);
                        }
                    }
                }
                context.provide(outputResource, ResourceStampers.hashFile());
                return outputResource.getKey();
            })
            .mapOrElse(f -> CommandFeedback.of(ShowFeedback.showFile(f)), e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.input));
    }
}
