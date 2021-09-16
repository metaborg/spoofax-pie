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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Value.Enclosing
public class ConstraintAnalyzerAdapterCompiler implements TaskDef<ConstraintAnalyzerAdapterCompiler.Input, None> {
    private final TemplateWriter analyzeTaskDefTemplate;
    private final TemplateWriter analyzeMultiTaskDefTemplate;
    private final TemplateWriter analyzeFileTaskDefTemplate;
    private final TemplateWriter showPreAnalyzeAstTaskDef;
    private final TemplateWriter showAnalyzedAstTaskDef;
    private final TemplateWriter showScopeGraphTaskDef;

    @Inject public ConstraintAnalyzerAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.analyzeTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeTaskDef.java.mustache");
        this.analyzeMultiTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeMultiTaskDef.java.mustache");
        this.analyzeFileTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeFileTaskDef.java.mustache");
        this.showPreAnalyzeAstTaskDef = templateCompiler.getOrCompileToWriter("constraint_analyzer/ShowPreAnalyzeAstTaskDef.java.mustache");
        this.showAnalyzedAstTaskDef = templateCompiler.getOrCompileToWriter("constraint_analyzer/ShowAnalyzedAstTaskDef.java.mustache");
        this.showScopeGraphTaskDef = templateCompiler.getOrCompileToWriter("constraint_analyzer/ShowScopeGraphTaskDef.java.mustache");
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
        if(input.languageProjectInput().enableStatix()) {
            showPreAnalyzeAstTaskDef.write(context, input.baseShowPreAnalyzeAstTaskDef().file(generatedJavaSourcesDirectory), input);
        }
        showAnalyzedAstTaskDef.write(context, input.baseShowAnalyzedAstTaskDef().file(generatedJavaSourcesDirectory), input);
        if(input.languageProjectInput().enableStatix()) {
            showScopeGraphTaskDef.write(context, input.baseShowScopeGraphTaskDef().file(generatedJavaSourcesDirectory), input);
        }
        return None.instance;
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override public Serializable key(Input input) {
        return input.adapterProject().project().baseDirectory();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>();
        dependencies.add(GradleConfiguredDependency.api(input.shared().constraintPieDep()));
        if(input.languageProjectInput().enableStatix()) {
            dependencies.add(GradleConfiguredDependency.api(input.shared().statixPieDep()));
        }
        return ListView.of(dependencies);
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

        // Show pre-analyze AST task definition and command

        @Value.Default default TypeInfo baseShowPreAnalyzeAstTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ShowPreAnalyzeAst");
        }

        Optional<TypeInfo> extendShowPreAnalyzeAstTaskDef();

        default TypeInfo showPreAnalyzeAstTaskDef() {
            return extendShowPreAnalyzeAstTaskDef().orElseGet(this::baseShowPreAnalyzeAstTaskDef);
        }

