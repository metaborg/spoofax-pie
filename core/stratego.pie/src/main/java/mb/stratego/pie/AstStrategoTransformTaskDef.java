package mb.stratego.pie;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Abstract task definition that gets the Stratego runtime provider with the provided implementation of {@link
 * GetStrategoRuntimeProvider}, gets a Stratego runtime, and then executes the provided strategies on the supplied
 * {@link IStrategoTerm input AST} in sequence.
 *
 * Inputs of this task are of type {@code Supplier<? extends Result<IStrategoTerm, ?>>} such that {@link IStrategoTerm
 * ASTs} can be {@link Supplier incrementally supplied}, and support {@link Result failure}.
 *
 * Outputs are of type {@code Result<IStrategoTerm, ?>}.
 *
 * This class should be implemented by language developers to create task definitions that run Stratego strategies on
 * {@link IStrategoTerm ASTs} directly by providing the {@link GetStrategoRuntimeProvider} task definition and
 * strategies to execute in the constructor, and by overriding {@link #getId()} to give the task definition a unique
 * ID.
 */
public abstract class AstStrategoTransformTaskDef extends StrategoTransformTaskDef<IStrategoTerm> {
    public AstStrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, ListView<String> strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    public AstStrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, String... strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    @Override protected IStrategoTerm getAst(ExecContext context, IStrategoTerm input) {
        return input;
    }
}
