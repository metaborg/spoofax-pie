package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class ParserAdapterCompiler implements TaskDef<ParserAdapterCompiler.Input, ParserAdapterCompiler.Output> {
    private final TemplateWriter parseTaskDefTemplate;
    private final TemplateWriter tokenizeTaskDefTemplate;

    @Inject public ParserAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.parseTaskDefTemplate = templateCompiler.getOrCompileToWriter("parser/ParseTaskDef.java.mustache");
        this.tokenizeTaskDefTemplate = templateCompiler.getOrCompileToWriter("parser/TokenizeTaskDef.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        parseTaskDefTemplate.write(context, input.baseParseTaskDef().file(generatedJavaSourcesDirectory), input);
        tokenizeTaskDefTemplate.write(context, input.baseTokenizeTaskDef().file(generatedJavaSourcesDirectory), input);
        return outputBuilder.build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().jsglr1PieDep())
        );
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ParserAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Parse task definition

        @Value.Default default TypeInfo baseParseTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Parse");
        }

        Optional<TypeInfo> extendParseTaskDef();

        default TypeInfo parseTaskDef() {
            return extendParseTaskDef().orElseGet(this::baseParseTaskDef);
        }

        // Tokenize task definition

        @Value.Default default TypeInfo baseTokenizeTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Tokenize");
        }

        Optional<TypeInfo> extendTokenizeTaskDef();

        default TypeInfo tokenizeTaskDef() {
            return extendTokenizeTaskDef().orElseGet(this::baseTokenizeTaskDef);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseParseTaskDef().file(generatedJavaSourcesDirectory),
                baseTokenizeTaskDef().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        AdapterProject adapterProject();

        ParserLanguageCompiler.Input languageProjectInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();


        @Value.Check default void check() {

        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends ParserAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
