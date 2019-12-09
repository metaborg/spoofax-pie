package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class Styler {
    private final Template rulesTemplate;
    private final Template stylerTemplate;
    private final Template factoryTemplate;
    private final Template styleTaskDefTemplate;
    private final ResourceService resourceService;
    private final Charset charset;


    private Styler(
        Template rulesTemplate,
        Template stylerTemplate,
        Template factoryTemplate,
        Template styleTaskDefTemplate,
        ResourceService resourceService,
        Charset charset
    ) {
        this.styleTaskDefTemplate = styleTaskDefTemplate;
        this.resourceService = resourceService;
        this.rulesTemplate = rulesTemplate;
        this.stylerTemplate = stylerTemplate;
        this.factoryTemplate = factoryTemplate;
        this.charset = charset;
    }

    public static Styler fromClassLoaderResources(
        ResourceService resourceService,
        Charset charset
    ) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(Styler.class);
        return new Styler(
            templateCompiler.compile("styler/StylingRules.java.mustache"),
            templateCompiler.compile("styler/Styler.java.mustache"),
            templateCompiler.compile("styler/StylerFactory.java.mustache"),
            templateCompiler.compile("styler/StyleTaskDef.java.mustache"),
            resourceService,
            charset
        );
    }


    public LanguageProjectOutput compileLanguageProject(Input input) throws IOException {
        final LanguageProjectOutput output = LanguageProjectOutput.builder().from(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final ResourcePath genDirectory = input.languageGenDirectory();
        resourceService.getHierarchicalResource(genDirectory).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genRules().file(genDirectory)).createParents(), charset)) {
            rulesTemplate.execute(input, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genStyler().file(genDirectory)).createParents(), charset)) {
            stylerTemplate.execute(input, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genFactory().file(genDirectory)).createParents(), charset)) {
            factoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }

    public AdapterProjectOutput compileAdapterProject(Input input) throws IOException {
        final AdapterProjectOutput output = AdapterProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final ResourcePath genDirectory = input.adapterGenDirectory();
        resourceService.getHierarchicalResource(genDirectory).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genStyleTaskDef().file(genDirectory)).createParents(), charset)) {
            styleTaskDefTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends StylerData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        Parser.Input parser();


        /// Packed ESV source file (to copy from), and destination file

        @Value.Default default String packedESVSourceRelPath() {
            return "target/metaborg/editor.esv.af";
        }

        @Value.Default default String packedESVTargetRelPath() {
            return shared().languageProject().packagePath() + "/" + packedESVSourceRelPath();
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        default ResourcePath languageGenDirectory() {
            return shared().languageProject().genSourceSpoofaxJavaDirectory();
        }

        // Styling rules

        @Value.Default default TypeInfo genRules() {
            return TypeInfo.of(shared().languagePackage(), shared().classSuffix() + "StylingRules");
        }

        // Styler

        @Value.Default default TypeInfo genStyler() {
            return TypeInfo.of(shared().languagePackage(), shared().classSuffix() + "Styler");
        }

        Optional<TypeInfo> manualStyler();

        default TypeInfo styler() {
            if(classKind().isManual() && manualStyler().isPresent()) {
                return manualStyler().get();
            }
            return genStyler();
        }

        // Styler factory

        @Value.Default default TypeInfo genFactory() {
            return TypeInfo.of(shared().languagePackage(), shared().classSuffix() + "StylerFactory");
        }

        Optional<TypeInfo> manualFactory();

        default TypeInfo factory() {
            if(classKind().isManual() && manualFactory().isPresent()) {
                return manualFactory().get();
            }
            return genFactory();
        }


        /// Adapter project classes

        @Value.Derived default ResourcePath adapterGenDirectory() {
            return shared().adapterProject().genSourceSpoofaxJavaDirectory();
        }

        // Style task definition

        @Value.Default default TypeInfo genStyleTaskDef() {
            return TypeInfo.of(shared().adapterTaskPackage(), shared().classSuffix() + "Style");
        }

        Optional<TypeInfo> manualStyleTaskDef();

        default TypeInfo styleTaskDef() {
            if(classKind().isManual() && manualStyleTaskDef().isPresent()) {
                return manualStyleTaskDef().get();
            }
            return genStyleTaskDef();
        }


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualStyler().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualStyler' has not been set");
            }
            if(!manualFactory().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactory' has not been set");
            }
            if(!manualStyleTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualStyleTaskDef' has not been set");
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
            public AdapterProjectOutput.Builder fromInput(Input input) {
                // Do not add style task definition, as it is handled explicitly.
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        List<GradleConfiguredDependency> dependencies();

        List<TypeInfo> additionalTaskDefs();
    }
}
