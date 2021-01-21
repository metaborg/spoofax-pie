package mb.spoofax.dynamicloading;

import mb.pie.api.ExecException;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.OutTransient;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.api.TaskKey;
import mb.resource.ResourceKey;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;
import mb.spoofax.compiler.spoofax3.standalone.dagger.Spoofax3CompilerStandalone;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DynamicLoader implements AutoCloseable {
    private final Spoofax3CompilerStandalone spoofax3CompilerStandalone;
    private final Pie pie;
    private final DynamicLoad dynamicLoad;
    private final HashMap<String, DynamicLanguage> dynamicLanguages = new HashMap<>();

    public DynamicLoader(PlatformComponent platformComponent) {
        this.spoofax3CompilerStandalone = new Spoofax3CompilerStandalone(platformComponent);
        this.dynamicLoad = new DynamicLoad(platformComponent, spoofax3CompilerStandalone.component.getCompileToJavaClassFiles(), this);
        this.pie = spoofax3CompilerStandalone.component.getPie().createChildBuilder()
            .addTaskDefs(new MapTaskDefs(dynamicLoad))
            .build();
    }

    @Override public void close() throws Exception {
        for(DynamicLanguage dynamicLanguage : dynamicLanguages.values()) {
            dynamicLanguage.close();
        }
        dynamicLanguages.clear();
        pie.close();
    }

    /**
     * Incrementally compiles, and(re))loads the compiled language with given {@code id} and {@code compilerInput}.
     */
    public DynamicLanguage load(String id, CompileToJavaClassFiles.Input compilerInput) throws ExecException, InterruptedException {
        try(final MixedSession session = pie.newSession()) {
            return session.require(createTask(id, compilerInput)).getValue();
        }
    }

    /**
     * Incrementally compiles all dynamically loaded languages that are affected by given {@code changedResources}.
     */
    public void updateAffectedBy(Set<? extends ResourceKey> changedResources) throws ExecException, InterruptedException {
        try(final MixedSession session = pie.newSession()) {
            session.updateAffectedBy(changedResources);
        }
    }

    public void updateAffectedBy(ResourceKey... changedResources) throws ExecException, InterruptedException {
        final HashSet<ResourceKey> changedResourcesSet = new HashSet<>();
        Collections.addAll(changedResourcesSet, changedResources);
        updateAffectedBy(changedResourcesSet);
    }

    /**
     * Unloads language with given {@code id}.
     */
    public void unload(String id) throws IOException {
        try(final MixedSession session = pie.newSession()) {
            session.unobserve(createTaskKey(id));
        }
        final @Nullable DynamicLanguage dynamicLanguage = dynamicLanguages.remove(id);
        if(dynamicLanguage != null) {
            dynamicLanguage.close();
        }
    }

    /**
     * Cleans up cached data for unloaded languages.
     */
    public void deleteCacheForUnloadedLanguages() throws IOException {
        try(final MixedSession session = pie.newSession()) {
            session.deleteUnobservedTasks(task -> true, (task, resource) -> false);
        }
    }

    void register(String id, DynamicLanguage dynamicLanguage) throws IOException {
        final @Nullable DynamicLanguage previousDynamicLanguage = dynamicLanguages.put(id, dynamicLanguage);
        if(previousDynamicLanguage != null) {
            previousDynamicLanguage.close();
        }
    }

    private Task<OutTransient<DynamicLanguage>> createTask(String id, CompileToJavaClassFiles.Input compilerInput) {
        return dynamicLoad.createTask(new DynamicLoad.Input(id, compilerInput));
    }

    private TaskKey createTaskKey(String id) {
        return new TaskKey(dynamicLoad.getId(), id);
    }
}
