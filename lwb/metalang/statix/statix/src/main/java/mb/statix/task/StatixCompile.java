package mb.statix.task;

import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.OutputStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixScope;
import mb.statix.task.spoofax.StatixAnalyzeMultiWrapper;
import mb.statix.task.spoofax.StatixParseWrapper;
import mb.statix.util.StatixUtil;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@StatixScope
public class StatixCompile implements TaskDef<StatixCompile.Input, Result<StatixCompile.Output, ?>> {
    public static class Input implements Serializable {
        public final ResourcePath file;
        public final StatixConfig config;

        public Input(ResourcePath file, StatixConfig config) {
            this.file = file;
            this.config = config;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!file.equals(input.file)) return false;
            return config.equals(input.config);
        }

        @Override public int hashCode() {
            int result = file.hashCode();
            result = 31 * result + config.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Input{" +
                "file=" + file +
                ", config=" + config +
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

    private final StatixParseWrapper parse;
    private final StatixAnalyzeMultiWrapper analyze;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public StatixCompile(
        StatixParseWrapper parse,
        StatixAnalyzeMultiWrapper analyze,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<Output, ?> exec(ExecContext context, StatixCompile.Input input) throws Exception {
        // Require source file origin tasks.
        input.config.sourceFileOrigins.forEach(origin -> context.require(origin, OutputStampers.inconsequential()));

        // TODO: this does not analyze all source and include directories
        return context.require(analyze.createSingleFileOutputSupplier(
            new ConstraintAnalyzeMultiTaskDef.Input(input.config.rootDirectory, parse.createMultiAstSupplierFunction(StatixUtil.createResourceWalker(), StatixUtil.createResourceMatcher())),
            input.file
        )).flatMapOrElse(mainFileOutput -> {
            if(mainFileOutput.result.messages.containsError()) {
                return Result.ofErr(new Exception("Cannot compile Statix specification; analysis resulted in errors")); // TODO: better error/exception
            }
            try {
                final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(mainFileOutput.context);
                final IStrategoTerm term = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), mainFileOutput.result.ast, input.file, input.config.rootDirectory);
                final IStrategoTerm outputTerm = strategoRuntime.invoke("generate-aterm", term);
                return Result.ofOk(new Output(TermUtils.toJavaStringAt(outputTerm, 0), outputTerm.getSubterm(1)));
            } catch(StrategoException e) {
                return Result.ofErr(e); // TODO: better error/exception
            }
        }, Result::ofErr); // TODO: better error/exception
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
