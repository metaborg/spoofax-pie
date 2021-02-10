package mb.spoofax.compiler.adapter;

import com.samskivert.mustache.Mustache;
import mb.common.option.Option;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.data.AutoCommandRequestRepr;
import mb.spoofax.compiler.adapter.data.CliCommandRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.MenuItemRepr;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.NamedTypeInfo;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.compiler.util.UniqueNamer;
import mb.spoofax.core.language.taskdef.NoneStyler;
import mb.spoofax.core.language.taskdef.NoneTokenizer;
import mb.spoofax.core.language.taskdef.NullCompleteTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class AdapterProjectCompiler implements TaskDef<AdapterProjectCompiler.Input, None> {
    private final TemplateWriter packageInfoTemplate;
    private final TemplateWriter checkTaskDefTemplate;
    private final TemplateWriter checkMultiTaskDefTemplate;
    private final TemplateWriter checkAggregatorTaskDefTemplate;
    private final TemplateWriter resourcesScopeTemplate;
    private final TemplateWriter resourcesComponentTemplate;
    private final TemplateWriter resourcesModuleTemplate;
    private final TemplateWriter scopeTemplate;
    private final TemplateWriter qualifierTemplate;
    private final TemplateWriter componentTemplate;
    private final TemplateWriter moduleTemplate;
    private final TemplateWriter instanceTemplate;
    private final TemplateWriter commandDefTemplate;

    private final ParserAdapterCompiler parserCompiler;
    private final StylerAdapterCompiler stylerCompiler;
    private final CompleterAdapterCompiler completerCompiler;
    private final StrategoRuntimeAdapterCompiler strategoRuntimeCompiler;
    private final ConstraintAnalyzerAdapterCompiler constraintAnalyzerCompiler;
    private final MultilangAnalyzerAdapterCompiler multilangAnalyzerCompiler;

    @Inject public AdapterProjectCompiler(
        TemplateCompiler templateCompiler,
        ParserAdapterCompiler parserCompiler,
        StylerAdapterCompiler stylerCompiler,
        CompleterAdapterCompiler completerCompiler,
        StrategoRuntimeAdapterCompiler strategoRuntimeCompiler,
        ConstraintAnalyzerAdapterCompiler constraintAnalyzerCompiler,
        MultilangAnalyzerAdapterCompiler multilangAnalyzerCompiler
    ) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("adapter_project/package-info.java.mustache");
        this.checkTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckTaskDef.java.mustache");
        this.checkMultiTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckMultiTaskDef.java.mustache");
        this.checkAggregatorTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckAggregatorTaskDef.java.mustache");
        this.resourcesScopeTemplate = templateCompiler.getOrCompileToWriter("adapter_project/ResourcesScope.java.mustache");
        this.resourcesComponentTemplate = templateCompiler.getOrCompileToWriter("adapter_project/ResourcesComponent.java.mustache");
        this.resourcesModuleTemplate = templateCompiler.getOrCompileToWriter("adapter_project/ResourcesModule.java.mustache");
        this.scopeTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Scope.java.mustache");
        this.qualifierTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Qualifier.java.mustache");
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


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        final Shared shared = input.shared();

        // Files from other compilers.
        input.parser().ifPresent((i) -> context.require(parserCompiler, i));
        input.styler().ifPresent((i) -> context.require(stylerCompiler, i));
        input.completer().ifPresent((i) -> context.require(completerCompiler, i));
        input.strategoRuntime().ifPresent((i) -> context.require(strategoRuntimeCompiler, i));
        input.constraintAnalyzer().ifPresent((i) -> context.require(constraintAnalyzerCompiler, i));
        input.multilangAnalyzer().ifPresent((i) -> context.require(multilangAnalyzerCompiler, i));

        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.

        // Collect all task definitions.
        final ArrayList<TypeInfo> allTaskDefs = new ArrayList<>(input.taskDefs());
        if(input.parser().isPresent()) {
            allTaskDefs.add(input.parser().get().tokenizeTaskDef());
            allTaskDefs.add(input.parser().get().parseTaskDef());
        } else {
            allTaskDefs.add(TypeInfo.of(NoneTokenizer.class));
        }
        if(input.styler().isPresent()) {
            allTaskDefs.add(input.styler().get().styleTaskDef());
        } else {
            allTaskDefs.add(TypeInfo.of(NoneStyler.class));
        }
        if(input.completer().isPresent()) {
            allTaskDefs.add(input.completer().get().completeTaskDef());
        } else {
            allTaskDefs.add(TypeInfo.of(NullCompleteTaskDef.class));
        }
        input.strategoRuntime().ifPresent((i) -> {
            allTaskDefs.add(i.getStrategoRuntimeProviderTaskDef());
        });
        input.constraintAnalyzer().ifPresent((i) -> {
            allTaskDefs.add(i.analyzeTaskDef());
            allTaskDefs.add(i.analyzeMultiTaskDef());
        });
        input.multilangAnalyzer().ifPresent((i) -> {
            allTaskDefs.add(i.analyzeTaskDef());
            allTaskDefs.add(i.indexAstTaskDef());
            allTaskDefs.add(i.preStatixTaskDef());
            allTaskDefs.add(i.postStatixTaskDef());
            allTaskDefs.add(i.checkTaskDef());
            allTaskDefs.addAll(i.libraryTaskDefs());
        });
        allTaskDefs.add(input.checkTaskDef());
        allTaskDefs.add(input.checkMultiTaskDef());
        allTaskDefs.add(input.checkAggregatorTaskDef());

        // Class files
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        if(input.languageProjectDependency().isSome()) {
            // Only generate package-info.java if the language project is a separate project. Otherwise we will have
            // two package-info.java files in the same package, which is an error.
            packageInfoTemplate.write(context, input.packageInfo().file(generatedJavaSourcesDirectory), input);
        }
        checkTaskDefTemplate.write(context, input.baseCheckTaskDef().file(generatedJavaSourcesDirectory), input);
        checkMultiTaskDefTemplate.write(context, input.baseCheckMultiTaskDef().file(generatedJavaSourcesDirectory), input);
        checkAggregatorTaskDefTemplate.write(context, input.baseCheckAggregatorTaskDef().file(generatedJavaSourcesDirectory), input);

        resourcesScopeTemplate.write(context, input.baseResourcesScope().file(generatedJavaSourcesDirectory), input);
        resourcesComponentTemplate.write(context, input.baseResourcesComponent().file(generatedJavaSourcesDirectory), input);
        resourcesModuleTemplate.write(context, input.baseResourcesModule().file(generatedJavaSourcesDirectory), input);

        scopeTemplate.write(context, input.adapterProject().baseScope().file(generatedJavaSourcesDirectory), input);
        qualifierTemplate.write(context, input.baseQualifier().file(generatedJavaSourcesDirectory), input);

        for(CommandDefRepr commandDef : input.commandDefs()) {
            final UniqueNamer uniqueNamer = new UniqueNamer();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("scope", input.scope());
            map.put("taskDefInjection", uniqueNamer.makeUnique(commandDef.taskDefType()));
            commandDefTemplate.write(context, commandDef.type().file(generatedJavaSourcesDirectory), commandDef, map);
        }

        { // Component
            final UniqueNamer uniqueNamer = new UniqueNamer();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("providedTaskDefs", allTaskDefs.stream().map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            map.put("providedCommandDefs", input.commandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            componentTemplate.write(context, input.baseComponent().file(generatedJavaSourcesDirectory), input, map);
        }

        { // Module
            final UniqueNamer uniqueNamer = new UniqueNamer();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("providedTaskDefs", allTaskDefs.stream().map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedCommandDefs", input.commandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedAutoCommandDefs", input.autoCommandDefs().stream().map((c) -> uniqueNamer.makeUnique(c, c.commandDef().asVariableId())).collect(Collectors.toList()));
            moduleTemplate.write(context, input.baseModule().file(generatedJavaSourcesDirectory), input, map);
        }

        { // Instance
            final UniqueNamer uniqueNamer = new UniqueNamer();
            uniqueNamer.reserve("fileExtensions");
            uniqueNamer.reserve("taskDefs");
            uniqueNamer.reserve("commandDefs");
            uniqueNamer.reserve("autoCommandDefs");
            final HashMap<String, Object> map = new HashMap<>();

            // Collect all injections.
            final ArrayList<NamedTypeInfo> injected = new ArrayList<>();
            map.put("injected", injected);

            // Create injections for tasks required in the language instance.
            if(input.parser().isPresent()) {
                final NamedTypeInfo parseInjection = uniqueNamer.makeUnique(input.parser().get().parseTaskDef());
                map.put("parseInjection", parseInjection);
                injected.add(parseInjection);
                final NamedTypeInfo tokenizeInjection = uniqueNamer.makeUnique(input.parser().get().tokenizeTaskDef());
                map.put("tokenizeInjection", tokenizeInjection);
                injected.add(tokenizeInjection);
            } else {
                map.put("parseInjection", false);
                final NamedTypeInfo tokenizeInjection = uniqueNamer.makeUnique(TypeInfo.of(NoneTokenizer.class));
                map.put("tokenizeInjection", tokenizeInjection);
                injected.add(tokenizeInjection);
            }
            final NamedTypeInfo checkInjection;
            if(input.multilangAnalyzer().isPresent()) { // isMultiLang will be true
                map.put("languageId", input.multilangAnalyzer().get().languageId());
                map.put("contextId", input.multilangAnalyzer().get().contextId());
            }
            if(input.multilangAnalyzer().isPresent()) {
                checkInjection = uniqueNamer.makeUnique(input.multilangAnalyzer().get().checkTaskDef());
            } else if(input.isMultiFile()) {
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
                styleInjection = uniqueNamer.makeUnique(TypeInfo.of(NoneStyler.class));
            }
            map.put("styleInjection", styleInjection);
            injected.add(styleInjection);
            final NamedTypeInfo completeInjection;
            if(input.completer().isPresent() && input.parser().isPresent()) {
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

            instanceTemplate.write(context, input.baseInstance().file(generatedJavaSourcesDirectory), input, map);
        }

        return None.instance;
    }


    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.apiPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessorPlatform(shared.spoofaxDependencyConstraintsDep()));
        input.languageProjectDependency().ifSome((d) -> dependencies.add(GradleConfiguredDependency.api(d)));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCoreDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.pieApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.daggerDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
        input.parser().ifPresent((i) -> parserCompiler.getDependencies(i).addAllTo(dependencies));
        input.constraintAnalyzer().ifPresent((i) -> constraintAnalyzerCompiler.getDependencies(i).addAllTo(dependencies));
        input.strategoRuntime().ifPresent((i) -> strategoRuntimeCompiler.getDependencies(i).addAllTo(dependencies));
        return dependencies;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends AdapterProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Project

        AdapterProject adapterProject();


        /// Sub-inputs

        ClassLoaderResourcesCompiler.Input classLoaderResources();

        Optional<ParserAdapterCompiler.Input> parser();

        Optional<StylerAdapterCompiler.Input> styler();

        Optional<CompleterAdapterCompiler.Input> completer();

        Optional<StrategoRuntimeAdapterCompiler.Input> strategoRuntime();

        Optional<ConstraintAnalyzerAdapterCompiler.Input> constraintAnalyzer();

        Optional<MultilangAnalyzerAdapterCompiler.Input> multilangAnalyzer();


        /// Configuration

        /* None indicates that the language project is the same project as the adapter project */
        Option<GradleDependency> languageProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();

        List<TypeInfo> additionalModules();

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

        @Value.Default default boolean isMultiFile() {
            return constraintAnalyzer().map(a -> a.languageProjectInput().multiFile()).orElse(false);
        }

        default Optional<AdapterProjectCompiler.Input> isMultiLang() {
            // Hack: return this ony if multilang is present
            // To make sure the compiler can reference data from other compilers as well
            if(multilangAnalyzer().isPresent()) {
                return Optional.of(this);
            }
            return Optional.empty();
        }

        default List<NamedTypeInfo> checkInjections() {
            ArrayList<NamedTypeInfo> results = new ArrayList<>();
            results.add(NamedTypeInfo.of("classLoaderResources", classLoaderResources().classLoaderResources()));
            parser().ifPresent(i -> results.add(NamedTypeInfo.of("parse", i.parseTaskDef())));
            constraintAnalyzer().ifPresent(i -> results.add(NamedTypeInfo.of("analyze", i.analyzeTaskDef())));
            return results;
        }

        default List<NamedTypeInfo> checkMultiInjections() {
            ArrayList<NamedTypeInfo> results = new ArrayList<>();
            results.add(NamedTypeInfo.of("classLoaderResources", classLoaderResources().classLoaderResources()));
            parser().ifPresent(i -> results.add(NamedTypeInfo.of("parse", i.parseTaskDef())));
            constraintAnalyzer().ifPresent(i -> results.add(NamedTypeInfo.of("analyze", i.analyzeMultiTaskDef())));
            return results;
        }

        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return adapterProject().project().baseDirectory().appendRelativePath("build.gradle.kts");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Adapter project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // package-info

        @Value.Default default TypeInfo basePackageInfo() {
            return TypeInfo.of(adapterProject().packageId(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return basePackageInfo();
        }

        // Dagger resources scope (passthrough from AdapterProject)

        default TypeInfo baseResourcesScope() { return adapterProject().baseResourcesScope(); }

        default TypeInfo resourcesScope() { return adapterProject().resourcesScope(); }

        // Dagger resources component

        @Value.Default default TypeInfo baseResourcesComponent() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "ResourcesComponent");
        }

        Optional<TypeInfo> extendResourcesComponent();

        default TypeInfo resourcesComponent() {
            return extendResourcesComponent().orElseGet(this::baseResourcesComponent);
        }

        default TypeInfo daggerResourcesComponent() {
            return TypeInfo.of(resourcesComponent().packageId(), "Dagger" + resourcesComponent().id());
        }

        // Dagger resources module

        @Value.Default default TypeInfo baseResourcesModule() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "ResourcesModule");
        }

        Optional<TypeInfo> extendResourcesModule();

        default TypeInfo resourcesModule() {
            return extendResourcesModule().orElseGet(this::baseResourcesModule);
        }

        // Dagger Scope (passthrough from AdapterProject)

        default TypeInfo baseScope() { return adapterProject().baseScope(); }

        default TypeInfo scope() { return adapterProject().scope(); }

        // Dagger Qualifier (passthrough from AdapterProject)

        default TypeInfo baseQualifier() { return adapterProject().baseQualifier(); }

        default TypeInfo qualifier() { return adapterProject().qualifier(); }

        // Dagger component

        @Value.Default default TypeInfo baseComponent() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "Component");
        }

        Optional<TypeInfo> extendComponent();

        default TypeInfo component() {
            return extendComponent().orElseGet(this::baseComponent);
        }

        default TypeInfo daggerComponent() {
            return TypeInfo.of(component().packageId(), "Dagger" + component().id());
        }

        // Dagger module

        @Value.Default default TypeInfo baseModule() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "Module");
        }

        Optional<TypeInfo> extendModule();

        default TypeInfo module() {
            return extendModule().orElseGet(this::baseModule);
        }

        // Language instance

        @Value.Default default TypeInfo baseInstance() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "Instance");
        }

        Optional<TypeInfo> extendInstance();

        default TypeInfo instance() {
            return extendInstance().orElseGet(this::baseInstance);
        }


        /// Adapter project task definitions

        // Check task definition

        @Value.Default default TypeInfo baseCheckTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Check");
        }

        Optional<TypeInfo> extendCheckTaskDef();

        default TypeInfo checkTaskDef() {
            return extendCheckTaskDef().orElseGet(this::baseCheckTaskDef);
        }

        // Multi-file check task definition

        @Value.Default default TypeInfo baseCheckMultiTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "CheckMulti");
        }

        Optional<TypeInfo> extendCheckMultiTaskDef();

        default TypeInfo checkMultiTaskDef() {
            return extendCheckMultiTaskDef().orElseGet(this::baseCheckMultiTaskDef);
        }

        // Single file check results aggregator task definition

        @Value.Default default TypeInfo baseCheckAggregatorTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "CheckAggregator");
        }

        Optional<TypeInfo> extendCheckAggregatorTaskDef();

        default TypeInfo checkAggregatorTaskDef() {
            return extendCheckAggregatorTaskDef().orElseGet(this::baseCheckAggregatorTaskDef);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ArrayList<ResourcePath> javaSourcePaths() {
            final ArrayList<ResourcePath> sourcePaths = new ArrayList<>();
            sourcePaths.add(generatedJavaSourcesDirectory());
            return sourcePaths;
        }

        default ArrayList<ResourcePath> javaSourceFiles() {
            final ArrayList<ResourcePath> javaSourceFiles = new ArrayList<>();
            if(classKind().isGenerating()) {
                final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
                if(languageProjectDependency().isSome()) {
                    // Only generate package-info.java if the language project is a separate project. Otherwise we will have
                    // two package-info.java files in the same package, which is an error.
                    javaSourceFiles.add(basePackageInfo().file(generatedJavaSourcesDirectory));
                }
                javaSourceFiles.add(baseResourcesScope().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseResourcesComponent().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseResourcesModule().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseScope().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseQualifier().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseComponent().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseModule().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseInstance().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseCheckTaskDef().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseCheckMultiTaskDef().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseCheckAggregatorTaskDef().file(generatedJavaSourcesDirectory));
                for(CommandDefRepr commandDef : commandDefs()) {
                    javaSourceFiles.add(commandDef.type().file(generatedJavaSourcesDirectory));
                }
            }
            parser().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            styler().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            completer().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            strategoRuntime().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            constraintAnalyzer().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            multilangAnalyzer().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            return javaSourceFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();
    }
}