        @Value.Default default CommandDefRepr showPreAnalyzeAstCommand() {
            return CommandDefRepr.builder()
                .type(adapterProject().commandPackageId(), shared().defaultClassPrefix() + "ShowPreAnalyzeAstCommand")
                .taskDefType(showPreAnalyzeAstTaskDef())
                .displayName("Show pre-analyze AST")
                .description("Shows the pre-analyze AST")
                .addParams("file", TypeInfo.of(ResourceKey.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ReadableResource)))
                .build();
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
                .type(adapterProject().commandPackageId(), shared().defaultClassPrefix() + "ShowAnalyzedAstCommand")
                .taskDefType(showAnalyzedAstTaskDef())
                .displayName("Show analyzed AST")
                .description("Shows the analyzed AST")
                .addParams("rootDirectory", TypeInfo.of(ResourcePath.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)))
                .addParams("file", TypeInfo.of(ResourceKey.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ReadableResource)))
                .build();
        }

        // Show scope graph task definition and command

        @Value.Default default TypeInfo baseShowScopeGraphTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ShowScopeGraph");
        }

        Optional<TypeInfo> extendShowScopeGraphTaskDef();

        default TypeInfo showScopeGraphTaskDef() {
            return extendShowScopeGraphTaskDef().orElseGet(this::baseShowScopeGraphTaskDef);
        }

        @Value.Default default CommandDefRepr showScopeGraphCommand() {
            return CommandDefRepr.builder()
                .type(adapterProject().commandPackageId(), shared().defaultClassPrefix() + "ShowScopeGraphCommand")
                .taskDefType(showScopeGraphTaskDef())
                .displayName("Show scope graph")
                .description("Shows the scope graph")
                .addParams("rootDirectory", TypeInfo.of(ResourcePath.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)))
                .addParams("file", TypeInfo.of(ResourcePath.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ReadableResource)))
                .build();
        }


        /// Menus

        @Value.Default default MenuItemRepr mainMenu() {
            return editorContextMenu();
        }

        @Value.Default default MenuItemRepr resourceContextMenu() {
            final ArrayList<MenuItemRepr> menuItems = new ArrayList<>();
            if(languageProjectInput().enableStatix()) {
                menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showPreAnalyzeAstCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).enclosingProjectRequired().build()));
                menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showPreAnalyzeAstCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).enclosingProjectRequired().build()));
            }
            menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showAnalyzedAstCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).enclosingProjectRequired().build()));
            menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showAnalyzedAstCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).enclosingProjectRequired().build()));
            if(languageProjectInput().enableStatix()) {
                menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showScopeGraphCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).enclosingProjectRequired().build()));
                menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showScopeGraphCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).enclosingProjectRequired().build()));
            }
            return MenuItemRepr.menu("Debug", menuItems);
        }

        @Value.Default default MenuItemRepr editorContextMenu() {
            final ArrayList<MenuItemRepr> menuItems = new ArrayList<>();
            if(languageProjectInput().enableStatix()) {
                menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showPreAnalyzeAstCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).build()));
                menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showPreAnalyzeAstCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).build()));
            }
            menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showAnalyzedAstCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).enclosingProjectRequired().build()));
            menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showAnalyzedAstCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).enclosingProjectRequired().build()));
            if(languageProjectInput().enableStatix()) {
                menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showScopeGraphCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).enclosingProjectRequired().build()));
                menuItems.add(MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showScopeGraphCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).enclosingProjectRequired().build()));
            }
            return MenuItemRepr.menu("Debug", menuItems);
        }


        /// Collection methods

        default void collectTaskDefs(TypeInfoCollection taskDefs) {
            taskDefs.add(analyzeTaskDef(), baseAnalyzeTaskDef());
            taskDefs.add(analyzeMultiTaskDef(), baseAnalyzeMultiTaskDef());
            taskDefs.add(analyzeFileTaskDef(), baseAnalyzeFileTaskDef());
            if(languageProjectInput().enableStatix()) {
                taskDefs.add(showPreAnalyzeAstTaskDef(), baseShowPreAnalyzeAstTaskDef());
                taskDefs.add(showScopeGraphTaskDef(), baseShowScopeGraphTaskDef());
            }
            taskDefs.add(showAnalyzedAstTaskDef(), baseShowAnalyzedAstTaskDef());
        }

        default void collectCommands(Collection<CommandDefRepr> commands) {
            if(languageProjectInput().enableStatix()) {
                commands.add(showPreAnalyzeAstCommand());
                commands.add(showScopeGraphCommand());
            }
            commands.add(showAnalyzedAstCommand());
        }

        default void collectMenus(MenuItemCollection menuItems) {
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
            final ArrayList<ResourcePath> javaSourceFiles = new ArrayList<>();
            javaSourceFiles.add(baseAnalyzeTaskDef().file(generatedJavaSourcesDirectory));
            javaSourceFiles.add(baseAnalyzeMultiTaskDef().file(generatedJavaSourcesDirectory));
            javaSourceFiles.add(baseAnalyzeFileTaskDef().file(generatedJavaSourcesDirectory));
            if(languageProjectInput().enableStatix()) {
                javaSourceFiles.add(baseShowPreAnalyzeAstTaskDef().file(generatedJavaSourcesDirectory));
            }
            javaSourceFiles.add(baseShowAnalyzedAstTaskDef().file(generatedJavaSourcesDirectory));
            if(languageProjectInput().enableStatix()) {
                javaSourceFiles.add(baseShowAnalyzedAstTaskDef().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseShowScopeGraphTaskDef().file(generatedJavaSourcesDirectory));
            }
            return ListView.of(javaSourceFiles);
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
