package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class CompleterLanguageCompiler implements TaskDef<CompleterLanguageCompiler.Input, CompleterLanguageCompiler.Output> {
    @Inject public CompleterLanguageCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        return outputBuilder.build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().completionsCommonDep())
        );
    }

    public ListView<String> getCopyResources(Input input) {
        return ListView.of();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends CompleterLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // Completer

        @Value.Default default TypeInfo baseCompleter() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "Completer");
        }

        Optional<TypeInfo> extendCompleter();

        default TypeInfo completer() {
            return extendCompleter().orElseGet(this::baseCompleter);
        }


        /// List of all provided files

        default ListView<ResourcePath> providedFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            return ListView.of(
                baseCompleter().file(generatedJavaSourcesDirectory())
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {

        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends CompleterLanguageCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
