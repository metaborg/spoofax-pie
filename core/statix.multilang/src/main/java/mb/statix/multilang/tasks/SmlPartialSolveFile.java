package mb.statix.multilang.tasks;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.statix.constraints.CUser;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;

import java.io.Serializable;

public class SmlPartialSolveFile implements TaskDef<SmlPartialSolveFile.Input, SmlPartialSolveFile.Output> {

    public static class Input implements Serializable {
        private final ITerm globalScope;
        private final SolverResult globalResult;
        private final IDebugContext debug;

        private final Spec spec;
        private final String fileConstraint;
        private final Supplier<IStrategoTerm> astSupplier;
        private final ResourceKey resourceKey;

        public Input(ITerm globalScope, SolverResult globalResult, IDebugContext debug, Spec spec,
                     String fileConstraint, Supplier<IStrategoTerm> astSupplier, ResourceKey resourceKey) {
            this.globalScope = globalScope;
            this.globalResult = globalResult;
            this.debug = debug;
            this.spec = spec;
            this.fileConstraint = fileConstraint;
            this.astSupplier = astSupplier;
            this.resourceKey = resourceKey;
        }
    }

    public class Output implements Serializable {
        private final SolverResult fileResult;

        public Output(SolverResult fileResult) {
            this.fileResult = fileResult;
        }

        public SolverResult getFileResult() {
            return fileResult;
        }
    }

    private final TermFactory tf = new TermFactory();
    private final StrategoTerms st = new StrategoTerms(tf);

    @Override public String getId() {
        return SmlPartialSolveFile.class.getSimpleName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        IStrategoTerm ast = input.astSupplier.get(context);
        Iterable<ITerm> constraintArgs = Iterables2.from(input.globalScope, st.fromStratego(ast));
        IConstraint fileConstraint = new CUser(input.fileConstraint, constraintArgs, null);

        SolverResult fileResult = SolverUtils.partialSolve(input.spec,
            input.globalResult.state().withResource(input.resourceKey.toString()),
            fileConstraint,
            input.debug);

        return new Output(fileResult);
    }
}
