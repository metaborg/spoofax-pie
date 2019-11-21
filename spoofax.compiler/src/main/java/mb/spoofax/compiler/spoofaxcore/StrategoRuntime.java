package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.JavaProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Properties;

@Value.Enclosing
public class StrategoRuntime {
    private final ResourceService resourceService;
    private final Template factoryTemplate;

    private StrategoRuntime(ResourceService resourceService, Template factoryTemplate) {
        this.resourceService = resourceService;
        this.factoryTemplate = factoryTemplate;
    }

    public static StrategoRuntime fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(StrategoRuntime.class);
        return new StrategoRuntime(
            resourceService,
            templateCompiler.compile("stratego_runtime/StrategoRuntimeBuilderFactory.java.mustache")
        );
    }


    public Output compile(Input input, Charset charset) throws IOException {
        final Output output = Output.builder().withDefaultsBasedOnInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genSourcesJavaDirectory());
        genSourcesJavaDirectory.ensureDirectoryExists();

        final HierarchicalResource factoryFile = resourceService.getHierarchicalResource(output.genFactoryFile());
        try(final ResourceWriter writer = new ResourceWriter(factoryFile, charset)) {
            factoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends StrategoRuntimeData.Input.Builder implements BuilderBase {
            public Builder withPersistentProperties(Properties properties) {
                with(properties, "genFactoryClass", this::genFactoryClass);
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        JavaProject languageProject();


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        Optional<String> manualStrategoRuntimeBuilderFactoryClass();


        @Value.Default default String genFactoryClass() {
            return shared().classSuffix() + "StrategoRuntimeBuilderFactory";
        }

        @Value.Derived default String genFactoryPath() {
            return genFactoryClass() + ".java";
        }


        default void savePersistentProperties(Properties properties) {
            shared().savePersistentProperties(properties);
            properties.setProperty("genStrategoRuntimeBuilderFactoryClass", genFactoryClass());
        }

        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualStrategoRuntimeBuilderFactoryClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactoryClass' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends StrategoRuntimeData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath genSourcesJavaDirectory = input.languageProject().genSourceSpoofaxJavaDirectory().appendRelativePath(input.languageProject().packagePath());
                return this
                    .genSourcesJavaDirectory(genSourcesJavaDirectory)
                    .genFactoryFile(genSourcesJavaDirectory.appendRelativePath(input.genFactoryPath()))
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath genSourcesJavaDirectory();

        ResourcePath genFactoryFile();
    }
}
