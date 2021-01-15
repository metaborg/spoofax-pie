package mb.spoofax.dynamicloading;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicUrlClassLoader implements AutoCloseable {
    private URLClassLoader classLoader;

    public DynamicUrlClassLoader(ClassLoader parent) {
        this.classLoader = new URLClassLoader(new URL[0], parent);
    }

    public DynamicUrlClassLoader(URL[] urls, ClassLoader parent) {
        this.classLoader = new URLClassLoader(urls, parent);
    }

    public DynamicUrlClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override public void close() throws IOException {
        classLoader.close();
    }

    Class<?> loadClass(String name) throws ClassNotFoundException {
        return classLoader.loadClass(name);
    }

    void reload(URL[] urls, ClassLoader parent) throws IOException {
        this.classLoader.close();
        this.classLoader = new URLClassLoader(urls, parent);
    }

    void reload(URLClassLoader classLoader) throws IOException {
        this.classLoader.close();
        this.classLoader = classLoader;
    }
}
