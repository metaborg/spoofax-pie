package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Value.Enclosing
public class StrategoRuntime {
    private final Template factoryTemplate;
    private final ResourceService resourceService;
    private final Charset charset;


    private StrategoRuntime(Template factoryTemplate, ResourceService resourceService, Charset charset) {
        this.resourceService = resourceService;
        this.factoryTemplate = factoryTemplate;
        this.charset = charset;
    }

    public static StrategoRuntime fromClassLoaderResources(ResourceService resourceService, Charset charset) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(StrategoRuntime.class);
        return new StrategoRuntime(
            templateCompiler.compile("stratego_runtime/StrategoRuntimeBuilderFactory.java.mustache"),
            resourceService,
            charset
        );
    }


    public LanguageProjectOutput compileLanguageProject(Input input) throws IOException {
        final LanguageProjectOutput output = LanguageProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genDirectory());
        genSourcesJavaDirectory.ensureDirectoryExists();

        final HierarchicalResource factoryFile = resourceService.getHierarchicalResource(output.genFactoryFile());
        try(final ResourceWriter writer = new ResourceWriter(factoryFile, charset)) {
            factoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }

    public AdapterProjectOutput compileAdapterProject(Input input, GradleProject languageProject) throws IOException {
        return AdapterProjectOutput.builder().build();
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


        List<String> interopRegisterersByReflection();

        boolean addNaBL2Primitives();

        boolean addStatixPrimitives();


        @Value.Default default boolean copyCTree() {
            return false;
        }

        @Value.Default default boolean copyClasses() {
            return true;
        }

        boolean copyJavaStrategyClasses();


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        Optional<String> manualFactoryClass();

        @Value.Default default String genFactoryClass() {
            return shared().classSuffix() + "StrategoRuntimeBuilderFactory";
        }

        @Value.Derived default String genFactoryFileName() {
            return genFactoryClass() + ".java";
        }

        @Value.Derived default String factoryClass() {
            if(classKind().isManual() && manualFactoryClass().isPresent()) {
                return manualFactoryClass().get();
            }
            return genFactoryClass();
        }


        default void savePersistentProperties(Properties properties) {
            shared().savePersistentProperties(properties);
            properties.setProperty("genStrategoRuntimeBuilderFactoryClass", genFactoryClass());
        }

        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualFactoryClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactoryClass' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface LanguageProjectOutput extends Serializable {
        class Builder extends StrategoRuntimeData.LanguageProjectOutput.Builder {
            public Builder fromInput(Input input) {
                final GradleProject languageProject = input.shared().languageProject();
                final ResourcePath genDirectory = languageProject.genSourceSpoofaxJavaDirectory().appendRelativePath(languageProject.packagePath());
                genDirectory(genDirectory);
                genFactoryFile(genDirectory.appendRelativePath(input.genFactoryFileName()));
                addDependencies(
                    GradleConfiguredDependency.api(input.shared().strategoCommonDep()),
                    GradleConfiguredDependency.api(input.shared().orgStrategoXTStrjDep()),
                    GradleConfiguredDependency.implementation(input.shared().strategoXTMinJarDep())
                );
                // NaBL2 (required by Statix as well)
                if(input.addNaBL2Primitives() || input.addStatixPrimitives()) {
                    addDependencies(GradleConfiguredDependency.implementation(input.shared().nabl2CommonDep()));
                }
                if(input.addStatixPrimitives()) {
                    addDependencies(GradleConfiguredDependency.implementation(input.shared().statixCommonDep()));
                    addCopyResources("src-gen/statix/statics.spec.aterm");
                }
                if(input.copyCTree()) {
                    addCopyResources("target/metaborg/stratego.ctree");
                }
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath genDirectory();

        ResourcePath genFactoryFile();


        List<GradleConfiguredDependency> dependencies();

        List<String> copyResources();
    }

    @Value.Immutable
    public interface AdapterProjectOutput extends Serializable {
        class Builder extends StrategoRuntimeData.AdapterProjectOutput.Builder {

        }

        static Builder builder() {
            return new Builder();
        }
    }
}
