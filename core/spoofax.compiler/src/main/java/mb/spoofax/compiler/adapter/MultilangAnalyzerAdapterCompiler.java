package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.language.MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Value.Enclosing
public class MultilangAnalyzerAdapterCompiler implements TaskDef<MultilangAnalyzerAdapterCompiler.Input, MultilangAnalyzerAdapterCompiler.Output> {
    private final TemplateWriter analyzeProjectTemplate;
    private final TemplateWriter indexAstTaskDefTemplate;
    private final TemplateWriter preStatixTaskDefTemplate;
    private final TemplateWriter postStatixTaskDefTemplate;
    private final TemplateWriter checkTaskDefTemplate;

    @Inject public MultilangAnalyzerAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.analyzeProjectTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/AnalyzeProjectTaskDef.java.mustache");
        this.indexAstTaskDefTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/IndexAstTaskDef.java.mustache");
        this.preStatixTaskDefTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/PreStatixTaskDef.java.mustache");
        this.postStatixTaskDefTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/PostStatixTaskDef.java.mustache");
        this.checkTaskDefTemplate = templateCompiler.getOrCompileToWriter("multilang_analyzer/SmlCheckTaskDef.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        analyzeProjectTemplate.write(context, input.genAnalyzeTaskDef().file(generatedJavaSourcesDirectory), input);
        indexAstTaskDefTemplate.write(context, input.genIndexAstTaskDef().file(generatedJavaSourcesDirectory), input);
        preStatixTaskDefTemplate.write(context, input.genPreStatixTaskDef().file(generatedJavaSourcesDirectory), input);
        postStatixTaskDefTemplate.write(context, input.genPostStatixTaskDef().file(generatedJavaSourcesDirectory), input);
        checkTaskDefTemplate.write(context, input.genCheckTaskDef().file(generatedJavaSourcesDirectory), input);
        return outputBuilder.build();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends MultilangAnalyzerAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Analyze

        @Value.Default default TypeInfo genAnalyzeTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "AnalyzeProject");
        }

        Optional<TypeInfo> extendedAnalyzeTaskDef();

        default TypeInfo analyzeTaskDef() {
            return extendedAnalyzeTaskDef().orElseGet(this::genAnalyzeTaskDef);
        }

        // Transformation tasks

        @Value.Default default TypeInfo genIndexAstTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "IndexAst");
        }

        Optional<TypeInfo> extendedIndexAstTaskDef();

        default TypeInfo indexAstTaskDef() {
            return extendedIndexAstTaskDef().orElseGet(this::genIndexAstTaskDef);
        }

        @Value.Default default TypeInfo genPreStatixTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "PreStatix");
        }

        Optional<TypeInfo> extendedPreStatixTaskDef();

        default TypeInfo preStatixTaskDef() {
            return extendedPreStatixTaskDef().orElseGet(this::genPreStatixTaskDef);
        }

        @Value.Default default TypeInfo genPostStatixTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "PostStatix");
        }

        Optional<TypeInfo> extendedPostStatixTaskDef();

        default TypeInfo postStatixTaskDef() {
            return extendedPostStatixTaskDef().orElseGet(this::genPostStatixTaskDef);
        }

        @Value.Default default TypeInfo genCheckTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "SmlCheck");
        }

        Optional<TypeInfo> extendedCheckTaskDef();

        default TypeInfo checkTaskDef() {
            return extendedCheckTaskDef().orElseGet(this::genCheckTaskDef);
        }

        // Transformation settings

        @Value.Default default String preAnalysisStrategy() { return "pre"; }

        @Value.Default default String postAnalysisStrategy() { return "post"; }

        // Identifiers

        @Value.Default default String languageId() { return languageProjectInput().languageId(); }

        @Value.Default default String contextId() { return shared().defaultPackageId(); }

        // Statix spec metadata

        String rootModule();

        @Value.Default default String fileConstraint() { return "fileOk"; }

        @Value.Default default String projectConstraint() { return "projectOk"; }

        default Collection<TypeInfo> libraryTaskDefs() {
            ArrayList<TypeInfo> taskDefs = new ArrayList<>();
            String multilangTaskDefPackage = "mb.statix.multilang.pie";
            taskDefs.add(TypeInfo.of(multilangTaskDefPackage, "SmlSolveProject"));
            taskDefs.add(TypeInfo.of(multilangTaskDefPackage, "SmlBuildSpec"));
            taskDefs.add(TypeInfo.of(multilangTaskDefPackage, "SmlInstantiateGlobalScope"));
            taskDefs.add(TypeInfo.of(multilangTaskDefPackage, "SmlPartialSolveFile"));
            taskDefs.add(TypeInfo.of(multilangTaskDefPackage, "SmlPartialSolveProject"));
            String multilangConfigTaskDefPackage = "mb.statix.multilang.pie.config";
            taskDefs.add(TypeInfo.of(multilangConfigTaskDefPackage, "SmlBuildContextConfiguration"));
            taskDefs.add(TypeInfo.of(multilangConfigTaskDefPackage, "SmlReadConfigYaml"));
            return taskDefs;
        }


        // List of all generated files

        default ListView<ResourcePath> generatedFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            return ListView.of(
                genAnalyzeTaskDef().file(generatedJavaSourcesDirectory()),
                genIndexAstTaskDef().file(generatedJavaSourcesDirectory()),
                genPreStatixTaskDef().file(generatedJavaSourcesDirectory()),
                genPostStatixTaskDef().file(generatedJavaSourcesDirectory()),
                genCheckTaskDef().file(generatedJavaSourcesDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProject adapterProject();

        MultilangAnalyzerLanguageCompiler.Input languageProjectInput();
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends MultilangAnalyzerAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}

