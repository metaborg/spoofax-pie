package mb.spoofax.lwb.dynamicloading;

import mb.common.result.Result;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.CompileLanguageToJavaClassPath;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A session for dynamically (re)loading specific languages with {@link #reload}, as well as reloading all languages
 * affected by changes to source file changes with {@link #updateAffectedBy}. Mimics the design of {@link MixedSession
 * PIE's MixedSession}.
 */
public class DynamicLoaderMixedSession extends DynamicLoaderSession implements AutoCloseable {
    private final MixedSession session;

    public DynamicLoaderMixedSession(MixedSession session, DynamicLoader dynamicLoader, DynamicLoad dynamicLoad) {
        super(session, dynamicLoader, dynamicLoad);
        this.session = session;
    }

    @Override public void close() throws Exception {
        session.close();
    }


    /**
     * Incrementally (re)compiles, and (re)loads the compiled language with given {@code id} and {@code compilerInput}.
     * After calling this method, {@link #updateAffectedBy} may not be called any more this session.
     *
     * @return Dynamically (re)loaded language.
     */
    public DynamicLanguage reload(ResourcePath rootDirectory) throws ExecException, InterruptedException {
        return session.require(createTask(rootDirectory)).getValue();
    }

    /**
     * Incrementally (re)compiles all dynamically loaded languages that are affected by given {@code changedResources}.
     * After calling this method, this session may not be used any more. Use the returned {@link
     * DynamicLoaderReloadSession session} instead.
     *
     * @return A {@link DynamicLoaderReloadSession reload session} which may only be used to (re)load languages.
     */
    public DynamicLoaderReloadSession updateAffectedBy(Set<? extends ResourceKey> changedResources) throws ExecException, InterruptedException {
        return new DynamicLoaderReloadSession(session.updateAffectedBy(changedResources), dynamicLoader, dynamicLoad);
    }

    /**
     * Incrementally (re)compiles all dynamically loaded languages that are affected by given {@code changedResources}.
     * After calling this method, this session may not be used any more. Use the returned {@link
     * DynamicLoaderReloadSession session} instead.
     *
     * @return A {@link DynamicLoaderReloadSession reload session} which may only be used to (re)load languages.
     */
    public DynamicLoaderReloadSession updateAffectedBy(ResourceKey... changedResources) throws ExecException, InterruptedException {
        final HashSet<ResourceKey> changedResourcesSet = new HashSet<>();
        Collections.addAll(changedResourcesSet, changedResources);
        return updateAffectedBy(changedResourcesSet);
    }
}
