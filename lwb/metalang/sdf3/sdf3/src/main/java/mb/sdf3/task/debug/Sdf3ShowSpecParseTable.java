package mb.sdf3.task.debug;

import mb.aterm.common.TermToString;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.spec.Sdf3Config;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import mb.sdf3.task.spoofax.Sdf3ConfigSupplierWrapper;
import mb.sdf3.task.spoofax.Sdf3SpecConfigFunctionWrapper;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.metaborg.sdf2table.parsetable.ParseTable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.NoSuchElementException;

@Sdf3Scope
public class Sdf3ShowSpecParseTable implements TaskDef<Sdf3ShowSpecParseTable.Args, CommandFeedback> {
    public static class Args implements Serializable {
        // TODO: this should take a Sdf3SpecConfig directly, which must be assignable from CLI and such, but this is not possible yet.
        public final ResourcePath root;
        public final String strategyAffix;

        public Args(ResourcePath root, String strategyAffix) {
            this.root = root;
            this.strategyAffix = strategyAffix;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args that = (Args)o;
            return this.root.equals(that.root)
                && this.strategyAffix.equals(that.strategyAffix);
        }

        @Override public int hashCode() {
            return root.hashCode();
        }

        @Override public String toString() {
            return "Sdf3ShowSpecParseTable$Args{" +
                "root=" + root +
                ", strategyAffix='" + strategyAffix + '\'' +
                '}';
        }
    }

    private final Sdf3SpecConfigFunctionWrapper specConfigFunctionWrapper;
    private final Sdf3ConfigSupplierWrapper configSupplierWrapper;
    private final Sdf3SpecToParseTable specToParseTable;

    @Inject public Sdf3ShowSpecParseTable(
        Sdf3SpecConfigFunctionWrapper specConfigFunctionWrapper,
        Sdf3ConfigSupplierWrapper configSupplierWrapper,
        Sdf3SpecToParseTable specToParseTable
    ) {
        this.specConfigFunctionWrapper = specConfigFunctionWrapper;
        this.configSupplierWrapper = configSupplierWrapper;
        this.specToParseTable = specToParseTable;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) {
        final String name = "Parse table for project '" + args.root + "'";
        try {
            final Sdf3SpecConfig specConfig = context.require(specConfigFunctionWrapper.get(), args.root).unwrap().unwrap();
            final Sdf3Config config = context.require(configSupplierWrapper.get()).unwrap().unwrap();
            return run(context, specConfig, config, args.strategyAffix, args, name);
        } catch (NoSuchElementException ex) {
            return CommandFeedback.of(ShowFeedback.showText("Cannot show parse table; SDF3 was not configured in '" + args.root + "'", name));
        } catch (Exception ex) {
            // TODO: should we propagate configuration errors here? Every task that requires some configuration will
            //       propagate the same configuration errors, which would lead to duplicates.
            return CommandFeedback.ofTryExtractMessagesFrom(ex, args.root);
        }
    }

    private CommandFeedback run(ExecContext context, Sdf3SpecConfig specConfig, Sdf3Config config, String strategyAffix, Args args, String name) {
        final Result<ParseTable, ?> parseTableResult = context.require(specToParseTable, new Sdf3SpecToParseTable.Input(
            specConfig,
            config,
            strategyAffix,
            false
        ));
        return parseTableResult
            .mapCatching(ParseTableIO::generateATerm)
            .mapOrElse(
                ast -> CommandFeedback.of(ShowFeedback.showText(TermToString.toString(ast), name)),
                e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.root)
            );
    }
}
