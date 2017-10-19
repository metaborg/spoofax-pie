package mb.spoofax.runtime.benchmark;

import com.google.inject.Singleton;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.editor.NullEditorRegistry;
import org.metaborg.spoofax.core.SpoofaxModule;

public class SpoofaxCoreModule extends SpoofaxModule {
    @Override protected void bindEditor() {
        bind(IEditorRegistry.class).to(NullEditorRegistry.class).in(Singleton.class);
    }
}
