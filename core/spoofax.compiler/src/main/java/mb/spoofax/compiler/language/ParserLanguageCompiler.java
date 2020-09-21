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
public class ParserLanguageCompiler implements TaskDef<ParserLanguageCompiler.Input, None> {
    private final TemplateWriter tableTemplate;
    private final TemplateWriter parserTemplate;
    private final TemplateWriter factoryTemplate;

    @Inject public ParserLanguageCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.tableTemplate = templateCompiler.getOrCompileToWriter("parser/ParseTable.java.mustache");
        this.parserTemplate = templateCompiler.getOrCompileToWriter("parser/Parser.java.mustache");
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("parser/ParserFactory.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManualOnly()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        tableTemplate.write(context, input.genTable().file(generatedJavaSourcesDirectory), input);
        parserTemplate.write(context, input.genParser().file(generatedJavaSourcesDirectory), input);
        factoryTemplate.write(context, input.genFactory().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().jsglrCommonDep()),
            GradleConfiguredDependency.api(input.shared().jsglr1CommonDep())
        );
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends ParserLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Configuration

        String startSymbol();


        /**
         * @return path to the parse table to load, relative to the classloader resources.
         */
        String parseTableRelativePath();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // Parse table

        @Value.Default default TypeInfo genTable() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ParseTable");
        }

        // Parser

        @Value.Default default TypeInfo genParser() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "Parser");
        }

        Optional<TypeInfo> manualParser();

        default TypeInfo parser() {
            if(classKind().isManual() && manualParser().isPresent()) {
                return manualParser().get();
            }
            return genParser();
        }

        // Parser factory

        @Value.Default default TypeInfo genFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ParserFactory");
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
                genTable().file(generatedJavaSourcesDirectory()),
                genParser().file(generatedJavaSourcesDirectory()),
                genFactory().file(generatedJavaSourcesDirectory())
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualParser().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParser' has not been set");
            }
            if(!manualFactory().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactory' has not been set");
            }
        }
    }
}
