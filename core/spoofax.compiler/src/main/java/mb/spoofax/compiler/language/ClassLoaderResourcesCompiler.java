package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class ClassLoaderResourcesCompiler implements TaskDef<ClassLoaderResourcesCompiler.Input, ClassLoaderResourcesCompiler.Output> {
    private final TemplateWriter classloaderResourcesTemplate;

    @Inject public ClassLoaderResourcesCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.classloaderResourcesTemplate = templateCompiler.getOrCompileToWriter("classloader_resources/ClassLoaderResources.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        classloaderResourcesTemplate.write(context, input.baseClassLoaderResources().file(generatedJavaSourcesDirectory), input);
        return outputBuilder.build();
    }

    @Override public Serializable key(Input input) {
        return input.languageProject().project().baseDirectory();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ClassLoaderResourcesCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Language project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // Classloader resources

        @Value.Default default TypeInfo baseClassLoaderResources() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ClassLoaderResources");
        }

        Optional<TypeInfo> extendClassLoaderResources();

        default TypeInfo classLoaderResources() {
            return extendClassLoaderResources().orElseGet(this::baseClassLoaderResources);
        }

        @Value.Default default String qualifier() {
            return languageProject().packageId().replace(".", "-") + "-classloader-resource";
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseClassLoaderResources().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {

        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends ClassLoaderResourcesCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
