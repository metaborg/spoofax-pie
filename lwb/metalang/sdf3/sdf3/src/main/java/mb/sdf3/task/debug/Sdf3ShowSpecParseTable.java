package mb.sdf3.task.debug;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.ValueSupplier;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.Sdf3SpecConfigFunctionWrapper;
import mb.sdf3.task.spec.Sdf3CreateSpec;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.metaborg.sdf2table.parsetable.ParseTable;

import javax.inject.Inject;
import java.io.Serializable;

@Sdf3Scope
public class Sdf3ShowSpecParseTable implements TaskDef<Sdf3ShowSpecParseTable.Args, CommandFeedback> {
    public static class Args implements Serializable {
        // TODO: this should take a Sdf3SpecConfig directly, which must be assignable from CLI and such, but this is not possible yet.
        public final ResourcePath root;

        public Args(ResourcePath root) {
            this.root = root;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return root.equals(args.root);
        }

        @Override public int hashCode() {
            return root.hashCode();
        }

        @Override public String toString() {
            return "Args{" +
                "root=" + root +
                '}';
        }
    }

    private final Sdf3SpecConfigFunctionWrapper configFunction;
    private final Sdf3CreateSpec createSpec;
    private final Sdf3SpecToParseTable specToParseTable;

    @Inject public Sdf3ShowSpecParseTable(
        Sdf3SpecConfigFunctionWrapper configFunction,
        Sdf3CreateSpec createSpec,
        Sdf3SpecToParseTable specToParseTable
    ) {
        this.configFunction = configFunction;
        this.createSpec = createSpec;
        this.specToParseTable = specToParseTable;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) {
        final String name = "Parse table for project '" + args.root + "'";
        return context.require(configFunction.get(), args.root).mapOrElse(o ->
                o.mapOrElse(c ->
                        run(context, c, args, name),
                    () -> CommandFeedback.of(ShowFeedback.showText("Cannot show parse table; SDF3 was not configured in '" + args.root + "'", name))
                ),
            // TODO: should we propagate configuration errors here? Every task that requires some configuration will
            //       propagate the same configuration errors, which would lead to duplicates.
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.root)
        );
    }

    private CommandFeedback run(ExecContext context, Sdf3SpecConfig config, Args args, String name) {
        final Result<ParseTable, ?> parseTableResult = context.require(specToParseTable, new Sdf3SpecToParseTable.Input(
            createSpec.createSupplier(new ValueSupplier<>(Result.ofOk(config))),
            false
        ));
        return parseTableResult
            .mapCatching(ParseTableIO::generateATerm)
            .mapOrElse(
                ast -> CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(ast), name)),
                e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.root)
            );
    }
}
