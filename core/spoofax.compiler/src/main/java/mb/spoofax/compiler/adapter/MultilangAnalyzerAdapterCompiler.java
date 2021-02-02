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
import org.checkerframework.checker.nullness.qual.Nullable;
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
        analyzeProjectTemplate.write(context, input.baseAnalyzeTaskDef().file(generatedJavaSourcesDirectory), input);
        indexAstTaskDefTemplate.write(context, input.baseIndexAstTaskDef().file(generatedJavaSourcesDirectory), input);
        preStatixTaskDefTemplate.write(context, input.basePreStatixTaskDef().file(generatedJavaSourcesDirectory), input);
        postStatixTaskDefTemplate.write(context, input.basePostStatixTaskDef().file(generatedJavaSourcesDirectory), input);
        checkTaskDefTemplate.write(context, input.baseCheckTaskDef().file(generatedJavaSourcesDirectory), input);
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

        @Value.Default default TypeInfo baseAnalyzeTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "AnalyzeProject");
        }

        Optional<TypeInfo> extendAnalyzeTaskDef();

        default TypeInfo analyzeTaskDef() {
            return extendAnalyzeTaskDef().orElseGet(this::baseAnalyzeTaskDef);
        }

        // Transformation tasks

        @Value.Default default TypeInfo baseIndexAstTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "IndexAst");
        }

        Optional<TypeInfo> extendIndexAstTaskDef();

        default TypeInfo indexAstTaskDef() {
            return extendIndexAstTaskDef().orElseGet(this::baseIndexAstTaskDef);
        }

        @Value.Default default TypeInfo basePreStatixTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "PreStatix");
        }

        Optional<TypeInfo> extendPreStatixTaskDef();

        default TypeInfo preStatixTaskDef() {
            return extendPreStatixTaskDef().orElseGet(this::basePreStatixTaskDef);
        }

        @Value.Default default TypeInfo basePostStatixTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "PostStatix");
        }

        Optional<TypeInfo> extendPostStatixTaskDef();

        default TypeInfo postStatixTaskDef() {
            return extendPostStatixTaskDef().orElseGet(this::basePostStatixTaskDef);
        }

        @Value.Default default TypeInfo baseCheckTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "SmlCheck");
        }

        Optional<TypeInfo> extendCheckTaskDef();

        default TypeInfo checkTaskDef() {
            return extendCheckTaskDef().orElseGet(this::baseCheckTaskDef);
        }

        // Transformation settings

        @Value.Default default String preAnalysisStrategy() { return "pre-analyze"; }

        @Value.Default default String postAnalysisStrategy() { return "post-analyze"; }

        // Identifiers

        @Value.Default default String languageId() { return languageProjectInput().languageId(); }

        @Value.Default default String contextId() { return languageId(); }

        // Statix spec metadata

        @Value.Default default String fileConstraint() { return "statics!fileOk"; }

        @Value.Default default String projectConstraint() { return "statics!projectOk"; }

        default Collection<TypeInfo> libraryTaskDefs() {
            ArrayList<TypeInfo> taskDefs = new ArrayList<>();
            String multilangTaskDefPackage = "mb.statix.multilang.pie";
            taskDefs.add(TypeInfo.of(multilangTaskDefPackage, "SmlSolveProject"));
            taskDefs.add(TypeInfo.of(multilangTaskDefPackage, "SmlInstantiateGlobalScope"));
            taskDefs.add(TypeInfo.of(multilangTaskDefPackage, "SmlPartialSolveFile"));
            taskDefs.add(TypeInfo.of(multilangTaskDefPackage, "SmlPartialSolveProject"));
            String multilangConfigTaskDefPackage = "mb.statix.multilang.pie.config";
            taskDefs.add(TypeInfo.of(multilangConfigTaskDefPackage, "SmlBuildContextConfiguration"));
            taskDefs.add(TypeInfo.of(multilangConfigTaskDefPackage, "SmlReadConfigYaml"));
            String multilangSpecTaskDefPackage = "mb.statix.multilang.pie.spec";
            taskDefs.add(TypeInfo.of(multilangSpecTaskDefPackage, "SmlLoadFragment"));
            taskDefs.add(TypeInfo.of(multilangSpecTaskDefPackage, "SmlBuildSpec"));
            return taskDefs;
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseAnalyzeTaskDef().file(generatedJavaSourcesDirectory),
                baseIndexAstTaskDef().file(generatedJavaSourcesDirectory),
                basePreStatixTaskDef().file(generatedJavaSourcesDirectory),
                basePostStatixTaskDef().file(generatedJavaSourcesDirectory),
                baseCheckTaskDef().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProject adapterProject();

        MultilangAnalyzerLanguageCompiler.Input languageProjectInput();

        StrategoRuntimeAdapterCompiler.Input strategoRuntimeInput();
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends MultilangAnalyzerAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}

