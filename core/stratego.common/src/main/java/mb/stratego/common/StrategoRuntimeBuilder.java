package mb.stratego.common;

import mb.log.api.LoggerFactory;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
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
    private ITermFactory termFactory;
    private StrategoIOAgent ioAgent;
    private @Nullable ClassLoader jarParentClassLoader;
    private AdaptableContext contextObject;

    private final ArrayList<String> components;
    private final ArrayList<IOperatorRegistry> libraries;
    private final ArrayList<ReadableResource> ctrees;
    private final ArrayList<URL> jars;
    private final ArrayList<InteropRegisterer> interopRegisterers;
    private final ArrayList<String> interopRegisterersByReflection;


    public StrategoRuntimeBuilder(LoggerFactory loggerFactory, ResourceService resourceService) {
        this.termFactory = defaultTermFactory();
        this.ioAgent = defaultIoAgent(loggerFactory, resourceService);
        this.jarParentClassLoader = null;
        this.contextObject = new AdaptableContext();

        this.components = defaultComponents();
        this.libraries = new ArrayList<>();
        this.ctrees = new ArrayList<>();
        this.jars = new ArrayList<>();
        this.interopRegisterers = new ArrayList<>();
        this.interopRegisterersByReflection = new ArrayList<>();
    }

    public StrategoRuntimeBuilder(StrategoRuntimeBuilder other) {
        this.termFactory = other.termFactory;
        this.ioAgent = new StrategoIOAgent(other.ioAgent);
        this.jarParentClassLoader = other.jarParentClassLoader;
        this.contextObject = new AdaptableContext(other.contextObject);

        this.components = new ArrayList<>(other.components);
        this.libraries = new ArrayList<>(other.libraries);
        this.ctrees = new ArrayList<>(other.ctrees);
        this.jars = new ArrayList<>(other.jars);
        this.interopRegisterers = new ArrayList<>(other.interopRegisterers);
        this.interopRegisterersByReflection = new ArrayList<>(other.interopRegisterersByReflection);
    }


    private static ImploderOriginTermFactory defaultTermFactory() {
        return new ImploderOriginTermFactory(new TermFactory());
    }

    public StrategoRuntimeBuilder withDefaultTermFactory() {
        this.termFactory = defaultTermFactory();
        return this;
    }

    private static ArrayList<String> defaultComponents() {
        final ArrayList<String> components = new ArrayList<>();
        components.add("stratego_lib");
        components.add("stratego_sglr");
        return components;
    }

    public StrategoRuntimeBuilder withDefaultComponents() {
        this.components.clear();
        this.components.addAll(defaultComponents());
        return this;
    }

    private static StrategoIOAgent defaultIoAgent(LoggerFactory loggerFactory, ResourceService resourceService) {
        return new StrategoIOAgent(loggerFactory, resourceService);
    }

    public StrategoRuntimeBuilder withDefaultIoAgent(LoggerFactory loggerFactory, ResourceService resourceService) {
        this.ioAgent = defaultIoAgent(loggerFactory, resourceService);
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

    public StrategoRuntimeBuilder withIoAgent(StrategoIOAgent ioAgent) {
        this.ioAgent = ioAgent;
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

    public StrategoRuntimeBuilder withContextObject(AdaptableContext contextObject) {
        this.contextObject = contextObject;
        return this;
    }

    public StrategoRuntimeBuilder addContextObject(Object contextObject) {
        this.contextObject.put(contextObject);
        return this;
    }

    public StrategoRuntimeBuilder addContextObjectsFrom(AdaptableContext contextObject) {
        this.contextObject.putAll(contextObject);
        return this;
    }


    public StrategoRuntimeBuilder copy() {
        return new StrategoRuntimeBuilder(this);
    }


    /**
     * @throws RuntimeException When building the Stratego runtime fails unexpectedly.
     */
    public StrategoRuntime build() {
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
                hybridInterpreter.load(resource.openRead());
            } catch(IOException | InterpreterException e) {
                throw new RuntimeException("Loading Stratego CTree from resource '" + resource + "' failed unexpectedly", e);
            }
        }

        final URL[] classpath = jars.toArray(new URL[0]);
        if(classpath.length > 0) {
            try {
                hybridInterpreter.loadJars(jarParentClassLoader, classpath);
            } catch(IOException | IncompatibleJarException e) {
                throw new RuntimeException("Loading Stratego JAR from resources '" + jars + "' failed unexpectedly", e);
            }
        }

        for(InteropRegisterer interopRegisterer : interopRegisterers) {
            hybridInterpreter.registerClass(interopRegisterer, jarParentClassLoader);
        }

        for(String interopRegistererClassName : interopRegisterersByReflection) {
            try {
                final Class<?> interopRegistererClass = Class.forName(interopRegistererClassName);
                final InteropRegisterer interopRegisterer = (InteropRegisterer)interopRegistererClass.newInstance();
                hybridInterpreter.registerClass(interopRegisterer, jarParentClassLoader);
            } catch(IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                throw new RuntimeException("Loading InteropRegisterer '" + interopRegistererClassName + "' by reflection failed unexpectedly", e);
            }
        }

        hybridInterpreter.getCompiledContext().getExceptionHandler().setEnabled(false);
        hybridInterpreter.init();

        return new StrategoRuntime(hybridInterpreter, ioAgent, contextObject);
    }

    public StrategoRuntime buildFromPrototype(StrategoRuntime prototype) {
        final HybridInterpreter hybridInterpreter = new HybridInterpreter(prototype.getHybridInterpreter());

        hybridInterpreter.getCompiledContext().getExceptionHandler().setEnabled(false);

        // Add primitive libraries again, to make sure that our libraries override any default ones.
        for(IOperatorRegistry library : libraries) {
            hybridInterpreter.getCompiledContext().addOperatorRegistry(library);
        }

        hybridInterpreter.getContext().setFactory(termFactory);
        hybridInterpreter.getCompiledContext().setFactory(termFactory);

        hybridInterpreter.init();

        return new StrategoRuntime(hybridInterpreter, new StrategoIOAgent(prototype.getIoAgent()), new AdaptableContext(prototype.getContextObject()));
    }
}
