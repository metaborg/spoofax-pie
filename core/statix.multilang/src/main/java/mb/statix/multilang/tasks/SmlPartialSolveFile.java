package mb.statix.multilang.tasks;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.statix.constraints.CUser;
import mb.statix.multilang.FileResult;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

public class SmlPartialSolveFile implements TaskDef<SmlPartialSolveFile.Input, SmlPartialSolveFile.Output> {

    public static class Input implements Serializable {
        private final ITerm globalScope;
        private final SolverResult globalResult;
        private final IDebugContext debug;

        private final Spec spec;
        private final String fileConstraint;
        private final Function<ResourceKey, IStrategoTerm> astSupplier;
        private final Function<Supplier<IStrategoTerm>, IStrategoTerm> postAnalysisTransform;
        private final ResourceKey resourceKey;

        public Input(ITerm globalScope, SolverResult globalResult, IDebugContext debug, Spec spec,
                     String fileConstraint, Function<ResourceKey, IStrategoTerm> astSupplier,
                     Function<Supplier<IStrategoTerm>, IStrategoTerm> postAnalysisTransform, ResourceKey resourceKey) {
            this.globalScope = globalScope;
            this.globalResult = globalResult;
            this.debug = debug;
            this.spec = spec;
            this.fileConstraint = fileConstraint;
            this.astSupplier = astSupplier;
            this.postAnalysisTransform = postAnalysisTransform;
            this.resourceKey = resourceKey;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return globalScope.equals(input.globalScope) &&
                globalResult.equals(input.globalResult) &&
                spec.equals(input.spec) &&
                fileConstraint.equals(input.fileConstraint) &&
                astSupplier.equals(input.astSupplier) &&
                postAnalysisTransform.equals(input.postAnalysisTransform) &&
                resourceKey.equals(input.resourceKey);
        }

        @Override public int hashCode() {
            return Objects.hash(globalScope, globalResult, spec, fileConstraint, astSupplier,
                postAnalysisTransform, resourceKey);
        }

        @Override public String toString() {
            return "Input{" +
                "globalScope=" + globalScope +
                ", globalResult=" + globalResult +
                ", spec=" + spec +
                ", fileConstraint='" + fileConstraint + '\'' +
                ", astSupplier=" + astSupplier +
                ", postAnalysisTransform=" + postAnalysisTransform +
                ", resourceKey=" + resourceKey +
                '}';
        }
    }

    public static class Output implements Serializable {
        private final FileResult fileResult;

        public Output(FileResult fileResult) {
            this.fileResult = fileResult;
        }

        public FileResult getFileResult() {
            return fileResult;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Output output = (Output)o;
            return fileResult.equals(output.fileResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileResult);
        }

        @Override public String toString() {
            return "Output{" +
                "fileResult=" + fileResult +
                '}';
        }
    }

    private final StrategoTerms st;
    private final Logger logger;

    @Inject public SmlPartialSolveFile(ITermFactory termFactory, LoggerFactory loggerFactory) {
        st = new StrategoTerms(termFactory);
        logger = loggerFactory.create(SmlPartialSolveFile.class);
    }

    @Override public String getId() {
        return SmlPartialSolveFile.class.getSimpleName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        Supplier<IStrategoTerm> astSupplier = exec -> input.astSupplier.apply(exec, input.resourceKey);
        IStrategoTerm ast = context.require(astSupplier);
        Iterable<ITerm> constraintArgs = Iterables2.from(input.globalScope, st.fromStratego(ast));
        IConstraint fileConstraint = new CUser(input.fileConstraint, constraintArgs, null);

        long t0 = System.currentTimeMillis();
        SolverResult fileResult = SolverUtils.partialSolve(input.spec,
            input.globalResult.state().withResource(input.resourceKey.toString()),
            fileConstraint,
            input.debug);
        long dt = System.currentTimeMillis() - t0;
        logger.info("{} analyzed in {} ms] ", input.resourceKey, dt);

        IStrategoTerm analyzedAst = input.postAnalysisTransform.apply(context, astSupplier);

        return new Output(new FileResult(analyzedAst, fileResult));
    }
}
