package mb.spoofax.dynamicloading;

import mb.pie.api.OutTransient;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.pie.api.TaskKey;
import mb.pie.api.TopDownSession;
import mb.resource.ResourceKey;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;

import java.io.IOException;
import java.util.Set;

public class DynamicLoaderSession {
    private final Session session;
    protected final DynamicLoader dynamicLoader;
    protected final DynamicLoad dynamicLoad;

    protected DynamicLoaderSession(Session session, DynamicLoader dynamicLoader, DynamicLoad dynamicLoad) {
        this.session = session;
        this.dynamicLoader = dynamicLoader;
        this.dynamicLoad = dynamicLoad;
    }


    /**
     * Unloads language with given {@code id}.
     */
    public void unload(String id) throws IOException {
        session.unobserve(createTaskKey(id));
        dynamicLoader.unregister(id);
    }

    /**
     * Cleans up cached data for unloaded languages.
     */
    public void deleteCacheForUnloadedLanguages() throws IOException {
        session.deleteUnobservedTasks(task -> true, (task, resource) -> false); // TODO: should we delete files?
    }


    /**
     * Gets the resources that were provided by executed tasks so far this session.
     *
     * @return Read-only set of provided resources.
     */
    public Set<ResourceKey> getProvidedResources() {
        return session.getProvidedResources();
    }


    protected Task<OutTransient<DynamicLanguage>> createTask(String id, CompileToJavaClassFiles.Input compilerInput) {
        return dynamicLoad.createTask(new DynamicLoad.Input(id, compilerInput));
    }

    protected TaskKey createTaskKey(String id) {
        return new TaskKey(dynamicLoad.getId(), id);
    }
}
