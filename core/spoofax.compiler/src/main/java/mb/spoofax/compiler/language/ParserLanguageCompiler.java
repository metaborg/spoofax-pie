package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
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
public class ParserLanguageCompiler {
    private final TemplateWriter tableTemplate;
    private final TemplateWriter parserTemplate;
    private final TemplateWriter factoryTemplate;
    private final TemplateWriter completionFactoryTemplate;

    @Inject public ParserLanguageCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.tableTemplate = templateCompiler.getOrCompileToWriter("parser/ParseTable.java.mustache");
        this.parserTemplate = templateCompiler.getOrCompileToWriter("parser/Parser.java.mustache");
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("parser/ParserFactory.java.mustache");
        this.completionFactoryTemplate = templateCompiler.getOrCompileToWriter("parser/CompletionParserFactory.java.mustache");
    }


    public None compile(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        tableTemplate.write(context, input.baseParseTable().file(generatedJavaSourcesDirectory), input);
        parserTemplate.write(context, input.baseParser().file(generatedJavaSourcesDirectory), input);
        factoryTemplate.write(context, input.baseParserFactory().file(generatedJavaSourcesDirectory), input);
        completionFactoryTemplate.write(context, input.baseCompletionParserFactory().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().atermCommonDep()),
            GradleConfiguredDependency.api(input.shared().jsglrCommonDep()),
            GradleConfiguredDependency.api(input.variant().caseOf()
                .jsglr1(() -> input.shared().jsglr1CommonDep())
                .jsglr2(preset -> input.shared().jsglr2CommonDep())
            )
        );
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends ParserLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Configuration

        String startSymbol();

        /**
         * @return path to the parse table aterm file to load, relative to the classloader resources.
         */
        String parseTableAtermFileRelativePath();

        /**
         * @return path to the parse table persisted file to load, relative to the classloader resources.
         */
        String parseTablePersistedFileRelativePath();

        /**
         * @return path to the completion parse table aterm file to load, relative to the classloader resources.
         */
        String completionParseTableAtermFileRelativePath();

        /**
         * @return path to the completion parse table persisted file to load, relative to the classloader resources.
         */
        String completionParseTablePersistedFileRelativePath();

        @Value.Default default ParserVariant variant() { return ParserVariant.jsglr1(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // Parse table

        @Value.Default default TypeInfo baseParseTable() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ParseTable");
        }

        Optional<TypeInfo> extendParseTable();

        default TypeInfo parseTable() {
            return extendParseTable().orElseGet(this::baseParseTable);
        }

        // Parser

        @Value.Default default TypeInfo baseParser() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "Parser");
        }

        Optional<TypeInfo> extendParser();

        default TypeInfo parser() {
            return extendParser().orElseGet(this::baseParser);
        }

        // Parser factory

        @Value.Default default TypeInfo baseParserFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ParserFactory");
        }

        Optional<TypeInfo> extendParserFactory();

        default TypeInfo parserFactory() {
            return extendParserFactory().orElseGet(this::baseParserFactory);
        }

        // Completion Parser

        @Value.Default default TypeInfo baseCompletionParser() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "Parser");
        }

        Optional<TypeInfo> extendCompletionParser();

        default TypeInfo completionParser() {
            return extendCompletionParser().orElseGet(this::baseCompletionParser);
        }

        // Completion Parser factory

        @Value.Default default TypeInfo baseCompletionParserFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "CompletionParserFactory");
        }

        Optional<TypeInfo> extendCompletionParserFactory();

        default TypeInfo completionParserFactory() {
            return extendCompletionParserFactory().orElseGet(this::baseCompletionParserFactory);
        }


        /// Mustache template helpers

        default boolean isJsglr2() {
            return variant().isJsglr2();
        }

        default TypeInfo parseTableType() {
            return variant().caseOf()
                .jsglr1(() -> TypeInfo.of("mb.jsglr1.common.JSGLR1ParseTable"))
                .jsglr2(preset -> TypeInfo.of("mb.jsglr2.common.Jsglr2ParseTable"));
        }

        default TypeInfo parseTableExceptionType() {
            return variant().caseOf()
                .jsglr1(() -> TypeInfo.of("mb.jsglr1.common.JSGLR1ParseTableException"))
                .jsglr2(preset -> TypeInfo.of("mb.jsglr2.common.Jsglr2ParseTableException"));
        }

        default TypeInfo parserType() {
            return variant().caseOf()
                .jsglr1(() -> TypeInfo.of("mb.jsglr1.common.JSGLR1Parser"))
                .jsglr2(preset -> TypeInfo.of("mb.jsglr2.common.Jsglr2Parser"));
        }

        default Optional<String> parserConstructorAdditionalArguments() {
            return variant().caseOf()
                .jsglr2(preset -> ", org.spoofax.jsglr2.JSGLR2Variant.Preset." + preset.toJsglr2PresetString())
                .otherwiseEmpty();
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseParseTable().file(generatedJavaSourcesDirectory),
                baseParser().file(generatedJavaSourcesDirectory),
                baseParserFactory().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {

        }
    }
}
