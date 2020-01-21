package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class StrategoRuntime {
    private final Template factoryTemplate;
    private final ResourceService resourceService;
    private final Charset charset;


    private StrategoRuntime(
        Template factoryTemplate,
        ResourceService resourceService,
        Charset charset
    ) {
        this.resourceService = resourceService;
        this.factoryTemplate = factoryTemplate;
        this.charset = charset;
    }

    public static StrategoRuntime fromClassLoaderResources(
        ResourceService resourceService,
        Charset charset
    ) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(StrategoRuntime.class, resourceService, charset);
        return new StrategoRuntime(
            templateCompiler.getOrCompile("stratego_runtime/StrategoRuntimeBuilderFactory.java.mustache"),
            resourceService,
            charset
        );
    }


    public LanguageProjectOutput compileLanguageProject(Input input) throws IOException {
        final LanguageProjectOutput output = LanguageProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final ResourcePath genDirectory = input.languageGenDirectory();
        resourceService.getHierarchicalResource(genDirectory).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genFactory().file(genDirectory)).createParents(), charset)) {
            factoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }


    public AdapterProjectOutput compileAdapterProject(Input input) throws IOException {
        return AdapterProjectOutput.builder().build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends StrategoRuntimeData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        /// Configuration

        List<String> interopRegisterersByReflection();

        boolean addNaBL2Primitives();

        boolean addStatixPrimitives();


        /// Whether to copy certain files from the Spoofax 2.x project.

        @Value.Default default boolean copyCTree() {
            return false;
        }

        @Value.Default default boolean copyClasses() {
            return true;
        }

        boolean copyJavaStrategyClasses();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        default ResourcePath languageGenDirectory() {
            return shared().languageProject().genSourceSpoofaxJavaDirectory();
        }

        // Stratego runtime builder factory

        @Value.Default default TypeInfo genFactory() {
            return TypeInfo.of(shared().languagePackage(), shared().classPrefix() + "StrategoRuntimeBuilderFactory");
        }

        Optional<TypeInfo> manualFactory();

        default TypeInfo factory() {
            if(classKind().isManual() && manualFactory().isPresent()) {
                return manualFactory().get();
            }
            return genFactory();
        }


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualFactory().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactory' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface LanguageProjectOutput extends Serializable {
        class Builder extends StrategoRuntimeData.LanguageProjectOutput.Builder {
            public Builder fromInput(Input input) {
                final Shared shared = input.shared();
                addDependencies(
                    GradleConfiguredDependency.api(shared.strategoCommonDep()),
                    GradleConfiguredDependency.api(shared.orgStrategoXTStrjDep()),
                    GradleConfiguredDependency.implementation(shared.strategoXTMinJarDep())
                );
                // NaBL2 (required by Statix as well)
                if(input.addNaBL2Primitives() || input.addStatixPrimitives()) {
                    addDependencies(GradleConfiguredDependency.implementation(shared.nabl2CommonDep()));
                }
                if(input.addStatixPrimitives()) {
                    addDependencies(GradleConfiguredDependency.implementation(shared.statixCommonDep()));
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


        List<GradleConfiguredDependency> dependencies();

        List<String> copyResources();
    }

    @Value.Immutable
    public interface AdapterProjectOutput extends Serializable {
        class Builder extends StrategoRuntimeData.AdapterProjectOutput.Builder {}

        static Builder builder() {
            return new Builder();
        }


        List<GradleConfiguredDependency> dependencies();

        List<TypeInfo> additionalTaskDefs();
    }
}
