package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
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
public class StylerAdapterCompiler {
    private final TemplateWriter styleTaskDefTemplate;

    @Inject public StylerAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.styleTaskDefTemplate = templateCompiler.getOrCompileToWriter("styler/StyleTaskDef.java.mustache");
    }


    public None compile(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        styleTaskDefTemplate.write(context, input.baseStyleTaskDef().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends StylerAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Adapter project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Style task definition

        @Value.Default default TypeInfo baseStyleTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Style");
        }

        Optional<TypeInfo> extendStyleTaskDef();

        default TypeInfo styleTaskDef() {
            return extendStyleTaskDef().orElseGet(this::baseStyleTaskDef);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseStyleTaskDef().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs

        @Value.Auxiliary Shared shared();

        AdapterProject adapterProject();

        StylerLanguageCompiler.Input languageProjectInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();
    }
}
