package mb.stratego.pie;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Abstract task definition that gets the Stratego runtime provider with the provided implementation of {@link
 * GetStrategoRuntimeProvider}, gets a Stratego runtime, and then executes the provided strategies on the {@link
 * IStrategoTerm input AST} in sequence.
 *
 * This class should be implemented by language developers to create task definitions that run Stratego strategies on
 * {@link IStrategoTerm ASTs} directly by providing the {@link GetStrategoRuntimeProvider} and strategies to execute in
 * the constructor, and by overriding {@link #getId()}.
 *
 * @implNote Taking {@link IStrategoTerm ASTs} directly as input is supported in PIE, but incurs additional space and
 * time overhead, as the input AST must be stored and checked for changes. Consider using {@link
 * ProviderStrategoTransformTaskDef} and extracting the {@link IStrategoTerm AST} from the output of another task, such
 * as a task that returns the a parsed, analyzed, or transformed (desugared, normalized, etc.) AST.
 */
public abstract class AstStrategoTransformTaskDef extends ProviderStrategoTransformTaskDef<IStrategoTerm> {
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
