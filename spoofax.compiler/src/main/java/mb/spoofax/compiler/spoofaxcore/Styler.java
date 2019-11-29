package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleAddDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
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

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genDirectory());
        genSourcesJavaDirectory.ensureDirectoryExists();

        final HashMap<String, Object> map = new HashMap<>();
        map.put("packedESVFile", packedESVTargetRelPath(input.shared().languageProject())); // TODO: move to input?

        final HierarchicalResource rulesFile = resourceService.getHierarchicalResource(output.genRulesFile());
        try(final ResourceWriter writer = new ResourceWriter(rulesFile, charset)) {
            rulesTemplate.execute(input, map, writer);
            writer.flush();
        }

        final HierarchicalResource stylerFile = resourceService.getHierarchicalResource(output.genStylerFile());
        try(final ResourceWriter writer = new ResourceWriter(stylerFile, charset)) {
            stylerTemplate.execute(input, map, writer);
            writer.flush();
        }

        final HierarchicalResource factoryFile = resourceService.getHierarchicalResource(output.genFactoryFile());
        try(final ResourceWriter writer = new ResourceWriter(factoryFile, charset)) {
            factoryTemplate.execute(input, map, writer);
            writer.flush();
        }

        return output;
    }

    private static String packedESVSourceRelPath() {
        return "target/metaborg/editor.esv.af";
    }

    private static String packedESVTargetRelPath(GradleProject languageProject) {
        return languageProject.packagePath() + "/" + packedESVSourceRelPath();
    }


    public AdapterProjectOutput compileAdapterProject(Input input, GradleProject adapterProject) throws IOException {
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


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        Optional<String> manualStylerClass();

        Optional<String> manualFactoryClass();

        @Value.Default default String genRulesClass() {
            return shared().classSuffix() + "StylingRules";
        }

        @Value.Derived default String genRulesFileName() {
            return genRulesClass() + ".java";
        }

        @Value.Default default String genStylerClass() {
            return shared().classSuffix() + "Styler";
        }

        @Value.Derived default String genStylerFileName() {
            return genStylerClass() + ".java";
        }

        @Value.Default default String genFactoryClass() {
            return shared().classSuffix() + "StylerFactory";
        }

        @Value.Derived default String genFactoryFileName() {
            return genFactoryClass() + ".java";
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
                final GradleProject languageProject = input.shared().languageProject();
                final ResourcePath genDirectory = languageProject.genSourceSpoofaxJavaDirectory().appendRelativePath(languageProject.packagePath());
                genDirectory(genDirectory);
                genRulesFile(genDirectory.appendRelativePath(input.genRulesFileName()));
                genStylerFile(genDirectory.appendRelativePath(input.genStylerFileName()));
                genFactoryFile(genDirectory.appendRelativePath(input.genFactoryFileName()));
                addDependencies(GradleAddDependency.api(input.shared().esvCommonDep()));
                addCopyResources(packedESVSourceRelPath());
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath genDirectory();

        ResourcePath genRulesFile();

        ResourcePath genStylerFile();

        ResourcePath genFactoryFile();


        List<GradleAddDependency> dependencies();

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
