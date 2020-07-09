package mb.statix.multilang.pie;

import mb.common.option.Option;
import mb.common.result.Result;
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
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.FileResult;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.Level;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@MultiLangScope
public class SmlPartialSolveFile implements TaskDef<SmlPartialSolveFile.Input, FileResult> {

    public static class Input implements Serializable {
        private final LanguageId languageId;
        private final ResourceKey resourceKey;

        private final Supplier<Spec> specSupplier;
        private final Supplier<GlobalResult> globalResultSupplier;

        private final @Nullable Level logLevel;

        public Input(
            LanguageId languageId,
            ResourceKey resourceKey,
            Supplier<Spec> specSupplier,
            Supplier<GlobalResult> globalResultSupplier,
            @Nullable Level logLevel
        ) {
            this.languageId = languageId;
            this.resourceKey = resourceKey;
            this.specSupplier = specSupplier;
            this.globalResultSupplier = globalResultSupplier;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return Objects.equals(languageId, input.languageId) &&
                Objects.equals(resourceKey, input.resourceKey) &&
                Objects.equals(specSupplier, input.specSupplier) &&
                Objects.equals(globalResultSupplier, input.globalResultSupplier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(languageId, resourceKey, specSupplier, globalResultSupplier);
        }
    }

    // This task unfortunately has to be tightly integrated with AnalysisContextService, for the following reason:
    // The statix solver needs to transform strategoTerms to nabl terms, using the StrategoTerms class. This class
    // requires an ITermFactory, which is language specfic, and therefore needs to be injected. It can not be injected
    // By a supplier, because it is not Serializable. Therefore, we need to request it directly from the service.
    private final AnalysisContextService analysisContextService;
    private final Logger logger;

    @Inject public SmlPartialSolveFile(AnalysisContextService analysisContextService, LoggerFactory loggerFactory) {
        this.analysisContextService = analysisContextService;
        logger = loggerFactory.create(SmlPartialSolveFile.class);
    }

    @Override public String getId() {
        return SmlPartialSolveFile.class.getCanonicalName();
    }

    @Override public FileResult exec(ExecContext context, Input input) throws Exception {
        LanguageMetadata languageMetadata = analysisContextService.getLanguageMetadata(input.languageId);
        Supplier<Option<IStrategoTerm>> astSupplier = exec -> languageMetadata.astFunction().apply(exec, input.resourceKey);
        IStrategoTerm ast = context.require(astSupplier).unwrap();

        GlobalResult globalResult = context.require(input.globalResultSupplier);
        Spec spec = context.require(input.specSupplier);

        StrategoTerms st = new StrategoTerms(languageMetadata.termFactory());

        IDebugContext debug = TaskUtils.createDebugContext(SmlPartialSolveFile.class, input.logLevel);
        Iterable<ITerm> constraintArgs = Iterables2.from(globalResult.getGlobalScope(), st.fromStratego(ast));
        IConstraint fileConstraint = new CUser(languageMetadata.fileConstraint(), constraintArgs, null);

        long t0 = System.currentTimeMillis();
        SolverResult fileResult = SolverUtils.partialSolve(spec,
            globalResult.getResult().state().withResource(input.resourceKey.toString()),
            fileConstraint,
            debug,
            new NullProgress(),
            new NullCancel()
        );
        long dt = System.currentTimeMillis() - t0;
        logger.info("{} analyzed in {} ms] ", input.resourceKey, dt);

        IStrategoTerm analyzedAst = languageMetadata.postTransform().apply(context, astSupplier).unwrap();
        return new FileResult(analyzedAst, fileResult);
    }
}
