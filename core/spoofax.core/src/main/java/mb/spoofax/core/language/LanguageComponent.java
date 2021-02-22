package mb.spoofax.core.language;

import mb.pie.dagger.TaskDefsProvider;

public interface LanguageComponent extends TaskDefsProvider, AutoCloseable {
    LanguageInstance getLanguageInstance();


    @Override default void close() throws Exception {
        // Override to make Dagger not treat this as a component method.
    }
}
