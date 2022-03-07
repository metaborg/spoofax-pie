package mb.dynamix.task;

import mb.aterm.common.InvalidAstShapeException;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.dynamix.DynamixClassLoaderResources;
import mb.dynamix.DynamixScope;
import mb.dynamix.task.spoofax.DynamixConfigFunctionWrapper;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@DynamixScope
public class DynamixCompileModule implements TaskDef<DynamixCompileModule.Input, Result<Option<DynamixCompileModule.Output>, ?>> {
    public static class Input implements Serializable {
        public final ResourcePath rootDirectory;
        public final ResourcePath file;

        public Input(ResourcePath rootDirectory, ResourcePath file) {
            this.rootDirectory = rootDirectory;
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!rootDirectory.equals(input.rootDirectory)) return false;
            return file.equals(input.file);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + file.hashCode();
            return result;
        }

        @Override public String toString() {
            return "DynamixCompileModule.Input{" +
                "rootDirectory=" + rootDirectory +
                ", file=" + file +
                '}';
        }
    }

    public static class Output implements Serializable {
        public final String relativeOutputPath;
        public final IStrategoTerm spec;

        public Output(String relativeOutputPath, IStrategoTerm spec) {
            this.relativeOutputPath = relativeOutputPath;
            this.spec = spec;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Output output = (Output)o;
            return relativeOutputPath.equals(output.relativeOutputPath) && spec.equals(output.spec);
        }

        @Override public int hashCode() {
            return Objects.hash(relativeOutputPath, spec);
        }

        @Override public String toString() {
            return "Output{" +
                "relativeOutputPath='" + relativeOutputPath + '\'' +
                ", spec=" + spec +
                '}';
        }
    }

    private final DynamixClassLoaderResources classLoaderResources;
    private final DynamixConfigFunctionWrapper configFunctionWrapper;
    private final DynamixAnalyzeFile analyzeFile;
    private final DynamixGetStrategoRuntimeProvider getStrategoRuntimeProvider;

    @Inject public DynamixCompileModule(
        DynamixClassLoaderResources classLoaderResources,
        DynamixConfigFunctionWrapper configFunctionWrapper,
        DynamixAnalyzeFile analyzeFile,
        DynamixGetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        this.classLoaderResources = classLoaderResources;
        this.configFunctionWrapper = configFunctionWrapper;
        this.analyzeFile = analyzeFile;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<Output>, ?> exec(ExecContext context, DynamixCompileModule.Input input) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(Input.class), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(Output.class), ResourceStampers.hashFile());

        final ResourcePath rootDirectory = input.rootDirectory;
        final ResourcePath file = input.file;
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();

        context.require(configFunctionWrapper.get(), rootDirectory).ifOk(o -> o.ifSome(config -> {
            // TODO: only require the origin that is needed to compile this module?
            config.sourceFileOrigins.forEach(origin -> context.require(origin, OutputStampers.inconsequential()));
        }));

        return context.require(analyzeFile, new DynamixAnalyzeFile.Input(rootDirectory, file)).flatMapOrElse(output -> {
            try {
                final IStrategoTerm term = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), output.ast, file, rootDirectory);
                final IStrategoTerm outputTerm = strategoRuntime.addContextObject(output.context).invoke("dx--generate-aterm", term);

                // we expect an output of (<output path>, term)
                final String relativeOutputPath = TermUtils.asJavaStringAt(outputTerm, 0)
                    .orElseThrow(() -> new InvalidAstShapeException("string as first subterm", outputTerm));
                if(outputTerm.getSubtermCount() < 2) {
                    throw new InvalidAstShapeException("term with two subterms", outputTerm);
                }
                final IStrategoTerm spec = outputTerm.getSubterm(1);
                return Result.ofOk(Option.ofSome(new Output(relativeOutputPath, spec)));
            } catch(StrategoException e) {
                return Result.ofErr(e); // TODO: better error/exception?
            }
        }, Result::ofErr); // TODO: better error/exception?
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
