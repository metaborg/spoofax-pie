package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
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
public class Styler {
    private final Template rulesTemplate;
    private final Template stylerTemplate;
    private final Template factoryTemplate;
    private final ResourceService resourceService;
    private final Charset charset;


    private Styler(Template rulesTemplate, Template stylerTemplate, Template factoryTemplate, ResourceService resourceService, Charset charset) {
        this.resourceService = resourceService;
        this.rulesTemplate = rulesTemplate;
        this.stylerTemplate = stylerTemplate;
        this.factoryTemplate = factoryTemplate;
        this.charset = charset;
    }

    public static Styler fromClassLoaderResources(ResourceService resourceService, Charset charset) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(Styler.class);
        return new Styler(
            templateCompiler.compile("styler/StylingRules.java.mustache"),
            templateCompiler.compile("styler/Styler.java.mustache"),
            templateCompiler.compile("styler/StylerFactory.java.mustache"),
            resourceService,
            charset
        );
    }


    public LanguageProjectOutput compileLanguageProject(Input input) throws IOException {
        final LanguageProjectOutput output = LanguageProjectOutput.builder().from(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        resourceService.getHierarchicalResource(input.genDirectory()).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genRulesFile()), charset)) {
            rulesTemplate.execute(input, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genStylerFile()), charset)) {
            stylerTemplate.execute(input, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genFactoryFile()), charset)) {
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
        class Builder extends StylerData.Input.Builder implements BuilderBase {
            public Builder withPersistentProperties(Properties properties) {
                with(properties, "genRulesClass", this::genRulesClass);
                with(properties, "genStylerClass", this::genStylerClass);
                with(properties, "genFactoryClass", this::genFactoryClass);
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        @Value.Default default String packedESVSourceRelPath() {
            return "target/metaborg/editor.esv.af";
        }

        @Value.Default default String packedESVTargetRelPath() {
            return shared().languageProject().packagePath() + "/" + packedESVSourceRelPath();
        }


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        @Value.Derived default ResourcePath genDirectory() {
            final GradleProject languageProject = shared().languageProject();
            return languageProject.genSourceSpoofaxJavaDirectory().appendRelativePath(languageProject.packagePath());
        }


        @Value.Default default String genRulesClass() {
            return shared().classSuffix() + "StylingRules";
        }

        @Value.Derived default String genRulesFileName() {
            return genRulesClass() + ".java";
        }

        @Value.Derived default ResourcePath genRulesFile() {
            return genDirectory().appendSegment(genRulesFileName());
        }


        @Value.Default default String genStylerClass() {
            return shared().classSuffix() + "Styler";
        }

        @Value.Derived default String genStylerFileName() {
            return genStylerClass() + ".java";
        }

        @Value.Derived default ResourcePath genStylerFile() {
            return genDirectory().appendSegment(genStylerFileName());
        }

        Optional<String> manualStylerClass();

        @Value.Derived default String stylerClass() {
            if(classKind().isManual() && manualStylerClass().isPresent()) {
                return manualStylerClass().get();
            }
            return genStylerClass();
        }


        @Value.Default default String genFactoryClass() {
            return shared().classSuffix() + "StylerFactory";
        }

        @Value.Derived default String genFactoryFileName() {
            return genFactoryClass() + ".java";
        }

        @Value.Derived default ResourcePath genFactoryFile() {
            return genDirectory().appendSegment(genFactoryFileName());
        }

        Optional<String> manualFactoryClass();

        @Value.Derived default String factoryClass() {
            if(classKind().isManual() && manualFactoryClass().isPresent()) {
                return manualFactoryClass().get();
            }
            return genFactoryClass();
        }


        default void savePersistentProperties(Properties properties) {
            shared().savePersistentProperties(properties);
            properties.setProperty("genRulesClass", genRulesClass());
            properties.setProperty("genStylerClass", genStylerClass());
            properties.setProperty("genFactoryClass", genFactoryClass());
        }

        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualStylerClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualStylerClass' has not been set");
            }
            if(!manualFactoryClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactoryClass' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface LanguageProjectOutput extends Serializable {
        class Builder extends StylerData.LanguageProjectOutput.Builder {
            public Builder from(Input input) {
                addDependencies(GradleConfiguredDependency.api(input.shared().esvCommonDep()));
                addCopyResources(input.packedESVSourceRelPath());
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
        class Builder extends StylerData.AdapterProjectOutput.Builder {

        }

        static Builder builder() {
            return new Builder();
        }
    }
}
