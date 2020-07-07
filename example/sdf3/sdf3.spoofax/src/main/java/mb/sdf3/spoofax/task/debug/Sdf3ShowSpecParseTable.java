package mb.sdf3.spoofax.task.debug;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.spoofax.task.Sdf3CreateSpec;
import mb.sdf3.spoofax.task.Sdf3SpecToParseTable;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoUtil;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@LanguageScope
public class Sdf3ShowSpecParseTable implements TaskDef<Sdf3ShowSpecParseTable.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourcePath project;
        public final ResourceKey mainFile;

        public Args(ResourcePath project, ResourceKey mainFile) {
            this.project = project;
            this.mainFile = mainFile;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return project.equals(args.project) &&
                mainFile.equals(args.mainFile);
        }

        @Override public int hashCode() {
            return Objects.hash(project, mainFile);
        }

        @Override public String toString() {
            return "Args{project=" + project + ", mainFile=" + mainFile + '}';
        }
    }

    private final Sdf3CreateSpec createSpec;
    private final Sdf3SpecToParseTable specToParseTable;

    @Inject public Sdf3ShowSpecParseTable(Sdf3CreateSpec createSpec, Sdf3SpecToParseTable specToParseTable) {
        this.createSpec = createSpec;
        this.specToParseTable = specToParseTable;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) {
        return context.require(specToParseTable, new Sdf3SpecToParseTable.Args(
            createSpec.createSupplier(new Sdf3CreateSpec.Input(args.project, args.mainFile)),
            new ParseTableConfiguration(false, false, true, false, false, false),
            false
        ))
            .mapCatching(ParseTableIO::generateATerm)
            .mapOrElse(
                ast -> CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(ast), "Parse table for '" + args.mainFile + "' in project '" + args.project + "'")),
                e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.mainFile)
            );
    }
}
