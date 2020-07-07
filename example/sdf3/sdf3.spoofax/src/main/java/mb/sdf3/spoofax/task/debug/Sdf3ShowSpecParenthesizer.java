package mb.sdf3.spoofax.task.debug;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.spoofax.task.Sdf3CreateSpec;
import mb.sdf3.spoofax.task.Sdf3ParseTableToParenthesizer;
import mb.sdf3.spoofax.task.Sdf3SpecToParseTable;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.stratego.common.StrategoRuntime;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;

@LanguageScope
public class Sdf3ShowSpecParenthesizer extends ProvideOutputShared implements TaskDef<Sdf3ShowSpecParenthesizer.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourcePath project;
        public final ResourceKey mainFile;
        public final boolean concrete;

        public Args(ResourcePath project, ResourceKey mainFile, boolean concrete) {
            this.project = project;
            this.mainFile = mainFile;
            this.concrete = concrete;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return concrete == args.concrete &&
                project.equals(args.project) &&
                mainFile.equals(args.mainFile);
        }

        @Override public int hashCode() {
            return Objects.hash(project, mainFile, concrete);
        }

        @Override public String toString() {
            return "Args{project=" + project + ", mainFile=" + mainFile + ", concrete=" + concrete + '}';
        }
    }

    private final Sdf3CreateSpec createSpec;
    private final Sdf3SpecToParseTable specToParseTable;
    private final Sdf3ParseTableToParenthesizer specToParenthesizer;

    @Inject public Sdf3ShowSpecParenthesizer(
        Provider<StrategoRuntime> strategoRuntimeProvider,
        Sdf3CreateSpec createSpec,
        Sdf3SpecToParseTable specToParseTable,
        Sdf3ParseTableToParenthesizer sdf3ParseTableToParenthesizer
    ) {
        super(strategoRuntimeProvider, "pp-stratego-string", "parenthesizer");
        this.createSpec = createSpec;
        this.specToParseTable = specToParseTable;
        this.specToParenthesizer = sdf3ParseTableToParenthesizer;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        final Supplier<? extends Result<ParseTable, ?>> parseTableSupplier = specToParseTable.createSupplier(new Sdf3SpecToParseTable.Args(
            createSpec.createSupplier(new Sdf3CreateSpec.Input(args.project, args.mainFile)),
            new ParseTableConfiguration(false, false, true, false, false, false),
            false
        ));
        return context.require(specToParenthesizer, new Sdf3ParseTableToParenthesizer.Args(parseTableSupplier, "parenthesizer"))
            .mapOrElse(ast -> provideOutput(args.concrete, ast, args.mainFile), e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.mainFile));
    }
}
