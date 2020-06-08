package mb.statix.multilang.tasks;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
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

public class SmlPartialSolveFile implements TaskDef<SmlPartialSolveFile.Input, SmlPartialSolveFile.Output> {

    public static class Input implements Serializable {
        private final ITerm globalScope;
        private final SolverResult globalResult;
        private final IDebugContext debug;

        private final Spec spec;
        private final String fileConstraint;
        private final Function<ResourceKey, IStrategoTerm> astSupplier;
        private final Function<IStrategoTerm, IStrategoTerm> postAnalysisTransform;
        private final ResourceKey resourceKey;

        public Input(ITerm globalScope, SolverResult globalResult, IDebugContext debug, Spec spec,
                     String fileConstraint, Function<ResourceKey, IStrategoTerm> astSupplier,
                     Function<IStrategoTerm, IStrategoTerm> postAnalysisTransform, ResourceKey resourceKey) {
            this.globalScope = globalScope;
            this.globalResult = globalResult;
            this.debug = debug;
            this.spec = spec;
            this.fileConstraint = fileConstraint;
            this.astSupplier = astSupplier;
            this.postAnalysisTransform = postAnalysisTransform;
            this.resourceKey = resourceKey;
        }
    }

    public class Output implements Serializable {
        private final FileResult fileResult;

        public Output(FileResult fileResult) {
            this.fileResult = fileResult;
        }

        public FileResult getFileResult() {
            return fileResult;
        }
    }

    private final StrategoTerms st;

    @Inject public SmlPartialSolveFile(ITermFactory termFactory) {
        st = new StrategoTerms(termFactory);
    }

    @Override public String getId() {
        return SmlPartialSolveFile.class.getSimpleName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        IStrategoTerm ast = input.astSupplier.apply(context, input.resourceKey);
        Iterable<ITerm> constraintArgs = Iterables2.from(input.globalScope, st.fromStratego(ast));
        IConstraint fileConstraint = new CUser(input.fileConstraint, constraintArgs, null);

        SolverResult fileResult = SolverUtils.partialSolve(input.spec,
            input.globalResult.state().withResource(input.resourceKey.toString()),
            fileConstraint,
            input.debug);

        IStrategoTerm analyzedAst = input.postAnalysisTransform.apply(context, ast);

        return new Output(new FileResult(analyzedAst, fileResult));
    }
}
