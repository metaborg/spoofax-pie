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
import mb.spoofax.compiler.language.ParserLanguageCompiler;
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
import mb.spoofax.core.language.command.HierarchicalResourceType;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Value.Enclosing
public class ParserAdapterCompiler implements TaskDef<ParserAdapterCompiler.Input, None> {
    private final TemplateWriter parseTaskDefTemplate;
    private final TemplateWriter tokenizeTaskDefTemplate;
    private final TemplateWriter showParsedAstTaskDefTemplate;
    private final TemplateWriter showParsedTokensTaskDefTemplate;

    @Inject public ParserAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.parseTaskDefTemplate = templateCompiler.getOrCompileToWriter("parser/ParseTaskDef.java.mustache");
        this.tokenizeTaskDefTemplate = templateCompiler.getOrCompileToWriter("parser/TokenizeTaskDef.java.mustache");
        this.showParsedAstTaskDefTemplate = templateCompiler.getOrCompileToWriter("parser/ShowParsedAstTaskDef.java.mustache");
        this.showParsedTokensTaskDefTemplate = templateCompiler.getOrCompileToWriter("parser/ShowParsedTokensTaskDef.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        parseTaskDefTemplate.write(context, input.baseParseTaskDef().file(generatedJavaSourcesDirectory), input);
        tokenizeTaskDefTemplate.write(context, input.baseTokenizeTaskDef().file(generatedJavaSourcesDirectory), input);
        showParsedAstTaskDefTemplate.write(context, input.baseShowParsedAstTaskDef().file(generatedJavaSourcesDirectory), input);
        showParsedTokensTaskDefTemplate.write(context, input.baseShowParsedTokensTaskDef().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override public Serializable key(Input input) {
        return input.adapterProject().project().baseDirectory();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().atermCommonDep()),
            GradleConfiguredDependency.api(input.shared().jsglrPieDep())
        );
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ParserAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Parse task definition

        @Value.Default default TypeInfo baseParseTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Parse");
        }

        Optional<TypeInfo> extendParseTaskDef();

        default TypeInfo parseTaskDef() {
            return extendParseTaskDef().orElseGet(this::baseParseTaskDef);
        }

        // Tokenize task definition

        @Value.Default default TypeInfo baseTokenizeTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Tokenize");
        }

        Optional<TypeInfo> extendTokenizeTaskDef();

        default TypeInfo tokenizeTaskDef() {
            return extendTokenizeTaskDef().orElseGet(this::baseTokenizeTaskDef);
        }

        // Show parsed AST task definition and command

        @Value.Default default TypeInfo baseShowParsedAstTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ShowParsedAst");
        }

        Optional<TypeInfo> extendShowParsedAstTaskDef();

        default TypeInfo showParsedAstTaskDef() {
            return extendShowParsedAstTaskDef().orElseGet(this::baseShowParsedAstTaskDef);
        }

        @Value.Default default CommandDefRepr showParsedAstCommand() {
            return CommandDefRepr.builder()
                .type(adapterProject().commandPackageId(), shared().defaultClassPrefix() + "ShowParsedAstCommand")
                .taskDefType(showParsedAstTaskDef())
                .displayName("Show parsed AST")
                .description("Shows the parsed AST")
                .addParams("file", TypeInfo.of(ResourceKey.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ReadableResource)))
                .build();
        }

        // Show parsed tokens task definition and command

        @Value.Default default TypeInfo baseShowParsedTokensTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ShowParsedTokens");
        }

        Optional<TypeInfo> extendShowParsedTokensTaskDef();

        default TypeInfo showParsedTokensTaskDef() {
            return extendShowParsedTokensTaskDef().orElseGet(this::baseShowParsedTokensTaskDef);
        }

        @Value.Default default CommandDefRepr showParsedTokensCommand() {
            return CommandDefRepr.builder()
                .type(adapterProject().commandPackageId(), shared().defaultClassPrefix() + "ShowParsedTokensCommand")
                .taskDefType(showParsedTokensTaskDef())
                .displayName("Show parsed tokens")
                .description("Shows the parsed tokens")
                .addParams("file", TypeInfo.of(ResourceKey.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ReadableResource)))
                .build();
        }


        /// Menus

        @Value.Default default MenuItemRepr mainMenu() {
            return editorContextMenu();
        }

        @Value.Default default MenuItemRepr resourceContextMenu() {
            return MenuItemRepr.menu("Debug",
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showParsedAstCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showParsedAstCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showParsedTokensCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showParsedTokensCommand()).addRequiredResourceTypes(HierarchicalResourceType.File).build())
            );
        }

        @Value.Default default MenuItemRepr editorContextMenu() {
            return MenuItemRepr.menu("Debug",
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showParsedAstCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showParsedAstCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualOnce(showParsedTokensCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder().manualContinuous(showParsedTokensCommand()).addRequiredEditorFileTypes(EditorFileType.ReadableResource).build())
            );
        }


        /// Collection methods

        default void collectInto(TypeInfoCollection taskDefs, Collection<CommandDefRepr> commands, MenuItemCollection menuItems) {
            taskDefs.add(tokenizeTaskDef(), baseTokenizeTaskDef());
            taskDefs.add(parseTaskDef(), baseParseTaskDef());
            taskDefs.add(showParsedAstTaskDef(), baseShowParsedAstTaskDef());
            commands.add(showParsedAstCommand());
            taskDefs.add(showParsedTokensTaskDef(), baseShowParsedTokensTaskDef());
            commands.add(showParsedTokensCommand());
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
                baseParseTaskDef().file(generatedJavaSourcesDirectory),
                baseTokenizeTaskDef().file(generatedJavaSourcesDirectory),
                baseShowParsedAstTaskDef().file(generatedJavaSourcesDirectory),
                baseShowParsedTokensTaskDef().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs.

        @Value.Auxiliary Shared shared();

        AdapterProject adapterProject();

        ParserLanguageCompiler.Input languageProjectInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();
    }
}
