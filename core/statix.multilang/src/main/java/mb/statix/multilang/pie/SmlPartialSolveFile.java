package mb.statix.multilang.pie;

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
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.Level;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@MultiLangScope
public class SmlPartialSolveFile implements TaskDef<SmlPartialSolveFile.Input, FileResult> {

    public static class Input implements Serializable {
        private final Function<ResourceKey, IStrategoTerm> astSupplier;
        private final Function<Supplier<IStrategoTerm>, IStrategoTerm> postAnalysisTransform;
        private final ResourceKey resourceKey;

        private final Supplier<Spec> specSupplier;
        private final Supplier<GlobalResult> globalResultSupplier;
        private final String fileConstraint;

        private final @Nullable Level logLevel;

        public Input(
            Function<ResourceKey, IStrategoTerm> astSupplier,
            Function<Supplier<IStrategoTerm>, IStrategoTerm> postAnalysisTransform,
            ResourceKey resourceKey,
            Supplier<Spec> specSupplier,
            Supplier<GlobalResult> globalResultSupplier,
            String fileConstraint,
            @Nullable Level logLevel
        ) {
            this.astSupplier = astSupplier;
            this.postAnalysisTransform = postAnalysisTransform;
            this.resourceKey = resourceKey;
            this.specSupplier = specSupplier;
            this.globalResultSupplier = globalResultSupplier;
            this.fileConstraint = fileConstraint;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return Objects.equals(astSupplier, input.astSupplier) &&
                Objects.equals(postAnalysisTransform, input.postAnalysisTransform) &&
                Objects.equals(resourceKey, input.resourceKey) &&
                Objects.equals(specSupplier, input.specSupplier) &&
                Objects.equals(globalResultSupplier, input.globalResultSupplier) &&
                Objects.equals(fileConstraint, input.fileConstraint);
        }

        @Override
        public int hashCode() {
            return Objects.hash(astSupplier, postAnalysisTransform, resourceKey, specSupplier, globalResultSupplier,
                fileConstraint);
        }
    }

    private final StrategoTerms st;
    private final Logger logger;

    @Inject public SmlPartialSolveFile(ITermFactory termFactory, LoggerFactory loggerFactory) {
        st = new StrategoTerms(termFactory);
        logger = loggerFactory.create(SmlPartialSolveFile.class);
    }

    @Override public String getId() {
        return SmlPartialSolveFile.class.getCanonicalName();
    }

    @Override public FileResult exec(ExecContext context, Input input) throws Exception {
        Supplier<IStrategoTerm> astSupplier = exec -> input.astSupplier.apply(exec, input.resourceKey);
        IStrategoTerm ast = context.require(astSupplier);

        GlobalResult globalResult = context.require(input.globalResultSupplier);
        Spec spec = context.require(input.specSupplier);

        IDebugContext debug = TaskUtils.createDebugContext(SmlPartialSolveFile.class, input.logLevel);
        Iterable<ITerm> constraintArgs = Iterables2.from(globalResult.getGlobalScope(), st.fromStratego(ast));
        IConstraint fileConstraint = new CUser(input.fileConstraint, constraintArgs, null);

        long t0 = System.currentTimeMillis();
        SolverResult fileResult = SolverUtils.partialSolve(spec,
            globalResult.getResult().state().withResource(input.resourceKey.toString()),
            fileConstraint,
            debug);
        long dt = System.currentTimeMillis() - t0;
        logger.info("{} analyzed in {} ms] ", input.resourceKey, dt);

        IStrategoTerm analyzedAst = input.postAnalysisTransform.apply(context, astSupplier);
        return new FileResult(analyzedAst, fileResult);
    }
}
