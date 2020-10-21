package mb.statix.task;

import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixScope;
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

@StatixScope
public class StatixCompile implements TaskDef<StatixCompile.Input, Result<StatixCompile.Output, ?>> {
    public static class Input implements Serializable {
        public final ResourcePath root;
        public final ResourcePath resource;

        public Input(ResourcePath root, ResourcePath resource) {
            this.root = root;
            this.resource = resource;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return root.equals(input.root) && resource.equals(input.resource);
        }

        @Override public int hashCode() {
            return Objects.hash(root, resource);
        }

        @Override public String toString() {
            return "Input{" +
                "root=" + root +
                ", resource=" + resource +
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

    private final StatixParse parse;
    private final StatixAnalyzeMulti analyze;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public StatixCompile(
        StatixParse parse,
        StatixAnalyzeMulti analyze,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<Output, ?> exec(ExecContext context, Input input) throws Exception {
        final Supplier<Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> supplier = analyze.createSingleFileOutputSupplier(
            new ConstraintAnalyzeMultiTaskDef.Input(input.root, StatixUtil.createResourceWalker(), StatixUtil.createResourceMatcher(), parse.createAstFunction()),
            input.resource
        );
        return context.require(supplier).flatMapOrElse((output) -> {
            if(output.result.messages.containsError()) {
                return Result.ofErr(new Exception("Cannot compile Statix specification; analysis resulted in errors")); // TODO: better error/exception
            }
            try {
                final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(output.context);
                final IStrategoTerm term = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), output.result.ast, input.resource, input.root);
                final IStrategoTerm outputTerm = strategoRuntime.invoke("generate-aterm", term);
                return Result.ofOk(new Output(TermUtils.toJavaStringAt(outputTerm, 0), outputTerm.getSubterm(1)));
            } catch(StrategoException e) {
                return Result.ofErr(e);
            }
        }, Result::ofErr);
    }
}
