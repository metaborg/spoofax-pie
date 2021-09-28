package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
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
import java.util.Set;

@Value.Enclosing
public class CodeCompletionLanguageCompiler implements TaskDef<CodeCompletionLanguageCompiler.Input, None> {
    @Inject public CodeCompletionLanguageCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        // No templates specified (yet)
        return None.instance;
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override public Serializable key(Input input) {
        return input.languageProject().project().baseDirectory();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().statixCodeCompletionDep())
        );
    }

    public ListView<String> getCopyResources(Input input) {
        return ListView.of();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends CodeCompletionLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // Completer

        @Value.Default default TypeInfo baseCodeCompletion() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "CodeCompletion");
        }

        Optional<TypeInfo> extendCodeCompletion();

        default TypeInfo codeCompletion() {
            return extendCodeCompletion().orElseGet(this::baseCodeCompletion);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseCodeCompletion().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {

        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends CodeCompletionLanguageCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
