package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.language.CodeCompletionLanguageCompiler;
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
import java.util.Set;

/**
 * Compiles the code completion task.
 */
@Value.Enclosing
public class CodeCompletionAdapterCompiler implements TaskDef<CodeCompletionAdapterCompiler.Input, None> {
    private final TemplateWriter codeCompletionTaskDefTemplate;
    private final TemplateWriter statixSpecTaskDefTemplate;

    private final TemplateWriter upgradePlaceholdersStatixTaskDefTemplate;
    private final TemplateWriter downgradePlaceholdersStatixTaskDefTemplate;
    private final TemplateWriter isInjectionStatixTaskDefTemplate;

    /**
     * Initializes a new instance of the {@link CodeCompletionAdapterCompiler} class.
     *
     * @param templateCompiler the template compiler to use
     */
    @Inject public CodeCompletionAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.codeCompletionTaskDefTemplate = templateCompiler.getOrCompileToWriter("code_completion/CodeCompletionTaskDef.java.mustache");
        this.statixSpecTaskDefTemplate = templateCompiler.getOrCompileToWriter("code_completion/StatixSpecTaskDef.java.mustache");

        this.upgradePlaceholdersStatixTaskDefTemplate = templateCompiler.getOrCompileToWriter("code_completion/UpgradePlaceholdersStatixTaskDef.java.mustache");
        this.downgradePlaceholdersStatixTaskDefTemplate = templateCompiler.getOrCompileToWriter("code_completion/DowngradePlaceholdersStatixTaskDef.java.mustache");
        this.isInjectionStatixTaskDefTemplate = templateCompiler.getOrCompileToWriter("code_completion/IsInjectionStatixTaskDef.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        codeCompletionTaskDefTemplate.write(context, input.baseCodeCompletionTaskDef().file(generatedJavaSourcesDirectory), input);
        statixSpecTaskDefTemplate.write(context, input.baseStatixSpecTaskDef().file(generatedJavaSourcesDirectory), input);

        upgradePlaceholdersStatixTaskDefTemplate.write(context, input.baseUpgradePlaceholdersStatixTaskDef().file(generatedJavaSourcesDirectory), input);
        downgradePlaceholdersStatixTaskDefTemplate.write(context, input.baseDowngradePlaceholdersStatixTaskDef().file(generatedJavaSourcesDirectory), input);
        isInjectionStatixTaskDefTemplate.write(context, input.baseIsInjectionStatixTaskDef().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override public Serializable key(Input input) {
        return input.adapterProject().project().baseDirectory();
    }


    public ListView<GradleConfiguredDependency> getDependencies(CodeCompletionAdapterCompiler.Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().atermCommonDep()),
            GradleConfiguredDependency.api(input.shared().jsglrPieDep()),
            GradleConfiguredDependency.api(input.shared().constraintPieDep()),
            GradleConfiguredDependency.api(input.shared().statixPieDep()),
            GradleConfiguredDependency.api(input.shared().statixCodeCompletionPieDep())
        );
    }

    @Value.Immutable public interface Input extends Serializable {
        class Builder extends CodeCompletionAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Code completion task definition

        @Value.Default default TypeInfo baseCodeCompletionTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "CodeCompletionTaskDef");
        }

        Optional<TypeInfo> extendCompleteTaskDef();

        default TypeInfo codeCompletionTaskDef() {
            return extendCompleteTaskDef().orElseGet(this::baseCodeCompletionTaskDef);
        }

        // Statix Spec task definition

        @Value.Default default TypeInfo baseStatixSpecTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "StatixSpecTaskDef");
        }

        Optional<TypeInfo> extendStatixSpecTaskDef();

        default TypeInfo statixSpecTaskDef() {
            return extendStatixSpecTaskDef().orElseGet(this::baseStatixSpecTaskDef);
        }

        // Transformation tasks

        @Value.Default default TypeInfo baseUpgradePlaceholdersStatixTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "UpgradePlaceholdersStatix");
        }

        Optional<TypeInfo> extendUpgradePlaceholdersStatixTaskDef();

        default TypeInfo upgradePlaceholdersStatixTaskDef() {
            return extendUpgradePlaceholdersStatixTaskDef().orElseGet(this::baseUpgradePlaceholdersStatixTaskDef);
        }

        @Value.Default default TypeInfo baseDowngradePlaceholdersStatixTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "DowngradePlaceholdersStatix");
        }

        Optional<TypeInfo> extendDowngradePlaceholdersStatixTaskDef();

        default TypeInfo downgradePlaceholdersStatixTaskDef() {
            return extendDowngradePlaceholdersStatixTaskDef().orElseGet(this::baseDowngradePlaceholdersStatixTaskDef);
        }

        @Value.Default default TypeInfo baseIsInjectionStatixTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "IsInjectionStatix");
        }

        Optional<TypeInfo> extendIsInjectionStatixTaskDef();

        default TypeInfo isInjectionStatixTaskDef() {
            return extendIsInjectionStatixTaskDef().orElseGet(this::baseIsInjectionStatixTaskDef);
        }

        // Transformation settings

        @Value.Default default String upgradePlaceholdersStrategy() { return "upgrade-placeholders"; } // upgrade-placeholders-Tiger

        @Value.Default default String downgradePlaceholdersStrategy() { return "downgrade-placeholders"; } // downgrade-placeholders-Tiger

        @Value.Default default String isInjectionStrategy() { return "is-inj"; } // is-Tiger-inj-cons

        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseCodeCompletionTaskDef().file(generatedJavaSourcesDirectory),
                baseStatixSpecTaskDef().file(generatedJavaSourcesDirectory),
                baseUpgradePlaceholdersStatixTaskDef().file(generatedJavaSourcesDirectory),
                baseDowngradePlaceholdersStatixTaskDef().file(generatedJavaSourcesDirectory),
                baseIsInjectionStatixTaskDef().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        AdapterProject adapterProject();

        CodeCompletionLanguageCompiler.Input languageProjectInput();

        ParserAdapterCompiler.Input parserInput();

        ConstraintAnalyzerAdapterCompiler.Input constraintAnalyzerInput();

        StrategoRuntimeAdapterCompiler.Input strategoRuntimeInput();

        TegoRuntimeAdapterCompiler.Input tegoRuntimeInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();

        @Value.Check default void check() {

        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends CodeCompletionAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
