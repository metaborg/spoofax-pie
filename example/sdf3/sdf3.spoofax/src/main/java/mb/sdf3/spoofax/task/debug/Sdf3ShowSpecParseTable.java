package mb.sdf3.spoofax.task.debug;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import mb.sdf3.spoofax.task.Sdf3Desugar;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3SpecToParseTable;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@LanguageScope
public class Sdf3ShowSpecParseTable implements TaskDef<Sdf3ShowSpecParseTable.Args, CommandOutput> {
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

    private final ResourceService resourceService;
    private final Sdf3Parse parse;
    private final Sdf3Desugar desugar;
    private final Sdf3SpecToParseTable sdf3SpecToParseTable;

    @Inject
    public Sdf3ShowSpecParseTable(ResourceService resourceService, Sdf3Parse parse, Sdf3Desugar desugar, Sdf3SpecToParseTable sdf3SpecToParseTable) {
        this.resourceService = resourceService;
        this.parse = parse;
        this.desugar = desugar;
        this.sdf3SpecToParseTable = sdf3SpecToParseTable;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, Args args) throws Exception {
        final List<Supplier<@Nullable IStrategoTerm>> moduleSuppliers = resourceService.getHierarchicalResource(args.project)
            .walk(new TrueResourceWalker(), new PathResourceMatcher(new ExtensionsPathMatcher("tmpl", "sdf3")))
            .filter(file -> file.getPath() != args.mainFile) // Filter out main module, as it is supplied separately.
            .map(file -> desugar.createSupplier(parse.createAstSupplier(file.getKey())))
            .collect(Collectors.toList());
        final Sdf3SpecToParseTable.Args toParseTableArgs = new Sdf3SpecToParseTable.Args(
            desugar.createSupplier(parse.createAstSupplier(args.mainFile)),
            ListView.of(moduleSuppliers),
            new ParseTableConfiguration(false, false, true, false, false),
            false
        );
        final ParseTable parseTable = context.require(sdf3SpecToParseTable, toParseTableArgs);
        final IStrategoTerm ast = ParseTableIO.generateATerm(parseTable);
        return CommandOutput.of(CommandFeedback.showText(StrategoUtil.toString(ast), "Parse table for '" + args.mainFile + "' in project '" + args.project + "'"));
    }
}
