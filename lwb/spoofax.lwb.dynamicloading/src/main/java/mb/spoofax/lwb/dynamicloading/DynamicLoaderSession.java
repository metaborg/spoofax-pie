package mb.spoofax.lwb.dynamicloading;

import mb.pie.api.OutTransient;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.pie.api.TaskKey;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;

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
    public void unload(ResourcePath rootDirectory) throws IOException {
        session.unobserve(createTaskKey(rootDirectory));
        dynamicLoader.unregister(rootDirectory);
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


    protected Task<OutTransient<DynamicLanguage>> createTask(ResourcePath rootDirectory) {
        return dynamicLoad.createTask(rootDirectory);
    }

    protected TaskKey createTaskKey(ResourcePath rootDirectory) {
        return new TaskKey(dynamicLoad.getId(), rootDirectory);
    }
}
