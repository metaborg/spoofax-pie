package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class ClassloaderResourcesCompiler implements TaskDef<ClassloaderResourcesCompiler.Input, ClassloaderResourcesCompiler.Output> {
    private final TemplateWriter classloaderResourcesTemplate;

    @Inject public ClassloaderResourcesCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.classloaderResourcesTemplate = templateCompiler.getOrCompileToWriter("classloader_resources/ClassloaderResources.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        classloaderResourcesTemplate.write(context, input.genClassloaderResources().file(classesGenDirectory), input);
        return outputBuilder.build();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ClassloaderResourcesCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Language project classes

        default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Classloader resources

        @Value.Default default TypeInfo genClassloaderResources() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ClassloaderResources");
        }

        Optional<TypeInfo> manualClassloaderResources();

        default TypeInfo classloaderResources() {
            if(classKind().isManual() && manualClassloaderResources().isPresent()) {
                return manualClassloaderResources().get();
            }
            return genClassloaderResources();
        }

        @Value.Default default String qualifier() {
            return languageProject().packageId().replace(".", "-") + "-classloader-resource";
        }

        /// List of all provided files

        default ListView<ResourcePath> providedFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genClassloaderResources().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualClassloaderResources().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualClassloaderResources' has not been set");
            }
        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends ClassloaderResourcesCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
