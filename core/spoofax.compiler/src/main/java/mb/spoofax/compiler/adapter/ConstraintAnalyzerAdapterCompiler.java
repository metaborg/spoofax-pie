package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.data.ArgProviderRepr;
import mb.spoofax.compiler.adapter.data.CommandActionRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.MenuItemRepr;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.MenuItemCollection;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.compiler.util.TypeInfoCollection;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.EditorFileType;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Value.Enclosing
public class ConstraintAnalyzerAdapterCompiler implements TaskDef<ConstraintAnalyzerAdapterCompiler.Input, None> {
    private final TemplateWriter analyzeTaskDefTemplate;
    private final TemplateWriter analyzeMultiTaskDefTemplate;
    private final TemplateWriter analyzeFileTaskDefTemplate;
    private final TemplateWriter showAnalyzedAstTaskDef;

    @Inject public ConstraintAnalyzerAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.analyzeTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeTaskDef.java.mustache");
        this.analyzeMultiTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeMultiTaskDef.java.mustache");
        this.analyzeFileTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeFileTaskDef.java.mustache");
        this.showAnalyzedAstTaskDef = templateCompiler.getOrCompileToWriter("constraint_analyzer/ShowAnalyzedAstTaskDef.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        analyzeTaskDefTemplate.write(context, input.baseAnalyzeTaskDef().file(generatedJavaSourcesDirectory), input);
        analyzeMultiTaskDefTemplate.write(context, input.baseAnalyzeMultiTaskDef().file(generatedJavaSourcesDirectory), input);
        analyzeFileTaskDefTemplate.write(context, input.baseAnalyzeFileTaskDef().file(generatedJavaSourcesDirectory), input);
        showAnalyzedAstTaskDef.write(context, input.baseShowAnalyzedAstTaskDef().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override public Serializable key(Input input) {
        return input.adapterProject().project().baseDirectory();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().constraintPieDep()));
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ConstraintAnalyzerAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Analyze

        @Value.Default default TypeInfo baseAnalyzeTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Analyze");
        }

        Optional<TypeInfo> extendAnalyzeTaskDef();

        default TypeInfo analyzeTaskDef() {
            return extendAnalyzeTaskDef().orElseGet(this::baseAnalyzeTaskDef);
        }

        // Multi-file analyze

        @Value.Default default TypeInfo baseAnalyzeMultiTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "AnalyzeMulti");
        }

        Optional<TypeInfo> extendAnalyzeMultiTaskDef();

        default TypeInfo analyzeMultiTaskDef() {
            return extendAnalyzeMultiTaskDef().orElseGet(this::baseAnalyzeMultiTaskDef);
        }

        // Analyze specified file helper

        @Value.Default default TypeInfo baseAnalyzeFileTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "AnalyzeFile");
        }

        Optional<TypeInfo> extendAnalyzeFileTaskDef();

        default TypeInfo analyzeFileTaskDef() {
            return extendAnalyzeFileTaskDef().orElseGet(this::baseAnalyzeFileTaskDef);
        }

        // Show analyzed AST task definition and command

        @Value.Default default TypeInfo baseShowAnalyzedAstTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ShowAnalyzedAst");
        }

        Optional<TypeInfo> extendShowAnalyzedAstTaskDef();

        default TypeInfo showAnalyzedAstTaskDef() {
            return extendShowAnalyzedAstTaskDef().orElseGet(this::baseShowAnalyzedAstTaskDef);
        }

        @Value.Default default CommandDefRepr showAnalyzedAstCommand() {
            return CommandDefRepr.builder()
                .type(adapterProject().commandPackageId(), "ShowAnalyzedAstCommand")
                .taskDefType(showAnalyzedAstTaskDef())
                .displayName("Show analyzed AST")
                .description("Shows the analyzed AST")
                .addParams("rootDirectory", TypeInfo.of(ResourcePath.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)))
                .addParams("file", TypeInfo.of(ResourceKey.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ReadableResource)))
                .build();
        }


        /// Menus

        @Value.Default default MenuItemRepr mainMenu() {
            return editorContextMenu();
        }

        @Value.Default default MenuItemRepr resourceContextMenu() {
            return MenuItemRepr.menu("Debug",
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showAnalyzedAstCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).enclosingProjectRequired().build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showAnalyzedAstCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).enclosingProjectRequired().build())
            );
        }

        @Value.Default default MenuItemRepr editorContextMenu() {
            return MenuItemRepr.menu("Debug",
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showAnalyzedAstCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).enclosingProjectRequired().build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showAnalyzedAstCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).enclosingProjectRequired().build())
            );
        }


        /// Collection methods

        default void collectInto(
            TypeInfoCollection taskDefs,
            Collection<CommandDefRepr> commands,
            MenuItemCollection menuItems
        ) {
            taskDefs.add(analyzeTaskDef(), baseAnalyzeTaskDef());
            taskDefs.add(analyzeMultiTaskDef(), baseAnalyzeMultiTaskDef());
            taskDefs.add(analyzeFileTaskDef(), baseAnalyzeFileTaskDef());
            taskDefs.add(showAnalyzedAstTaskDef(), baseShowAnalyzedAstTaskDef());
            commands.add(showAnalyzedAstCommand());
            menuItems.addMainMenuItem(mainMenu());
            menuItems.addResourceContextMenuItem(resourceContextMenu());
            menuItems.addEditorContextMenuItem(editorContextMenu());
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseAnalyzeTaskDef().file(generatedJavaSourcesDirectory),
                baseAnalyzeMultiTaskDef().file(generatedJavaSourcesDirectory),
                baseAnalyzeFileTaskDef().file(generatedJavaSourcesDirectory),
                baseShowAnalyzedAstTaskDef().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically computed values

        @Value.Derived default boolean isMultiFile() {
            return languageProjectInput().multiFile();
        }

        @Value.Derived default TypeInfo runtimeAnalyzeTaskDef() {
            if(this.isMultiFile()) {
                return analyzeMultiTaskDef();
            } else {
                return analyzeTaskDef();
            }
        }


        /// Automatically provided sub-inputs

        @Value.Auxiliary Shared shared();

        AdapterProject adapterProject();

        ConstraintAnalyzerLanguageCompiler.Input languageProjectInput();

        ParserAdapterCompiler.Input parseInput();

        GetSourceFilesAdapterCompiler.Input getSourceFilesInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();

        StrategoRuntimeAdapterCompiler.Input strategoRuntimeInput();
    }
}
