package mb.spoofax.dynamicloading;

import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.spoofax.compiler.spoofax3.standalone.dagger.Spoofax3CompilerStandalone;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.HashMap;

public class DynamicLoader implements AutoCloseable {
    private final Pie pie;
    private final DynamicLoad dynamicLoad;
    private final HashMap<String, DynamicLanguage> dynamicLanguages = new HashMap<>();

    public DynamicLoader(Spoofax3CompilerStandalone spoofax3CompilerStandalone) {
        this.dynamicLoad = new DynamicLoad(
            spoofax3CompilerStandalone.spoofax3Compiler.resourceServiceComponent,
            spoofax3CompilerStandalone.spoofax3Compiler.platformComponent,
            spoofax3CompilerStandalone.component.getCompileToJavaClassFiles(),
            this
        );
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
     * Creates a new session for dynamically (re)loading languages, mimicking {@link MixedSession PIE's MixedSession}.
     *
     * @return Session for dynamically (re)loading languages. Must be closed after use with {@link
     * DynamicLoaderMixedSession#close}.
     */
    public DynamicLoaderMixedSession newSession() {
        return new DynamicLoaderMixedSession(pie.newSession(), this, dynamicLoad);
    }


    void register(String id, DynamicLanguage dynamicLanguage) throws IOException {
        final @Nullable DynamicLanguage previousDynamicLanguage = dynamicLanguages.put(id, dynamicLanguage);
        if(previousDynamicLanguage != null) {
            previousDynamicLanguage.close();
        }
    }

    void unregister(String id) throws IOException {
        final @Nullable DynamicLanguage dynamicLanguage = dynamicLanguages.remove(id);
        if(dynamicLanguage != null) {
            dynamicLanguage.close();
        }
    }
}
