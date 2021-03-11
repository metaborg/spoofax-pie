package mb.sdf3.task.debug;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.ValueSupplier;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.Sdf3SpecConfigFunctionWrapper;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.spec.Sdf3CreateSpec;
import mb.sdf3.task.spec.Sdf3ParseTableToParenthesizer;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.parsetable.ParseTable;

import javax.inject.Inject;
import java.io.Serializable;

@Sdf3Scope
public class Sdf3ShowSpecParenthesizer extends ProvideOutputShared implements TaskDef<Sdf3ShowSpecParenthesizer.Args, CommandFeedback> {
    public static class Args implements Serializable {
        // TODO: this should take a Sdf3SpecConfig directly, which must be assignable from CLI and such, but this is not possible yet.
        public final ResourcePath root;
        public final boolean concrete;

        public Args(ResourcePath root, boolean concrete) {
            this.root = root;
            this.concrete = concrete;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            if(concrete != args.concrete) return false;
            return root.equals(args.root);
        }

        @Override public int hashCode() {
            int result = root.hashCode();
            result = 31 * result + (concrete ? 1 : 0);
            return result;
        }

        @Override public String toString() {
            return "Args{" +
                "root=" + root +
                ", concrete=" + concrete +
                '}';
        }
    }

    private final Sdf3SpecConfigFunctionWrapper configFunction;
    private final Sdf3CreateSpec createSpec;
    private final Sdf3SpecToParseTable specToParseTable;
    private final Sdf3ParseTableToParenthesizer specToParenthesizer;

    @Inject public Sdf3ShowSpecParenthesizer(
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider,
        Sdf3SpecConfigFunctionWrapper configFunction,
        Sdf3CreateSpec createSpec,
        Sdf3SpecToParseTable specToParseTable,
        Sdf3ParseTableToParenthesizer sdf3ParseTableToParenthesizer
    ) {
        super(getStrategoRuntimeProvider, "pp-stratego-string", "parenthesizer");
        this.configFunction = configFunction;
        this.createSpec = createSpec;
        this.specToParseTable = specToParseTable;
        this.specToParenthesizer = sdf3ParseTableToParenthesizer;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        return context.require(configFunction.get(), args.root).mapOrElse(o ->
            o.mapOrElse(c ->
                run(context, c, args),
                () -> CommandFeedback.of(ShowFeedback.showText("Cannot show parenthesizer; SDF3 was not configured in '" + args.root + "'", getName(args.concrete, args.root)))
            ),
            // TODO: should we propagate configuration errors here? Every task that requires some configuration will
            //       propagate the same configuration errors, which would lead to duplicates.
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.root)
        );
    }

    private CommandFeedback run(ExecContext context, Sdf3SpecConfig config, Args args) {
        final Supplier<? extends Result<ParseTable, ?>> parseTableSupplier = specToParseTable.createSupplier(new Sdf3SpecToParseTable.Input(
            createSpec.createSupplier(new ValueSupplier<>(Result.ofOk(config))),
            false
        ));
        return context.require(specToParenthesizer, new Sdf3ParseTableToParenthesizer.Args(parseTableSupplier, "parenthesizer")).mapOrElse(
            ast -> provideOutput(context, args.concrete, ast, args.root),
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.root)
        );
    }
}
