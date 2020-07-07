package mb.tiger.spoofax.task;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.tiger.spoofax.task.reusable.TigerListLiteralVals;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TigerCompileFile implements TaskDef<TigerCompileFile.Args, CommandFeedback> {
    public static class Args implements Serializable {
        final ResourcePath file;

        public Args(ResourcePath file) {
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;
            final Args other = (Args)obj;
            return file.equals(other.file);
        }

        @Override public int hashCode() {
            return Objects.hash(file);
        }

        @Override public String toString() {
            return file.toString();
        }
    }

    private final TigerParse parse;
    private final TigerListLiteralVals listLiteralVals;
    private final ResourceService resourceService;

    @Inject
    public TigerCompileFile(TigerParse parse, TigerListLiteralVals listLiteralVals, ResourceService resourceService) {
        this.parse = parse;
        this.listLiteralVals = listLiteralVals;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args input) {
        final ResourcePath file = input.file;
        final Supplier<Result<IStrategoTerm, JSGLR1ParseException>> astSupplier = parse.createAstSupplier(file);
        final Result<String, ?> listedLiteralVals = context.require(listLiteralVals, astSupplier);
        return listedLiteralVals
            .mapCatching((literalVals) -> {
                final ResourcePath generatedPath = file.replaceLeafExtension("literals.aterm");
                final HierarchicalResource generatedResource = resourceService.getHierarchicalResource(generatedPath);
                generatedResource.writeBytes(literalVals.getBytes(StandardCharsets.UTF_8));
                context.provide(generatedResource, ResourceStampers.hashFile());
                return generatedPath;
            })
            .mapOrElse(f -> CommandFeedback.of(ShowFeedback.showFile(f)), e -> CommandFeedback.ofTryExtractMessagesFrom(e, file));
    }

    @Override public Task<CommandFeedback> createTask(Args input) {
        return TaskDef.super.createTask(input);
    }
}
