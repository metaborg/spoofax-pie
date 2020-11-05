package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
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
import java.util.Optional;

@Value.Enclosing
public class StylerLanguageCompiler implements TaskDef<StylerLanguageCompiler.Input, None> {
    private final TemplateWriter rulesTemplate;
    private final TemplateWriter stylerTemplate;
    private final TemplateWriter factoryTemplate;

    @Inject public StylerLanguageCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.rulesTemplate = templateCompiler.getOrCompileToWriter("styler/StylingRules.java.mustache");
        this.stylerTemplate = templateCompiler.getOrCompileToWriter("styler/Styler.java.mustache");
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("styler/StylerFactory.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        rulesTemplate.write(context, input.genRules().file(generatedJavaSourcesDirectory), input);
        stylerTemplate.write(context, input.genStyler().file(generatedJavaSourcesDirectory), input);
        factoryTemplate.write(context, input.genStylerFactory().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().esvCommonDep()));
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends StylerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /**
         * @return path to the packed ESV to load, relative to the classloader resources.
         */
        String packedEsvRelativePath();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // Styling rules

        @Value.Default default TypeInfo genRules() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "StylingRules");
        }

        // Styler

        @Value.Default default TypeInfo genStyler() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "Styler");
        }

        Optional<TypeInfo> extendedStyler();

        default TypeInfo styler() {
            return extendedStyler().orElseGet(this::genStyler);
        }

        // Styler factory

        @Value.Default default TypeInfo genStylerFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "StylerFactory");
        }

        Optional<TypeInfo> extendedStylerFactory();

        default TypeInfo stylerFactory() {
            return extendedStylerFactory().orElseGet(this::genStylerFactory);
        }


        /// List of all provided files

        default ListView<ResourcePath> providedFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            return ListView.of(
                genRules().file(generatedJavaSourcesDirectory()),
                genStyler().file(generatedJavaSourcesDirectory()),
                genStylerFactory().file(generatedJavaSourcesDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {

        }
    }
}
