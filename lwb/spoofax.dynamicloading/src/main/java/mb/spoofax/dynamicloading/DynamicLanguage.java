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

    public DynamicLanguage(URL[] classPath, String daggerComponentClassName, PlatformComponent platformComponent) throws ReflectiveOperationException {
        this.classLoader = new URLClassLoader(classPath, DynamicLanguage.class.getClassLoader());
        final Class<?> daggerComponentClass = classLoader.loadClass(daggerComponentClassName);
        final Method builderMethod = daggerComponentClass.getDeclaredMethod("builder");
        final Object builder = builderMethod.invoke(null);
        builder.getClass().getDeclaredMethod("platformComponent", PlatformComponent.class).invoke(builder, platformComponent);
        this.languageComponent = (LanguageComponent)builder.getClass().getDeclaredMethod("build").invoke(builder);
    }

    public void close() throws IOException {
        languageComponent.getPie().close();
        classLoader.close();
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public LanguageComponent getLanguageComponent() {
        return languageComponent;
    }
}
