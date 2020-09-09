package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class MultilangAnalyzerLanguageCompiler implements TaskDef<MultilangAnalyzerLanguageCompiler.Input, MultilangAnalyzerLanguageCompiler.Output> {
    private final TemplateWriter specConfigFactoryTemplate;

    @Inject public MultilangAnalyzerLanguageCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.specConfigFactoryTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/SpecConfigFactory.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        specConfigFactoryTemplate.write(context, input.genSpecConfigFactory().file(classesGenDirectory), input);
        return outputBuilder.build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().multilangDep()));
    }

    public ListView<String> getCopyResources(Input input) {
        return ListView.of("src-gen/statix/");
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends MultilangAnalyzerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }

        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }

        @Value.Derived default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Spec factory

        @Value.Default default TypeInfo genSpecConfigFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "SpecConfigFactory");
        }

        Optional<TypeInfo> manualSpecConfigFactory();

        default TypeInfo specConfigFactory() {
            if(classKind().isManual() && manualSpecConfigFactory().isPresent()) {
                return manualSpecConfigFactory().get();
            }
            return genSpecConfigFactory();
        }

        @Value.Default default String languageId() { return shared().defaultPackageId(); }

        @Value.Default default List<TypeInfo> dependencyFactories() { return new ArrayList<>(); }

        List<String> rootModules();

        // List of all provided files

        default ListView<ResourcePath> providedFiles() { return ListView.of(); }

        // Subinputs

        TypeInfo classloaderResources();

        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends MultilangAnalyzerLanguageCompilerData.Output.Builder {}

        static Builder builder() { return new Output.Builder(); }
    }
}

