package mb.spoofax.dynamicloading;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicLanguage {
    private final URLClassLoader classLoader;
    private final LanguageComponent languageComponent;
    private boolean closed = false;

    public DynamicLanguage(URL[] classPath, String daggerComponentClassName, PlatformComponent platformComponent) throws ReflectiveOperationException {
        this.classLoader = new URLClassLoader(classPath, DynamicLanguage.class.getClassLoader());
        final Class<?> daggerComponentClass = classLoader.loadClass(daggerComponentClassName);
        final Method builderMethod = daggerComponentClass.getDeclaredMethod("builder");
        final Object builder = builderMethod.invoke(null);
        builder.getClass().getDeclaredMethod("platformComponent", PlatformComponent.class).invoke(builder, platformComponent);
        this.languageComponent = (LanguageComponent)builder.getClass().getDeclaredMethod("build").invoke(builder);
    }

    /**
     * Closes the dynamic language, closing the {@link URLClassLoader classloader} and {@link LanguageComponent language
     * component}, freeing any resources they hold. This dynamic language cannot be used any more after closing it.
     *
     * @throws IOException when {@link URLClassLoader#close() closing the classloader} fails.
     */
    public void close() throws IOException {
        if(closed) return;
        try {
            languageComponent.getPie().close();
            classLoader.close();
        } finally {
            closed = true;
        }
    }

    /**
     * Gets the {@link URLClassLoader classloader} of this dynamic language.
     *
     * @throws IllegalStateException if the dynamic language has been closed with {@link #close}.
     */
    public URLClassLoader getClassLoader() {
        if(closed) throw new IllegalStateException("Cannot get class loader, dynamic language has been closed");
        return classLoader;
    }

    /**
     * Gets the {@link LanguageComponent language component} of this dynamic language.
     *
     * @throws IllegalStateException if the dynamic language has been closed with {@link #close}.
     */
    public LanguageComponent getLanguageComponent() {
        if(closed) throw new IllegalStateException("Cannot get language component, dynamic language has been closed");
        return languageComponent;
    }

    /**
     * @return true if the dynamic language has been closed with {@link #close}.
     */
    public boolean isClosed() {
        return closed;
    }
}
