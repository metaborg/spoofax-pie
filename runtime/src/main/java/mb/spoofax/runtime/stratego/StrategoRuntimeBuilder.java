package mb.spoofax.runtime.stratego;

import mb.fs.api.node.FSNode;
import mb.fs.java.JavaFSPath;
import mb.spoofax.api.SpoofaxEx;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;

public class StrategoRuntimeBuilder {
    private ITermFactory termFactory;
    private final ArrayList<String> components = new ArrayList<>();
    private final ArrayList<IOperatorRegistry> libraries = new ArrayList<>();
    private final ArrayList<JavaFSPath> ctrees = new ArrayList<>();
    private final ArrayList<URI> jars = new ArrayList<>();
    private @Nullable ClassLoader jarClassLoader;


    public StrategoRuntimeBuilder() {
        withDefaultTermFactory();
        withDefaultComponents();
        withDefaultJarClassLoader();
    }


    public StrategoRuntimeBuilder withDefaultTermFactory() {
        termFactory = new ImploderOriginTermFactory(new TermFactory());
        return this;
    }

    public StrategoRuntimeBuilder withDefaultComponents() {
        components.add("stratego_lib");
        components.add("stratego_sglr");
        return this;
    }

    public StrategoRuntimeBuilder withDefaultJarClassLoader() {
        jarClassLoader = null;
        return this;
    }


    public StrategoRuntimeBuilder withTermFactory(ITermFactory termFactory) {
        this.termFactory = termFactory;
        return this;
    }

    public StrategoRuntimeBuilder addComponent(String component) {
        this.components.add(component);
        return this;
    }

    public StrategoRuntimeBuilder addLibrary(IOperatorRegistry library) {
        this.libraries.add(library);
        return this;
    }

    public StrategoRuntimeBuilder addCtree(JavaFSPath ctree) {
        this.ctrees.add(ctree);
        return this;
    }

    public StrategoRuntimeBuilder addJar(URI jar) {
        this.jars.add(jar);
        return this;
    }

    public StrategoRuntimeBuilder withJarClassLoader(ClassLoader jarClassLoader) {
        this.jarClassLoader = jarClassLoader;
        return this;
    }


    public StrategoRuntime build() throws SpoofaxEx {
        return new StrategoRuntime(termFactory, components, libraries, ctrees, jars, jarClassLoader);
    }
}
