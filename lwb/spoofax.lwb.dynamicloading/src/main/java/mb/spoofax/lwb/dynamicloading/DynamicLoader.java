package mb.spoofax.lwb.dynamicloading;

import mb.pie.api.MixedSession;
import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Provider;
import java.io.IOException;
import java.util.HashMap;

public class DynamicLoader implements AutoCloseable {
    private final PieComponent pieComponent;
    private final DynamicLoad dynamicLoad;
    private final HashMap<ResourcePath, DynamicLanguage> dynamicLanguages = new HashMap<>();

    public DynamicLoader(Spoofax3Compiler spoofax3Compiler, Provider<RootPieModule> basePieModuleProvider) {
        dynamicLoad = new DynamicLoad(
            spoofax3Compiler.loggerComponent,
            spoofax3Compiler.resourceServiceComponent,
            spoofax3Compiler.platformComponent,
            spoofax3Compiler.cfgComponent.getCfgRootDirectoryToObject(),
            spoofax3Compiler.component.getCompileLanguageToJavaClassPath(),
            this,
            basePieModuleProvider
        );
        pieComponent = DaggerPieComponent.builder()
            .pieModule(spoofax3Compiler.pieComponent.createChildModule(dynamicLoad))
            .loggerComponent(spoofax3Compiler.loggerComponent)
            .resourceServiceComponent(spoofax3Compiler.resourceServiceComponent)
            .build();
    }

    @Override public void close() throws Exception {
        for(DynamicLanguage dynamicLanguage : dynamicLanguages.values()) {
            dynamicLanguage.close();
        }
        dynamicLanguages.clear();
        pieComponent.close();
    }


    /**
     * Creates a new session for dynamically (re)loading languages, mimicking {@link MixedSession PIE's MixedSession}.
     *
     * @return Session for dynamically (re)loading languages. Must be closed after use with {@link
     * DynamicLoaderMixedSession#close}.
     */
    public DynamicLoaderMixedSession newSession() {
        return new DynamicLoaderMixedSession(pieComponent.getPie().newSession(), this, dynamicLoad);
    }


    void register(ResourcePath rootDirectory, DynamicLanguage dynamicLanguage) throws IOException {
        final @Nullable DynamicLanguage previousDynamicLanguage = dynamicLanguages.put(rootDirectory, dynamicLanguage);
        if(previousDynamicLanguage != null) {
            previousDynamicLanguage.close();
        }
    }

    void unregister(ResourcePath rootDirectory) throws IOException {
        final @Nullable DynamicLanguage dynamicLanguage = dynamicLanguages.remove(rootDirectory);
        if(dynamicLanguage != null) {
            dynamicLanguage.close();
        }
    }
}
