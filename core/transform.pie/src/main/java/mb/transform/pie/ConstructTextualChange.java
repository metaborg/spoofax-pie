package mb.transform.pie;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.stratego.common.Strategy;
import mb.stratego.pie.AstStrategoTransformTaskDef;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import mb.stratego.pie.StrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

public abstract class ConstructTextualChange extends AstStrategoTransformTaskDef {

    public static class Input implements Serializable {

        private final IStrategoTerm originalAst;
        private final IStrategoTerm transformedAst;

        public Input(IStrategoTerm originalAst, IStrategoTerm transformedAst) {
            this.originalAst = originalAst;
            this.transformedAst = transformedAst;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return Objects.equals(originalAst, input.originalAst) && Objects.equals(transformedAst, input.transformedAst);
        }

        @Override
        public int hashCode() {
            return Objects.hash(originalAst, transformedAst);
        }

        @Override public String toString() {
            return "Input{" +
                "originalAst=" + originalAst +
                ", transformedAst=" + transformedAst +
                '}';
        }
    }

    private final Function<IStrategoTerm, Result<IStrategoTerm, ?>> transform;

    public ConstructTextualChange(
        GetStrategoRuntimeProvider getStrategoRuntimeProvider,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> transform,
        String strategyName,
        Strategy partialPP,
        Strategy parenthesize,
        Strategy overrideReconstruction,
        Strategy resugar
    ) {
        super(getStrategoRuntimeProvider, Strategy.strategy(strategyName, ListView.of(partialPP, parenthesize, overrideReconstruction, resugar), ListView.of()));
        this.transform = transform;
    }


    @Override
    protected Result<IStrategoTerm, ?> getAst(ExecContext context, IStrategoTerm originalAst) {
        return context.require(transform, originalAst).flatMapOrElse(
            transformedAst -> Result.ofOk(getStrategoRuntime(context, originalAst).getTermFactory().makeTuple(originalAst, transformedAst)),
            Result::ofErr
        );
    }
}
