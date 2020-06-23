package mb.tiger.spoofax.task;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandFeedbacks;
import mb.spoofax.core.language.command.CommandFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@LanguageScope
public class TigerCompileFileTaskDef implements TaskDef<TigerCompileFileTaskDef.Args, CommandFeedback> {
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


    @Inject public TigerCompileFileTaskDef(TigerParse parse, TigerListLiteralVals listLiteralVals, ResourceService resourceService) {
        this.parse = parse;
        this.listLiteralVals = listLiteralVals;
        this.resourceService = resourceService;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args input) throws Exception {
        final ResourcePath file = input.file;

        final Supplier<@Nullable IStrategoTerm> astSupplier = parse.createAstProvider(file);
        final @Nullable String literalsStr = context.require(listLiteralVals, astSupplier);
        //noinspection ConstantConditions (literalsStr can really be null)
        if(literalsStr == null) {
            return new CommandFeedback(ListView.of());
        }

        final ResourcePath generatedPath = file.replaceLeafExtension("literals.aterm");
        final HierarchicalResource generatedResource = resourceService.getHierarchicalResource(generatedPath);
        generatedResource.writeBytes(literalsStr.getBytes(StandardCharsets.UTF_8));
        context.provide(generatedResource, ResourceStampers.hashFile());

        return new CommandFeedback(ListView.of(CommandFeedbacks.showFile(generatedPath, null)));
    }
}
