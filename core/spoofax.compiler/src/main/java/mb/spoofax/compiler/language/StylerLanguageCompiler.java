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
import java.util.Optional;

@Value.Enclosing
public class StylerLanguageCompiler implements TaskDef<StylerLanguageCompiler.Input, StylerLanguageCompiler.Output> {
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

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        rulesTemplate.write(context, input.genRules().file(classesGenDirectory), input);
        stylerTemplate.write(context, input.genStyler().file(classesGenDirectory), input);
        factoryTemplate.write(context, input.genFactory().file(classesGenDirectory), input);
        return outputBuilder.build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().esvCommonDep()));
    }

    public ListView<String> getCopyResources(Input input) {
        return ListView.of(input.packedESVRelPath());
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends StylerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Packed ESV source file (to copy from), and destination file

        @Value.Default default String packedESVRelPath() {
            return "target/metaborg/editor.esv.af";
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Styling rules

        @Value.Default default TypeInfo genRules() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "StylingRules");
        }

        // Styler

        @Value.Default default TypeInfo genStyler() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "Styler");
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
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "StylerFactory");
        }

        Optional<TypeInfo> manualFactory();

        default TypeInfo factory() {
            if(classKind().isManual() && manualFactory().isPresent()) {
                return manualFactory().get();
            }
            return genFactory();
        }


        /// List of all provided files

        default ListView<ResourcePath> providedFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genRules().file(classesGenDirectory()),
                genStyler().file(classesGenDirectory()),
                genFactory().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();


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
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends StylerLanguageCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
