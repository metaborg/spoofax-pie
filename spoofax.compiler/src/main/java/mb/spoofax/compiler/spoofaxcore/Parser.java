package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class Parser {
    private final TemplateWriter tableTemplate;
    private final TemplateWriter parserTemplate;
    private final TemplateWriter factoryTemplate;
    private final TemplateWriter parseTaskDefTemplate;
    private final TemplateWriter tokenizeTaskDefTemplate;

    public Parser(TemplateCompiler templateCompiler) {
        this.tableTemplate = templateCompiler.getOrCompileToWriter("parser/ParseTable.java.mustache");
        this.parserTemplate = templateCompiler.getOrCompileToWriter("parser/Parser.java.mustache");
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("parser/ParserFactory.java.mustache");
        this.parseTaskDefTemplate = templateCompiler.getOrCompileToWriter("parser/ParseTaskDef.java.mustache");
        this.tokenizeTaskDefTemplate = templateCompiler.getOrCompileToWriter("parser/TokenizeTaskDef.java.mustache");
    }

    // Language project

    public ListView<GradleConfiguredDependency> getLanguageProjectDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().jsglrCommonDep()),
            GradleConfiguredDependency.api(input.shared().jsglr1CommonDep())
        );
    }

    public ListView<String> getLanguageProjectCopyResources(Input input) {
        return ListView.of(input.tableSourceRelPath());
    }

    public void compileLanguageProject(Input input) throws IOException {
        if(input.classKind().isManualOnly()) return; // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.languageClassesGenDirectory();
        tableTemplate.write(input, input.genTable().file(classesGenDirectory));
        parserTemplate.write(input, input.genParser().file(classesGenDirectory));
        factoryTemplate.write(input, input.genFactory().file(classesGenDirectory));
    }

    // Adapter project

    public void compileAdapterProject(Input input) throws IOException {
        if(input.classKind().isManualOnly()) return; // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.adapterClassesGenDirectory();
        parseTaskDefTemplate.write(input, input.genParseTaskDef().file(classesGenDirectory));
        tokenizeTaskDefTemplate.write(input, input.genTokenizeTaskDef().file(classesGenDirectory));
    }

    // Input

    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends ParserData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        /// Configuration

        String startSymbol();


        /// Parse table source file (to copy from), and destination file

        @Value.Default default String tableSourceRelPath() {
            return "target/metaborg/sdf.tbl";
        }

        @Value.Default default String tableTargetRelPath() {
            return shared().languageProject().packagePath() + "/" + tableSourceRelPath();
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        default ResourcePath languageClassesGenDirectory() {
            return shared().languageProject().genSourceSpoofaxJavaDirectory();
        }

        // Parse table

        @Value.Default default TypeInfo genTable() {
            return TypeInfo.of(shared().languagePackage(), shared().classPrefix() + "ParseTable");
        }

        // Parser

        @Value.Default default TypeInfo genParser() {
            return TypeInfo.of(shared().languagePackage(), shared().classPrefix() + "Parser");
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
            return TypeInfo.of(shared().languagePackage(), shared().classPrefix() + "ParserFactory");
        }

        Optional<TypeInfo> manualFactory();

        default TypeInfo factory() {
            if(classKind().isManual() && manualFactory().isPresent()) {
                return manualFactory().get();
            }
            return genFactory();
        }


        /// Adapter project classes

        default ResourcePath adapterClassesGenDirectory() {
            return shared().adapterProject().genSourceSpoofaxJavaDirectory();
        }

        // Parse task definition

        @Value.Default default TypeInfo genParseTaskDef() {
            return TypeInfo.of(shared().adapterTaskPackage(), shared().classPrefix() + "Parse");
        }

        Optional<TypeInfo> manualParseTaskDef();

        default TypeInfo parseTaskDef() {
            if(classKind().isManual() && manualParseTaskDef().isPresent()) {
                return manualParseTaskDef().get();
            }
            return genParseTaskDef();
        }

        // Tokenize task definition

        @Value.Default default TypeInfo genTokenizeTaskDef() {
            return TypeInfo.of(shared().adapterTaskPackage(), shared().classPrefix() + "Tokenize");
        }

        Optional<TypeInfo> manualTokenizeTaskDef();

        default TypeInfo tokenizeTaskDef() {
            if(classKind().isManual() && manualTokenizeTaskDef().isPresent()) {
                return manualTokenizeTaskDef().get();
            }
            return genTokenizeTaskDef();
        }


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
            if(!manualParseTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParseTaskDef' has not been set");
            }
            if(!manualTokenizeTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualTokenizeTaskDef' has not been set");
            }
        }
    }
}
