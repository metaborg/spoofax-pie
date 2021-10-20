package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.common.codecompletion.CodeCompletionResult;
import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Code completion interface.
 */
public interface CodeCompleter {
    /**
     * Produces completion results for the specified location in the specified text.
     *
     * @param ast the source code AST
     * @param primarySelection the primary selection
     * @param resource the resource key
     * @return a completion result; or {@code null}
     * @throws InterruptedException The operation was interrupted.
     */
    @Nullable CodeCompletionResult complete(IStrategoTerm ast, Region primarySelection, ResourceKey resource) throws InterruptedException;
}
