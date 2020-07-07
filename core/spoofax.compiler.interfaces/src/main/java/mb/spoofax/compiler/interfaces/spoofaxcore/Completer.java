package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.common.region.Region;
import mb.completions.common.CompletionResult;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Code completion interface.
 */
public interface Completer {
    /**
     * Produces completion results for the specified location in the specified text.
     *
     * @param ast the source code AST
     * @param primarySelection the primary selection
     * @param resource the resource key; or {@code null}
     * @return a completion result
     * @throws InterruptedException The operation was interrupted.
     */
    CompletionResult complete(IStrategoTerm ast, IStrategoTerm analysisResult, Region primarySelection, @Nullable ResourceKey resource) throws InterruptedException;
}
