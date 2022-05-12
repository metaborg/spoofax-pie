package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.data.ArgProviderRepr;
import mb.spoofax.compiler.adapter.data.CommandActionRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.MenuItemRepr;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
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
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Value.Enclosing
public class DynamixAdapterCompiler {
    private final TemplateWriter executeDynamixSpecificationTemplate;
    private final TemplateWriter showExecuteDynamixSpecificationTemplate;
    private final TemplateWriter readDynamixSpecTemplate;
    private final TemplateWriter executeAndRunDynamixSpecificationTemplate;
    private final TemplateWriter showExecuteAndRunDynamixSpecificationTemplate;

    @Inject public DynamixAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());

        this.executeDynamixSpecificationTemplate = templateCompiler.getOrCompileToWriter("dynamix/ExecuteDynamixSpecificationTaskDef.java.mustache");
        this.showExecuteDynamixSpecificationTemplate = templateCompiler.getOrCompileToWriter("dynamix/ShowExecuteDynamixSpecificationTaskDef.java.mustache");

        this.executeAndRunDynamixSpecificationTemplate = templateCompiler.getOrCompileToWriter("dynamix/ExecuteAndRunDynamixSpecificationTaskDef.java.mustache");
        this.showExecuteAndRunDynamixSpecificationTemplate = templateCompiler.getOrCompileToWriter("dynamix/ShowExecuteAndRunDynamixSpecificationTaskDef.java.mustache");

        this.readDynamixSpecTemplate = templateCompiler.getOrCompileToWriter("dynamix/ReadDynamixSpecTaskDef.java.mustache");
    }

    public None compile(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();

        executeDynamixSpecificationTemplate.write(context, input.baseExecuteDynamixSpecification().file(generatedJavaSourcesDirectory), input);
        showExecuteDynamixSpecificationTemplate.write(context, input.baseShowExecuteDynamixSpecification().file(generatedJavaSourcesDirectory), input);

        executeAndRunDynamixSpecificationTemplate.write(context, input.baseExecuteAndRunDynamixSpecification().file(generatedJavaSourcesDirectory), input);
        showExecuteAndRunDynamixSpecificationTemplate.write(context, input.baseShowExecuteAndRunDynamixSpecification().file(generatedJavaSourcesDirectory), input);

        readDynamixSpecTemplate.write(context, input.baseReadDynamixSpecTaskDef().file(generatedJavaSourcesDirectory), input);

        return None.instance;
    }

    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().dynamixRuntimeDep()),
            GradleConfiguredDependency.api(input.shared().timRuntimeDep())
        );
    }

    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends DynamixAdapterCompilerData.Input.Builder {
        }

        static DynamixAdapterCompiler.Input.Builder builder() {
            return new DynamixAdapterCompiler.Input.Builder();
        }

        /// Configuration

        @Value.Default default String mainRuleName() {
            return "main!compileFile";
        }

        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Adapter project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Execute dynamix specification task definition

        @Value.Default default TypeInfo baseExecuteDynamixSpecification() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ExecuteDynamixSpecification");
        }

        Optional<TypeInfo> extendExecuteDynamixSpecification();

        default TypeInfo executeDynamixSpecificationTaskDef() {
            return extendExecuteDynamixSpecification().orElseGet(this::baseExecuteDynamixSpecification);
        }

        // Read dynamix specification task definition

        @Value.Default default TypeInfo baseReadDynamixSpecTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ReadDynamixSpec");
        }

        Optional<TypeInfo> extendReadDynamixSpecTaskDef();

        default TypeInfo readDynamixSpecTaskDef() {
            return extendReadDynamixSpecTaskDef().orElseGet(this::baseReadDynamixSpecTaskDef);
        }

        // Execute dynamix specification and write to .tim file task definition

        @Value.Default default TypeInfo baseShowExecuteDynamixSpecification() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ShowExecuteDynamixSpecification");
        }

        Optional<TypeInfo> extendShowExecuteDynamixSpecification();

        default TypeInfo showExecuteDynamixSpecification() {
            return extendShowExecuteDynamixSpecification().orElseGet(this::baseShowExecuteDynamixSpecification);
        }

        // Execute dynamix specification into Tim AST, then execute Tim ast to string

        @Value.Default default TypeInfo baseExecuteAndRunDynamixSpecification() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ExecuteAndRunDynamixSpecification");
        }

        Optional<TypeInfo> extendExecuteAndRunDynamixSpecification();

        default TypeInfo executeAndRunDynamixSpecification() {
            return extendExecuteAndRunDynamixSpecification().orElseGet(this::baseExecuteAndRunDynamixSpecification);
        }

        // Execute dynamix specification to tim output, and show as feedback

        @Value.Default default TypeInfo baseShowExecuteAndRunDynamixSpecification() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ShowExecuteAndRunDynamixSpecification");
        }

        Optional<TypeInfo> extendShowExecuteAndRunDynamixSpecification();

        default TypeInfo showExecuteAndRunDynamixSpecification() {
            return extendShowExecuteAndRunDynamixSpecification().orElseGet(this::baseShowExecuteAndRunDynamixSpecification);
        }

        // Command for running dynamix to AST
        @Value.Default default CommandDefRepr showExecuteDynamixCommand() {
            return CommandDefRepr.builder()
                .type(adapterProject().commandPackageId(), shared().defaultClassPrefix() + "ShowExecuteDynamixSpecificationCommand")
                .taskDefType(showExecuteDynamixSpecification())
                .displayName("Compile using Dynamix")
                .description("Execute the Dynamix specification on the current input file and produce a compiled Tim file.")
                .addParams("rootDirectory", TypeInfo.of(ResourcePath.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)))
                .addParams("file", TypeInfo.of(ResourcePath.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ReadableResource)))
                .build();
        }

        // Command for executing dynamix and then running the resulting tim
        @Value.Default default CommandDefRepr showExecuteAndRunDynamixCommand() {
            return CommandDefRepr.builder()
                .type(adapterProject().commandPackageId(), shared().defaultClassPrefix() + "ShowExecuteAndRunDynamixSpecificationCommand")
                .taskDefType(showExecuteAndRunDynamixSpecification())
                .displayName("Compile and execute using Dynamix")
                .description("Execute the Dynamix specification on the current input file and execute the resulting Tim file.")
                .addParams("rootDirectory", TypeInfo.of(ResourcePath.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)))
                .addParams("file", TypeInfo.of(ResourceKey.class), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ReadableResource)))
                .build();
        }

        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                executeDynamixSpecificationTaskDef().file(generatedJavaSourcesDirectory),
                showExecuteDynamixSpecification().file(generatedJavaSourcesDirectory),

                executeAndRunDynamixSpecification().file(generatedJavaSourcesDirectory),
                showExecuteAndRunDynamixSpecification().file(generatedJavaSourcesDirectory),

                readDynamixSpecTaskDef().file(generatedJavaSourcesDirectory)
            );
        }

        /// Menus

        @Value.Default default MenuItemRepr mainMenu() {
            return editorContextMenu();
        }

        @Value.Default default MenuItemRepr resourceContextMenu() {
            return MenuItemRepr.menu("Dynamix",
                MenuItemRepr.commandAction(CommandActionRepr.builder()
                    .manualOnce(showExecuteDynamixCommand())
                    .addRequiredResourceTypes(HierarchicalResourceType.File)
                    .addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Project)
                    .build()),
                // todo: does not actually work since showFile cancels it?
//                MenuItemRepr.commandAction(CommandActionRepr.builder()
//                    .manualContinuous(showExecuteDynamixCommand())
//                    .addRequiredResourceTypes(HierarchicalResourceType.File)
//                    .addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Project)
//                    .build()),

                MenuItemRepr.commandAction(CommandActionRepr.builder()
                    .manualOnce(showExecuteAndRunDynamixCommand())
                    .addRequiredResourceTypes(HierarchicalResourceType.File)
                    .addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Project)
                    .build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder()
                    .manualContinuous(showExecuteAndRunDynamixCommand())
                    .addRequiredResourceTypes(HierarchicalResourceType.File)
                    .addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Project)
                    .build())
            );
        }

        @Value.Default default MenuItemRepr editorContextMenu() {
            return MenuItemRepr.menu("Dynamix",
                MenuItemRepr.commandAction(CommandActionRepr.builder()
                    .manualOnce(showExecuteDynamixCommand())
                    .addRequiredResourceTypes(HierarchicalResourceType.File)
                    .addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Project)
                    .build()),
                // todo: does not actually work since showFile cancels it?
//                MenuItemRepr.commandAction(CommandActionRepr.builder()
//                    .manualContinuous(showExecuteDynamixCommand())
//                    .addRequiredResourceTypes(HierarchicalResourceType.File)
//                    .addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Project)
//                    .build()),

                MenuItemRepr.commandAction(CommandActionRepr.builder()
                    .manualOnce(showExecuteAndRunDynamixCommand())
                    .addRequiredResourceTypes(HierarchicalResourceType.File)
                    .addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Project)
                    .build()),
                MenuItemRepr.commandAction(CommandActionRepr.builder()
                    .manualContinuous(showExecuteAndRunDynamixCommand())
                    .addRequiredResourceTypes(HierarchicalResourceType.File)
                    .addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Project)
                    .build())
            );
        }

        /// Collection methods

        default void collectTaskDefs(TypeInfoCollection taskDefs) {
            taskDefs.add(executeDynamixSpecificationTaskDef(), baseExecuteDynamixSpecification());
            taskDefs.add(showExecuteDynamixSpecification(), baseShowExecuteDynamixSpecification());

            taskDefs.add(executeAndRunDynamixSpecification(), baseExecuteAndRunDynamixSpecification());
            taskDefs.add(showExecuteAndRunDynamixSpecification(), baseShowExecuteAndRunDynamixSpecification());

            taskDefs.add(readDynamixSpecTaskDef(), baseReadDynamixSpecTaskDef());
        }

        default void collectCommands(Collection<CommandDefRepr> commands) {
            commands.add(showExecuteDynamixCommand());
            commands.add(showExecuteAndRunDynamixCommand());
        }

        default void collectMenus(MenuItemCollection menuItems) {
            menuItems.addMainMenuItem(mainMenu());
            menuItems.addResourceContextMenuItem(resourceContextMenu());
            menuItems.addEditorContextMenuItem(editorContextMenu());
        }

        /// Automatically provided sub-inputs

        @Value.Auxiliary Shared shared();

        AdapterProject adapterProject();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();

        ConstraintAnalyzerAdapterCompiler.Input constraintAnalyzerInput();
    }
}
