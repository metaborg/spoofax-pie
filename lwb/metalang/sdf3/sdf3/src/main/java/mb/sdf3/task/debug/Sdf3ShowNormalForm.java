package mb.sdf3.task.debug;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3AstStrategoTransformTaskDef;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToNormalForm;
import mb.sdf3.task.spec.Sdf3Config;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spoofax.Sdf3ConfigSupplierWrapper;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@Sdf3Scope
public final class Sdf3ShowNormalForm extends ProvideOutputShared implements TaskDef<Sdf3ShowNormalForm.Input, CommandFeedback> {
    public static class Input implements Serializable {
        // TODO: this should take a Sdf3SpecConfig and Sdf3Config directly,
        //  which must be assignable from CLI and such, but this is not possible yet.
        public final ResourcePath root;
        public final ResourceKey file;
        public final boolean concrete;

        public Input(ResourcePath root, ResourceKey file, boolean concrete) {
            this.root = root;
            this.file = file;
            this.concrete = concrete;
        }
        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Sdf3ShowNormalForm.Input that = (Sdf3ShowNormalForm.Input)o;
            return this.root.equals(that.root)
                && this.file.equals(that.file)
                && this.concrete == that.concrete;
        }

        @Override public int hashCode() {
            return Objects.hash(
                this.root,
                this.file,
                this.concrete
            );
        }

        @Override public String toString() {
            return "Sdf3ShowNormalForm$Input{" +
                "root=" + root +
                ", file=" + file +
                ", concrete=" + concrete +
                '}';
        }
    }


    private final Sdf3Parse parse;
    private final Sdf3Desugar desugar;
    private final Sdf3ToNormalForm operation;
    private final Sdf3ConfigSupplierWrapper configSupplierWrapper;

    @Inject public Sdf3ShowNormalForm(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToNormalForm operation,
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider,
        Sdf3ConfigSupplierWrapper configSupplierWrapper
    ) {
        super(getStrategoRuntimeProvider, "pp-SDF3-string", "normal-form");
        this.parse = parse;
        this.desugar = desugar;
        this.operation = operation;
        this.configSupplierWrapper = configSupplierWrapper;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Sdf3ShowNormalForm.Input args) {
        return context.require(configSupplierWrapper.get()).mapOrElse(
            configOpt -> configOpt.mapOrElse(
                config -> run(context, config, args),
                () -> CommandFeedback.of(ShowFeedback.showText("Cannot show normal form; SDF3 was not configured in '" + args.root + "'", "Normal form"))
            ),
            // TODO: should we propagate configuration errors here? Every task that requires some configuration will
            //       propagate the same configuration errors, which would lead to duplicates.
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.root)
        );
    }

    private CommandFeedback run(ExecContext context, Sdf3Config config, Sdf3ShowNormalForm.Input args) {
        return context.require(operation.createTask(
            new Sdf3AstStrategoTransformTaskDef.Input(
                desugar.createSupplier(parse.inputBuilder().withFile(args.file).buildRecoverableAstSupplier()),
                "", // TODO
                config
            )
        )).mapOrElse(
            ast -> provideOutput(context, args.concrete, ast, args.file),
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.file)
        );
    }
}
