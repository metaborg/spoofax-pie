package mb.sdf3.task.debug;

import java.io.Serializable;
import java.util.Objects;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3ToNormalForm;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spoofax.Sdf3ParseWrapper;
import mb.sdf3.task.spoofax.Sdf3SpecConfigFunctionWrapper;
import mb.sdf3.task.util.Sdf3StrategoTransformTaskDef;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;

import javax.inject.Inject;

import org.checkerframework.checker.nullness.qual.Nullable;

import static mb.sdf3.task.util.Sdf3StrategoTransformTaskDef.inputSupplier;

@Sdf3Scope
public class Sdf3ShowNormalForm  extends ProvideOutputShared implements TaskDef<Sdf3ShowNormalForm.Args, CommandFeedback> {
    public static class Args implements Serializable {
        // TODO: this should take a Sdf3SpecConfig directly, which must be assignable from CLI and such, but this is not possible yet.
        public final ResourcePath root;
        public final ResourceKey file;
        public final boolean concrete;

        public Args(ResourcePath root, ResourceKey file, boolean concrete) {
            this.root = root;
            this.file = file;
            this.concrete = concrete;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            Args args = (Args)o;
            return concrete == args.concrete && Objects.equals(root, args.root) && Objects.equals(file, args.file);
        }

        @Override public int hashCode() {
            return Objects.hash(root, file, concrete);
        }

        @Override public String toString() {
            return "Args{" + "root=" + root + ", file=" + file + ", concrete=" + concrete + '}';
        }
    }

    private final Sdf3SpecConfigFunctionWrapper configFunctionWrapper;
    private final Sdf3ParseWrapper parse;
    private final Sdf3Desugar desugar;
    private final Sdf3ToNormalForm toNormalForm;

    @Inject public Sdf3ShowNormalForm(
        Sdf3SpecConfigFunctionWrapper configFunctionWrapper,
        Sdf3ParseWrapper parse,
        Sdf3Desugar desugar,
        Sdf3ToNormalForm toNormalForm,
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(getStrategoRuntimeProvider, "pp-SDF3-string", "normal-form");
        this.configFunctionWrapper = configFunctionWrapper;
        this.parse = parse;
        this.desugar = desugar;
        this.toNormalForm = toNormalForm;
    }

    @Override public CommandFeedback exec(ExecContext context, Sdf3ShowNormalForm.Args args) {
        final String name = "Parse table for project '" + args.root + "'";
        return context.require(configFunctionWrapper.get(), args.root).mapOrElse(
            o -> o.mapOrElse(
                c -> run(context, c, args, name),
                () -> CommandFeedback.of(
                    ShowFeedback.showText("Cannot show normal form; SDF3 was not configured in '" + args.root + "'", name))
            ),
            // TODO: should we propagate configuration errors here? Every task that requires some configuration will
            //       propagate the same configuration errors, which would lead to duplicates.
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.root)
        );
    }

    private CommandFeedback run(ExecContext context, Sdf3SpecConfig config, Sdf3ShowNormalForm.Args args, String name) {
        return context.require(toNormalForm.createTask(
                inputSupplier(desugar.createSupplier(parse.inputBuilder().withFile(args.file).buildRecoverableAstSupplier()), config.placeholders)))
            .mapOrElse(ast -> provideOutput(context, args.concrete, ast, args.file), e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.file));
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
