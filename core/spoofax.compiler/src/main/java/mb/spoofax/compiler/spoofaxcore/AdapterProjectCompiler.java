package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Mustache;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.cli.CliCommandRepr;
import mb.spoofax.compiler.command.AutoCommandRequestRepr;
import mb.spoofax.compiler.command.CommandDefRepr;
import mb.spoofax.compiler.menu.MenuItemRepr;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.NamedTypeInfo;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.compiler.util.UniqueNamer;
import mb.spoofax.core.language.taskdef.NullCompleteTaskDef;
import mb.spoofax.core.language.taskdef.NullStyler;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class AdapterProjectCompiler {
    private final TemplateWriter buildGradleTemplate;
    private final TemplateWriter packageInfoTemplate;
    private final TemplateWriter checkTaskDefTemplate;
    private final TemplateWriter checkMultiTaskDefTemplate;
    private final TemplateWriter checkAggregatorTaskDefTemplate;
    private final TemplateWriter componentTemplate;
    private final TemplateWriter moduleTemplate;
    private final TemplateWriter instanceTemplate;
    private final TemplateWriter commandDefTemplate;

    private final ParserCompiler parserCompiler;
    private final StylerCompiler stylerCompiler;
    private final CompleterCompiler completerCompiler;
    private final StrategoRuntimeCompiler strategoRuntimeCompiler;
    private final ConstraintAnalyzerCompiler constraintAnalyzerCompiler;
    private final MultilangAnalyzerCompiler multilangAnalyzerCompiler;

    public AdapterProjectCompiler(
        TemplateCompiler templateCompiler,
        ParserCompiler parserCompiler,
        StylerCompiler stylerCompiler,
        CompleterCompiler completerCompiler,
        StrategoRuntimeCompiler strategoRuntimeCompiler,
        ConstraintAnalyzerCompiler constraintAnalyzerCompiler,
        MultilangAnalyzerCompiler multilangAnalyzerCompiler) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("adapter_project/build.gradle.kts.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("adapter_project/package-info.java.mustache");
        this.checkTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckTaskDef.java.mustache");
        this.checkMultiTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckMultiTaskDef.java.mustache");
        this.checkAggregatorTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckAggregatorTaskDef.java.mustache");
        this.componentTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Component.java.mustache");
        this.moduleTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Module.java.mustache");
        this.instanceTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Instance.java.mustache");
        this.commandDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CommandDef.java.mustache");

        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.completerCompiler = completerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
        this.multilangAnalyzerCompiler = multilangAnalyzerCompiler;
    }

    public void generateInitial(Input input) throws IOException {
        buildGradleTemplate.write(input.buildGradleKtsFile(), input);
    }

    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.apiPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessorPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.api(input.languageProjectDependency()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCoreDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.pieApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.daggerDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
        return dependencies;
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        // Files from other compilers.
        parserCompiler.compileAdapterProject(input.parser());
        try {
            input.styler().ifPresent((i) -> {
                try {
                    stylerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.completer().ifPresent((i) -> {
                try {
                    completerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.strategoRuntime().ifPresent((i) -> {
                try {
                    strategoRuntimeCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.constraintAnalyzer().ifPresent((i) -> {
                try {
                    constraintAnalyzerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.multilangAnalyzer().ifPresent((i) -> {
                try {
                    multilangAnalyzerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        // Collect all task definitions.
        final ArrayList<TypeInfo> allTaskDefs = new ArrayList<>(input.taskDefs());
        allTaskDefs.add(input.parser().tokenizeTaskDef());
        allTaskDefs.add(input.parser().parseTaskDef());
        if(input.styler().isPresent()) {
            allTaskDefs.add(input.styler().get().styleTaskDef());
        } else {
            allTaskDefs.add(TypeInfo.of(NullStyler.class));
        }
        if(input.completer().isPresent()) {
            allTaskDefs.add(input.completer().get().completeTaskDef());
        } else {
            allTaskDefs.add(TypeInfo.of(NullCompleteTaskDef.class));
        }
        input.constraintAnalyzer().ifPresent((i) -> {
            allTaskDefs.add(i.analyzeTaskDef());
            allTaskDefs.add(i.analyzeMultiTaskDef());
        });
        input.multilangAnalyzer().ifPresent((i) -> {
            allTaskDefs.add(i.analyzeTaskDef());
            allTaskDefs.add(i.indexAstTaskDef());
            allTaskDefs.add(i.preStatixTaskDef());
            allTaskDefs.add(i.postStatixTaskDef());
            allTaskDefs.addAll(i.libraryTaskDefs());
        });
        allTaskDefs.add(input.checkTaskDef());
        allTaskDefs.add(input.checkMultiTaskDef());
        allTaskDefs.add(input.checkAggregatorTaskDef());

        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        packageInfoTemplate.write(input.packageInfo().file(classesGenDirectory), input);
        checkTaskDefTemplate.write(input.genCheckTaskDef().file(classesGenDirectory), input);
        checkMultiTaskDefTemplate.write(input.genCheckMultiTaskDef().file(classesGenDirectory), input);
        checkAggregatorTaskDefTemplate.write(input.genCheckAggregatorTaskDef().file(classesGenDirectory), input);
        componentTemplate.write(input.genComponent().file(classesGenDirectory), input);
        for(CommandDefRepr commandDef : input.commandDefs()) {
            final UniqueNamer uniqueNamer = new UniqueNamer();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("taskDefInjection", uniqueNamer.makeUnique(commandDef.taskDefType()));
            commandDefTemplate.write(commandDef.type().file(classesGenDirectory), commandDef, map);
        }

        {
            final UniqueNamer uniqueNamer = new UniqueNamer();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("providedTaskDefs", allTaskDefs.stream().map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedCommandDefs", input.commandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedAutoCommandDefs", input.autoCommandDefs().stream().map((c) -> uniqueNamer.makeUnique(c, c.commandDef().asVariableId())).collect(Collectors.toList()));
            moduleTemplate.write(input.genModule().file(classesGenDirectory), input, map);
        }
        {
            final UniqueNamer uniqueNamer = new UniqueNamer();
            uniqueNamer.reserve("fileExtensions");
            uniqueNamer.reserve("commandDefs");
            uniqueNamer.reserve("autoCommandDefs");
            final HashMap<String, Object> map = new HashMap<>();

            // Collect all injections.
            final ArrayList<NamedTypeInfo> injected = new ArrayList<>();
            map.put("injected", injected);

            // Create injections for tasks required in the language instance.
            final NamedTypeInfo parseInjection = uniqueNamer.makeUnique(input.parser().parseTaskDef());
            map.put("parseInjection", parseInjection);
            injected.add(parseInjection);
            final NamedTypeInfo tokenizeInjection = uniqueNamer.makeUnique(input.parser().tokenizeTaskDef());
            map.put("tokenizeInjection", tokenizeInjection);
            injected.add(tokenizeInjection);
            final NamedTypeInfo checkInjection;
            if(input.isMultiFile()) {
                checkInjection = uniqueNamer.makeUnique(input.checkMultiTaskDef());
            } else {
                checkInjection = uniqueNamer.makeUnique(input.checkAggregatorTaskDef());
            }
            injected.add(checkInjection);
            map.put("checkInjection", checkInjection);
            final NamedTypeInfo styleInjection;
            if(input.styler().isPresent()) {
                styleInjection = uniqueNamer.makeUnique(input.styler().get().styleTaskDef());
            } else {
                styleInjection = uniqueNamer.makeUnique(TypeInfo.of(NullStyler.class));
            }
            map.put("styleInjection", styleInjection);
            injected.add(styleInjection);
            final NamedTypeInfo completeInjection;
            if(input.completer().isPresent()) {
                completeInjection = uniqueNamer.makeUnique(input.completer().get().completeTaskDef());
            } else {
                completeInjection = uniqueNamer.makeUnique(TypeInfo.of(NullCompleteTaskDef.class));
            }
            map.put("completeInjection", completeInjection);
            injected.add(completeInjection);

            // Create injections for all command definitions. TODO: only inject needed command definitions?
            injected.addAll(input.commandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            // Provide a lambda that gets the name of the injected command definition from the context.
            map.put("getInjectedCommandDef", (Mustache.Lambda)(frag, out) -> {
                final TypeInfo type = (TypeInfo)frag.context();
                final @Nullable String name = uniqueNamer.getNameFor(type);
                if(name == null) {
                    throw new IllegalStateException("Cannot get injected command definition for type '" + type + "'");
                }
                out.write(name);
            });

            instanceTemplate.write(input.genInstance().file(classesGenDirectory), input, map);
        }

        return Output.builder().addAllProvidedFiles(input.providedFiles()).build();
    }

    // Inputs

    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends AdapterProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Project

        AdapterProject adapterProject();


        /// Sub-inputs

        ClassloaderResourcesCompiler.AdapterProjectInput classloaderResources();

        ParserCompiler.AdapterProjectInput parser();

        Optional<StylerCompiler.AdapterProjectInput> styler();

        Optional<CompleterCompiler.AdapterProjectInput> completer();

        Optional<StrategoRuntimeCompiler.AdapterProjectInput> strategoRuntime();

        Optional<ConstraintAnalyzerCompiler.AdapterProjectInput> constraintAnalyzer();

        Optional<MultilangAnalyzerCompiler.AdapterProjectInput> multilangAnalyzer();


        /// Configuration

        GradleDependency languageProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();

        List<TypeInfo> taskDefs();

        List<CommandDefRepr> commandDefs();

        List<AutoCommandRequestRepr> autoCommandDefs();

        @Value.Default default CliCommandRepr cliCommand() {
            return CliCommandRepr.builder().name(shared().name()).build();
        }

        @Value.Default default List<MenuItemRepr> mainMenuItems() {
            return editorContextMenuItems();
        }

        List<MenuItemRepr> resourceContextMenuItems();

        List<MenuItemRepr> editorContextMenuItems();

        default boolean isMultiFile() {
            return constraintAnalyzer().map(a -> a.languageProjectInput().multiFile()).orElse(false);
        }

        default boolean isMultiLang() {
            return multilangAnalyzer().isPresent();
        }

        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return adapterProject().project().baseDirectory().appendRelativePath("build.gradle.kts");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return adapterProject().project().genSourceSpoofaxJavaDirectory();
        }


        /// Adapter project classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() {
            return TypeInfo.of(adapterProject().packageId(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return genPackageInfo();
        }

        // Dagger component

        @Value.Default default TypeInfo genComponent() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "Component");
        }

        Optional<TypeInfo> manualComponent();

        default TypeInfo component() {
            if(classKind().isManual() && manualComponent().isPresent()) {
                return manualComponent().get();
            }
            return genComponent();
        }

        default TypeInfo daggerComponent() {
            return TypeInfo.of(component().packageId(), "Dagger" + component().id());
        }

        // Dagger module

        @Value.Default default TypeInfo genModule() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "Module");
        }

        Optional<TypeInfo> manualModule();

        default TypeInfo module() {
            if(classKind().isManual() && manualModule().isPresent()) {
                return manualModule().get();
            }
            return genModule();
        }

        // Language instance

        @Value.Default default TypeInfo genInstance() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "Instance");
        }

        Optional<TypeInfo> manualInstance();

        default TypeInfo instance() {
            if(classKind().isManual() && manualInstance().isPresent()) {
                return manualInstance().get();
            }
            return genInstance();
        }


        /// Adapter project task definitions

        // Check task definition

        @Value.Default default TypeInfo genCheckTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Check");
        }

        Optional<TypeInfo> manualCheckTaskDef();

        default TypeInfo checkTaskDef() {
            if(classKind().isManual() && manualCheckTaskDef().isPresent()) {
                return manualCheckTaskDef().get();
            }
            return genCheckTaskDef();
        }

        // Multi-file check task definition

        @Value.Default default TypeInfo genCheckMultiTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "CheckMulti");
        }

        Optional<TypeInfo> manualCheckMultiTaskDef();

        default TypeInfo checkMultiTaskDef() {
            if(classKind().isManual() && manualCheckMultiTaskDef().isPresent()) {
                return manualCheckMultiTaskDef().get();
            }
            return genCheckMultiTaskDef();
        }

        // Single file check results aggregator task definition

        @Value.Default default TypeInfo genCheckAggregatorTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "CheckAggregator");
        }

        Optional<TypeInfo> manualCheckAggregatorTaskDef();

        default TypeInfo checkAggregatorTaskDef() {
            if(classKind().isManual() && manualCheckAggregatorTaskDef().isPresent()) {
                return manualCheckAggregatorTaskDef().get();
            }
            return genCheckAggregatorTaskDef();
        }

        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            final ArrayList<ResourcePath> generatedFiles = new ArrayList<>();
            if(classKind().isGenerating()) {
                final ResourcePath classesGenDirectory = classesGenDirectory();
                generatedFiles.add(genPackageInfo().file(classesGenDirectory));
                generatedFiles.add(genComponent().file(classesGenDirectory));
                generatedFiles.add(genModule().file(classesGenDirectory));
                generatedFiles.add(genInstance().file(classesGenDirectory));
                generatedFiles.add(genCheckTaskDef().file(classesGenDirectory));
                generatedFiles.add(genCheckMultiTaskDef().file(classesGenDirectory));
            }
            parser().generatedFiles().addAllTo(generatedFiles);
            styler().ifPresent((i) -> i.generatedFiles().addAllTo(generatedFiles));
            completer().ifPresent((i) -> i.generatedFiles().addAllTo(generatedFiles));
            constraintAnalyzer().ifPresent((i) -> i.generatedFiles().addAllTo(generatedFiles));
            multilangAnalyzer().ifPresent((i) -> i.generatedFiles().addAllTo(generatedFiles));
            return generatedFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualComponent().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualComponent' has not been set");
            }
            if(!manualModule().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualModule' has not been set");
            }
            if(!manualInstance().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualInstance' has not been set");
            }
            if(!manualCheckTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualCheckTaskDef' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output {
        class Builder extends AdapterProjectCompilerData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }

        List<ResourcePath> providedFiles();
    }
}
