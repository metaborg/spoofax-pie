package mb.stratego.common;

import mb.resource.ReadableResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.lang.InteropRegisterer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class StrategoRuntimeBuilder {
    @SuppressWarnings("NullableProblems") private ITermFactory termFactory;
    private final ArrayList<String> components = new ArrayList<>();
    private final ArrayList<IOperatorRegistry> libraries = new ArrayList<>();
    private final ArrayList<ReadableResource> ctrees = new ArrayList<>();
    private final ArrayList<URL> jars = new ArrayList<>();
    private final ArrayList<InteropRegisterer> interopRegisterers = new ArrayList<>();
    private final ArrayList<String> interopRegisterersByReflection = new ArrayList<>();
    private @Nullable ClassLoader jarParentClassLoader;


    public StrategoRuntimeBuilder() {
        withDefaultTermFactory();
        withDefaultComponents();
        withDefaultJarParentClassLoader();
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

    public StrategoRuntimeBuilder withDefaultJarParentClassLoader() {
        jarParentClassLoader = null;
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

    public StrategoRuntimeBuilder addCtree(ReadableResource ctree) {
        this.ctrees.add(ctree);
        return this;
    }

    public StrategoRuntimeBuilder addJar(URL jar) {
        this.jars.add(jar);
        return this;
    }

    public StrategoRuntimeBuilder addInteropRegisterer(InteropRegisterer interopRegisterer) {
        this.interopRegisterers.add(interopRegisterer);
        return this;
    }

    public StrategoRuntimeBuilder addInteropRegistererByReflection(String className) {
        this.interopRegisterersByReflection.add(className);
        return this;
    }

    public StrategoRuntimeBuilder withJarParentClassLoader(ClassLoader jarParentClassLoader) {
        this.jarParentClassLoader = jarParentClassLoader;
        return this;
    }


    public StrategoRuntime build() throws StrategoRuntimeBuilderException {
        final HybridInterpreter hybridInterpreter = new HybridInterpreter(termFactory);
        for(String component : components) {
            hybridInterpreter.getCompiledContext().registerComponent(component);
        }

        for(IOperatorRegistry library : libraries) {
            hybridInterpreter.getCompiledContext().addOperatorRegistry(library);
        }

        for(ReadableResource resource : ctrees) {
            try {
                // Load buffers the input stream, and closes the buffered stream, which closes our stream.
                hybridInterpreter.load(resource.newInputStream());
            } catch(IOException | InterpreterException e) {
                throw new StrategoRuntimeBuilderException(
                    "Loading Stratego ctree from resource '" + resource + "' failed unexpectedly", e);
            }
        }

        final URL[] classpath = jars.toArray(new URL[0]);
        if(classpath.length > 0) {
            try {
                hybridInterpreter.loadJars(jarParentClassLoader, classpath);
            } catch(IOException | IncompatibleJarException e) {
                throw new StrategoRuntimeBuilderException(
                    "Loading Stratego JAR from resources '" + jars + "' failed unexpectedly",
                    e);
            }
        }

        for(InteropRegisterer interopRegisterer : interopRegisterers) {
            hybridInterpreter.registerClass(interopRegisterer, jarParentClassLoader);
        }

        for(String interopRegistererClassName : interopRegisterersByReflection) {
            try {
                final Class<?> interopRegistererClass = Class.forName(interopRegistererClassName);
                final InteropRegisterer interopRegisterer = (InteropRegisterer) interopRegistererClass.newInstance();
                hybridInterpreter.registerClass(interopRegisterer, jarParentClassLoader);
            } catch(IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                throw new StrategoRuntimeBuilderException(
                    "Loading InteropRegisterer '" + interopRegistererClassName + "' by reflection failed unexpectedly",
                    e);
            }
        }

        hybridInterpreter.getCompiledContext().getExceptionHandler().setEnabled(false);
        hybridInterpreter.init();

        return new StrategoRuntime(hybridInterpreter);
    }
}
