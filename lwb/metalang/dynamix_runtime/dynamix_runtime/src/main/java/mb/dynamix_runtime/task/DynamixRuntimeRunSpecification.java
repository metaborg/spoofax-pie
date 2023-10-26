package mb.dynamix_runtime.task;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.dynamix_runtime.DynamixRuntimeConfig;
import mb.dynamix_runtime.DynamixRuntimeScope;
import mb.dynamix_runtime.DynamixSpecificationRunningException;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Set;

@DynamixRuntimeScope
public class DynamixRuntimeRunSpecification implements TaskDef<DynamixRuntimeRunSpecification.Input, Result<IStrategoTerm, DynamixSpecificationRunningException>> {
    public static class Input implements Serializable {
        public final Supplier<Result<IStrategoTerm, ?>> specSupplier;
        public final Supplier<Result<IStrategoTerm, ?>> astSupplier;
        public final Option<Supplier<Result<ConstraintAnalyzerContext, ?>>> constraintAnalyzerContext;
        public final DynamixRuntimeConfig dynamixConfig;

        public Input(Supplier<Result<IStrategoTerm, ?>> specSupplier, Supplier<Result<IStrategoTerm, ?>> astSupplier, Option<Supplier<Result<ConstraintAnalyzerContext, ?>>> constraintAnalyzerContext, DynamixRuntimeConfig dynamixConfig) {
            this.specSupplier = specSupplier;
            this.astSupplier = astSupplier;
            this.constraintAnalyzerContext = constraintAnalyzerContext;
            this.dynamixConfig = dynamixConfig;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            Input input = (Input)o;

            if(!specSupplier.equals(input.specSupplier)) return false;
            if(!astSupplier.equals(input.astSupplier)) return false;
            if(!constraintAnalyzerContext.equals(input.constraintAnalyzerContext)) return false;
            return dynamixConfig.equals(input.dynamixConfig);
        }

        @Override public int hashCode() {
            int result = specSupplier.hashCode();
            result = 31 * result + astSupplier.hashCode();
            result = 31 * result + constraintAnalyzerContext.hashCode();
            result = 31 * result + dynamixConfig.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Input{" +
                "specSupplier=" + specSupplier +
                ", astSupplier=" + astSupplier +
                ", constraintAnalyzerContext=" + constraintAnalyzerContext +
                ", dynamixConfig=" + dynamixConfig +
                '}';
        }
    }

    // TODO: should use the task instead of the provider directly, but currently
    // this seems to cause issues in PIE where it cannot resolve the resource
    // registry for the stratego ctree and other assets on subsequent runs
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public DynamixRuntimeRunSpecification(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<IStrategoTerm, DynamixSpecificationRunningException> exec(ExecContext context, Input input) throws Exception {
        StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();

        // read compiled spec as aterm
        Result<IStrategoTerm, ?> specTerm = context.require(input.specSupplier);
        if(specTerm.isErr()) {
            return Result.ofErr(DynamixSpecificationRunningException.getCompiledSpecificationFail(specTerm.getErr()));
        }

        // if given, include constraint analyzer context
        if(input.constraintAnalyzerContext.isSome()) {
            Result<ConstraintAnalyzerContext, ?> constraintContext = context.require(input.constraintAnalyzerContext.get());
            if(constraintContext.isErr()) {
                return Result.ofErr(DynamixSpecificationRunningException.analyzeFileFail(constraintContext.getErr()));
            }

            strategoRuntime = strategoRuntime.addContextObject(constraintContext.get());
        }

        // execute dynamix
        try {
            Result<IStrategoTerm, ?> sourceTerm = context.require(input.astSupplier);
            if(sourceTerm.isErr()) {
                return Result.ofErr(DynamixSpecificationRunningException.analyzeFileFail(sourceTerm.getErr()));
            }

            IStrategoTerm ruleTerm = strategoRuntime.getTermFactory().makeString(input.dynamixConfig.mainRuleName);
            IStrategoTerm result = strategoRuntime.invoke("dx-run-spec", sourceTerm.get(), ListView.of(specTerm.get(), ruleTerm));

            return Result.ofOk(result);
        } catch(StrategoException ex) {
            return Result.ofErr(DynamixSpecificationRunningException.dynamixExecutionFail(ex));
        }
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
