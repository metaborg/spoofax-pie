package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.tego.strategies.runtime.TegoRuntimeBuilder;

/**
 * Factory for a Tego runtime builder.
 */
public interface TegoRuntimeBuilderFactory {
    /**
     * Creates a new Tego runtime builder.
     *
     * @return the new Tego runtime builder
     */
    TegoRuntimeBuilder create();
}
