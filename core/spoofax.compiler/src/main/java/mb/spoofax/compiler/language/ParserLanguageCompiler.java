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
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        tableTemplate.write(context, input.baseParseTable().file(generatedJavaSourcesDirectory), input);
        parserTemplate.write(context, input.baseParser().file(generatedJavaSourcesDirectory), input);
        factoryTemplate.write(context, input.baseParserFactory().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }

    @Override public Serializable key(Input input) {
        return input.languageProject().project().baseDirectory();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().jsglrCommonDep()),
            GradleConfiguredDependency.api(input.jsglrVersion() == JsglrVersion.V1 ? input.shared().jsglr1CommonDep() : input.shared().jsglr2CommonDep())
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

        @Value.Default default JsglrVersion jsglrVersion() { return JsglrVersion.V1; }


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


        /// Mustache template helpers

        default TypeInfo parseTableType() {
            switch(jsglrVersion()) {
                default:
                case V1:
                    return TypeInfo.of("mb.jsglr1.common.JSGLR1ParseTable");
                case V2:
                    return TypeInfo.of("mb.jsglr2.common.Jsglr2ParseTable");
            }
        }

        default TypeInfo parseTableExceptionType() {
            switch(jsglrVersion()) {
                default:
                case V1:
                    return TypeInfo.of("mb.jsglr1.common.JSGLR1ParseTableException");
                case V2:
                    return TypeInfo.of("mb.jsglr2.common.Jsglr2ParseTableException");
            }
        }

        default TypeInfo parserType() {
            switch(jsglrVersion()) {
                default:
                case V1:
                    return TypeInfo.of("mb.jsglr1.common.JSGLR1Parser");
                case V2:
                    return TypeInfo.of("mb.jsglr2.common.Jsglr2Parser");
            }
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

    public enum JsglrVersion implements Serializable {
        V1, V2
    }
}
