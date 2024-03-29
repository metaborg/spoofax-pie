package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.Shared;
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
public class MultilangAnalyzerLanguageCompiler {
    private final TemplateWriter specConfigFactoryTemplate;

    @Inject public MultilangAnalyzerLanguageCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.specConfigFactoryTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/SpecConfigFactory.java.mustache");
    }


    public Output compile(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        specConfigFactoryTemplate.write(context, input.baseSpecConfigFactory().file(generatedJavaSourcesDirectory), input);
        return outputBuilder.build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().multilangDep()));
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends MultilangAnalyzerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }


        // Spec factory

        @Value.Default default TypeInfo baseSpecConfigFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "SpecConfigFactory");
        }

        Optional<TypeInfo> extendSpecConfigFactory();

        default TypeInfo specConfigFactory() {
            return extendSpecConfigFactory().orElseGet(this::baseSpecConfigFactory);
        }

        @Value.Default default String languageId() { return shared().defaultPackageId(); }

        @Value.Default default List<TypeInfo> dependencyFactories() { return new ArrayList<>(); }

        List<String> rootModules();


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseSpecConfigFactory().file(generatedJavaSourcesDirectory)
            );
        }


        // Subinputs

        TypeInfo classLoaderResources();


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends MultilangAnalyzerLanguageCompilerData.Output.Builder {}

        static Builder builder() { return new Output.Builder(); }
    }
}

