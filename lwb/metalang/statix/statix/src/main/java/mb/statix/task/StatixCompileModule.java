package mb.statix.task;

import mb.aterm.common.InvalidAstShapeException;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
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

@StatixScope
public class StatixCompileModule implements TaskDef<StatixCompileModule.Input, Result<Option<StatixCompileModule.Output>, ?>> {
    public static class Input implements Serializable {
        public final ResourcePath rootDirectory;
        public final ResourcePath file;
        public final ListView<STask<?>> sourceFileOrigins;

        public Input(ResourcePath rootDirectory, ResourcePath file, ListView<STask<?>> sourceFileOrigins) {
            this.rootDirectory = rootDirectory;
            this.file = file;
            this.sourceFileOrigins = sourceFileOrigins;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!rootDirectory.equals(input.rootDirectory)) return false;
            if(!file.equals(input.file)) return false;
            return sourceFileOrigins.equals(input.sourceFileOrigins);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + file.hashCode();
            result = 31 * result + sourceFileOrigins.hashCode();
            return result;
        }

        @Override public String toString() {
            return "StatixCompileModule.Input{" +
                "rootDirectory=" + rootDirectory +
                ", file=" + file +
                ", sourceFileOrigins=" + sourceFileOrigins +
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

    private final StatixClassLoaderResources classLoaderResources;
    private final StatixAnalyzeFile analyzeFile;
    private final StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider;

    @Inject public StatixCompileModule(
        StatixClassLoaderResources classLoaderResources,
        StatixAnalyzeFile analyzeFile,
        StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        this.classLoaderResources = classLoaderResources;
        this.analyzeFile = analyzeFile;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<Output>, ?> exec(ExecContext context, StatixCompileModule.Input input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsLocalResource(StatixCompileProject.Input.class), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsLocalResource(StatixCompileProject.Output.class), ResourceStampers.hashFile());

        // TODO: only require the origin that is needed to compile this module?
        input.sourceFileOrigins.forEach(origin -> context.require(origin, OutputStampers.inconsequential()));

        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();
        final ResourcePath rootDirectory = input.rootDirectory;
        final ResourcePath file = input.file;

        return context.require(analyzeFile, new StatixAnalyzeFile.Input(rootDirectory, file)).flatMapOrElse(output -> {
            try {
                final IStrategoTerm term = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), output.ast, file, rootDirectory);
                final IStrategoTerm outputTerm = strategoRuntime.addContextObject(output.context).invoke("generate-aterm", term);
                if(TermUtils.isAppl(outputTerm, "None", 0)) {
                    // Compiling a .stxtest file will result in none.
                    return Result.ofOk(Option.ofNone());
                }
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
