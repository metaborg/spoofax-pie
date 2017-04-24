package mb.pipe.run.cmd;

import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.editor.NullEditorRegistry;
import org.metaborg.spoofax.core.SpoofaxModule;

import com.google.inject.Singleton;

public class NullEditorModule extends SpoofaxModule {
    @Override protected void bindEditor() {
        bind(IEditorRegistry.class).to(NullEditorRegistry.class).in(Singleton.class);
    }
}
