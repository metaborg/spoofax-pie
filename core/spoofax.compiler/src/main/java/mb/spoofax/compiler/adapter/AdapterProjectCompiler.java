package mb.spoofax.compiler.adapter;

import com.samskivert.mustache.Mustache;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.common.BlockCommentSymbols;
import mb.spoofax.common.BracketSymbols;
import mb.spoofax.compiler.adapter.data.AutoCommandRequestRepr;
import mb.spoofax.compiler.adapter.data.CliCommandRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.MenuItemRepr;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.MenuItemCollection;
import mb.spoofax.compiler.util.NamedTypeInfo;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.compiler.util.TypeInfoCollection;
import mb.spoofax.compiler.util.UniqueNamer;
import mb.spoofax.core.language.taskdef.NoneHoverTaskDef;
import mb.spoofax.core.language.taskdef.NoneResolveTaskDef;
import mb.spoofax.core.language.taskdef.NoneStyler;
import mb.spoofax.core.language.taskdef.NoneTokenizer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Value.Enclosing
public class AdapterProjectCompiler implements TaskDef<Supplier<Result<AdapterProjectCompiler.Input, ?>>, Result<None, ?>> {
    private final TemplateWriter packageInfoTemplate;
    private final TemplateWriter checkTaskDefTemplate;
    private final TemplateWriter checkMultiTaskDefTemplate;
    private final TemplateWriter checkAggregatorTaskDefTemplate;
    private final TemplateWriter checkDeaggregatorTaskDefTemplate;
    private final TemplateWriter resourcesScopeTemplate;
    private final TemplateWriter resourcesComponentTemplate;
    private final TemplateWriter resourcesModuleTemplate;
    private final TemplateWriter scopeTemplate;
    private final TemplateWriter qualifierTemplate;
    private final TemplateWriter componentTemplate;
    private final TemplateWriter moduleTemplate;
    private final TemplateWriter instanceTemplate;
    private final TemplateWriter participantTemplate;
    private final TemplateWriter commandDefTemplate;
    private final TemplateWriter testStrategoTaskDef;

    private final ExportsCompiler exportsCompiler;
    private final ParserAdapterCompiler parserCompiler;
    private final StylerAdapterCompiler stylerCompiler;
    private final StrategoRuntimeAdapterCompiler strategoRuntimeCompiler;
    private final ConstraintAnalyzerAdapterCompiler constraintAnalyzerCompiler;
    private final MultilangAnalyzerAdapterCompiler multilangAnalyzerCompiler;
    private final CodeCompletionAdapterCompiler codeCompletionCompiler;
    private final TegoRuntimeAdapterCompiler tegoRuntimeCompiler;
    private final ReferenceResolutionAdapterCompiler referenceResolutionAdapterCompiler;
    private final DynamixAdapterCompiler dynamixAdapterCompiler;
    private final HoverAdapterCompiler hoverAdapterCompiler;
    private final GetSourceFilesAdapterCompiler getSourceFilesAdapterCompiler;

    @Inject public AdapterProjectCompiler(
        TemplateCompiler templateCompiler,
        ExportsCompiler exportsCompiler,
        ParserAdapterCompiler parserCompiler,
        StylerAdapterCompiler stylerCompiler,
        StrategoRuntimeAdapterCompiler strategoRuntimeCompiler,
        ConstraintAnalyzerAdapterCompiler constraintAnalyzerCompiler,
        MultilangAnalyzerAdapterCompiler multilangAnalyzerCompiler,
        CodeCompletionAdapterCompiler codeCompletionCompiler,
        TegoRuntimeAdapterCompiler tegoRuntimeCompiler,
        ReferenceResolutionAdapterCompiler referenceResolutionAdapterCompiler,
        DynamixAdapterCompiler dynamixAdapterCompiler,
        HoverAdapterCompiler hoverAdapterCompiler,
        GetSourceFilesAdapterCompiler getSourceFilesAdapterCompiler
    ) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("adapter_project/package-info.java.mustache");
        this.checkTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckTaskDef.java.mustache");
        this.checkMultiTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckMultiTaskDef.java.mustache");
        this.checkAggregatorTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckAggregatorTaskDef.java.mustache");
        this.checkDeaggregatorTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckDeaggregatorTaskDef.java.mustache");
        this.resourcesScopeTemplate = templateCompiler.getOrCompileToWriter("adapter_project/ResourcesScope.java.mustache");
        this.resourcesComponentTemplate = templateCompiler.getOrCompileToWriter("adapter_project/ResourcesComponent.java.mustache");
        this.resourcesModuleTemplate = templateCompiler.getOrCompileToWriter("adapter_project/ResourcesModule.java.mustache");
        this.scopeTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Scope.java.mustache");
        this.qualifierTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Qualifier.java.mustache");
        this.componentTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Component.java.mustache");
        this.moduleTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Module.java.mustache");
        this.instanceTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Instance.java.mustache");
        this.participantTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Participant.java.mustache");
        this.commandDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CommandDef.java.mustache");
        this.testStrategoTaskDef = templateCompiler.getOrCompileToWriter("adapter_project/TestStrategoTaskDef.java.mustache");

        this.exportsCompiler = exportsCompiler;
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
        this.multilangAnalyzerCompiler = multilangAnalyzerCompiler;
        this.codeCompletionCompiler = codeCompletionCompiler;
        this.tegoRuntimeCompiler = tegoRuntimeCompiler;
        this.referenceResolutionAdapterCompiler = referenceResolutionAdapterCompiler;
        this.dynamixAdapterCompiler = dynamixAdapterCompiler;
        this.hoverAdapterCompiler = hoverAdapterCompiler;
        this.getSourceFilesAdapterCompiler = getSourceFilesAdapterCompiler;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<None, ?> exec(ExecContext context, Supplier<Result<Input, ?>> input) throws IOException {
        return context.require(input).mapThrowing(i -> compile(context, i));
    }

    @Override public boolean shouldExecWhenAffected(Supplier<Result<Input, ?>> input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public None compile(ExecContext context, Input input) throws IOException {
        // Files from other compilers.
        exportsCompiler.compile(context, input.exports());
        Option.ofOptional(input.parser()).ifSomeThrowing((i) -> parserCompiler.compile(context, i));
        Option.ofOptional(input.styler()).ifSomeThrowing((i) -> stylerCompiler.compile(context, i));
        Option.ofOptional(input.strategoRuntime()).ifSomeThrowing((i) -> strategoRuntimeCompiler.compile(context, i));
        Option.ofOptional(input.constraintAnalyzer()).ifSomeThrowing((i) -> constraintAnalyzerCompiler.compile(context, i));
        Option.ofOptional(input.multilangAnalyzer()).ifSomeThrowing((i) -> multilangAnalyzerCompiler.compile(context, i));
        Option.ofOptional(input.codeCompletion()).ifSomeThrowing((i) -> codeCompletionCompiler.compile(context, i));
        Option.ofOptional(input.tegoRuntime()).ifSomeThrowing((i) -> tegoRuntimeCompiler.compile(context, i));
        Option.ofOptional(input.referenceResolution()).ifSomeThrowing((i) -> referenceResolutionAdapterCompiler.compile(context, i));
        Option.ofOptional(input.hover()).ifSomeThrowing((i) -> hoverAdapterCompiler.compile(context, i));
        Option.ofOptional(input.dynamix()).ifSomeThrowing((i) -> dynamixAdapterCompiler.compile(context, i));
        getSourceFilesAdapterCompiler.compile(context, input.getSourceFiles());

        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.

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
        checkDeaggregatorTaskDefTemplate.write(context, input.baseCheckDeaggregatorTaskDef().file(generatedJavaSourcesDirectory), input);

        resourcesScopeTemplate.write(context, input.baseResourcesScope().file(generatedJavaSourcesDirectory), input);
        resourcesComponentTemplate.write(context, input.baseResourcesComponent().file(generatedJavaSourcesDirectory), input);
        resourcesModuleTemplate.write(context, input.baseResourcesModule().file(generatedJavaSourcesDirectory), input);

        scopeTemplate.write(context, input.adapterProject().baseScope().file(generatedJavaSourcesDirectory), input);
        qualifierTemplate.write(context, input.baseQualifier().file(generatedJavaSourcesDirectory), input);
        participantTemplate.write(context, input.baseParticipant().file(generatedJavaSourcesDirectory), input);

        for(CommandDefRepr commandDef : input.allCommandDefs()) {
            final UniqueNamer uniqueNamer = new UniqueNamer(input.scope(), input.qualifier());
            final HashMap<String, Object> map = new HashMap<>();
            map.put("scope", input.scope());
            map.put("taskDefInjection", uniqueNamer.makeUnique(commandDef.taskDefType()));
            commandDefTemplate.write(context, commandDef.type().file(generatedJavaSourcesDirectory), commandDef, map);
        }

        if(input.strategoRuntime().isPresent() && input.parser().isPresent()) {
            final HashMap<String, Object> map = new HashMap<>();
            if(input.constraintAnalyzer().isPresent()) {
                map.put("taskInput", TypeInfo.of("mb.constraint.pie", "ConstraintAnalyzeTaskDef.Output"));
                map.put("astMember", ".result.analyzedAst");
            } else {
                map.put("taskInput", TypeInfo.of("org.spoofax.interpreter.terms", "IStrategoTerm"));
                map.put("astMember", "");
            }
            testStrategoTaskDef.write(context, input.baseTestStrategoTaskDef().file(generatedJavaSourcesDirectory), input, map);
        }

        { // Component
            final UniqueNamer uniqueNamer = new UniqueNamer(input.scope(), input.qualifier());
            final HashMap<String, Object> map = new HashMap<>();
            map.put("providedTaskDefs", input.allTaskDefs().stream().map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            map.put("providedCommandDefs", input.allCommandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            componentTemplate.write(context, input.baseComponent().file(generatedJavaSourcesDirectory), input, map);
        }

        { // Module
            final UniqueNamer uniqueNamer = new UniqueNamer(input.scope(), input.qualifier());
            final HashMap<String, Object> map = new HashMap<>();
            map.put("providedTaskDefs", input.allTaskDefs().stream().map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedCommandDefs", input.allCommandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedAutoCommandDefs", input.autoCommandDefs().stream().map((c) -> uniqueNamer.makeUnique(c, c.commandDef().asVariableId())).collect(Collectors.toList()));
            moduleTemplate.write(context, input.baseModule().file(generatedJavaSourcesDirectory), input, map);
        }

        { // Instance
            final UniqueNamer uniqueNamer = new UniqueNamer(input.scope(), input.qualifier());
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
            final NamedTypeInfo checkOneInjection;
            if(input.multilangAnalyzer().isPresent()) { // isMultiLang will be true
                map.put("languageId", input.multilangAnalyzer().get().languageId());
                map.put("contextId", input.multilangAnalyzer().get().contextId());
            }
            if(input.multilangAnalyzer().isPresent()) {
                checkInjection = uniqueNamer.makeUnique(input.multilangAnalyzer().get().checkTaskDef());
                checkOneInjection = uniqueNamer.makeUnique(input.checkDeaggregatorTaskDef());
            } else if(input.isMultiFile()) {
                checkInjection = uniqueNamer.makeUnique(input.checkMultiTaskDef());
                checkOneInjection = uniqueNamer.makeUnique(input.checkDeaggregatorTaskDef());
            } else {
                checkInjection = uniqueNamer.makeUnique(input.checkAggregatorTaskDef());
                checkOneInjection = uniqueNamer.makeUnique(input.checkTaskDef());
            }
            injected.add(checkInjection);
            injected.add(checkOneInjection);
            map.put("checkInjection", checkInjection);
            map.put("checkOneInjection", checkOneInjection);

            final NamedTypeInfo styleInjection;
            if(input.styler().isPresent()) {
                styleInjection = uniqueNamer.makeUnique(input.styler().get().styleTaskDef());
            } else {
                styleInjection = uniqueNamer.makeUnique(TypeInfo.of(NoneStyler.class));
            }
            map.put("styleInjection", styleInjection);
            injected.add(styleInjection);

            final Optional<NamedTypeInfo> codeCompletionInjection;
            if(input.codeCompletion().isPresent() && input.parser().isPresent()) {
                final NamedTypeInfo namedTypeInfo = uniqueNamer.makeUnique(input.codeCompletion().get().codeCompletionTaskDef());
                injected.add(namedTypeInfo);
                codeCompletionInjection = Optional.of(namedTypeInfo);
            } else {
                codeCompletionInjection = Optional.empty();
            }
            map.put("codeCompletionInjection", codeCompletionInjection);

            final NamedTypeInfo resolveInjection;
            if(input.referenceResolution().isPresent()) {
                resolveInjection = uniqueNamer.makeUnique(input.referenceResolution().get().resolveTaskDef());
                map.put("hasResolveInjection", true);
            } else {
                resolveInjection = uniqueNamer.makeUnique(TypeInfo.of(NoneResolveTaskDef.class));
                map.put("hasResolveInjection", false);
            }
            map.put("resolveInjection", resolveInjection);
            injected.add(resolveInjection);

            final NamedTypeInfo hoverInjection;
            if(input.hover().isPresent()) {
                hoverInjection = uniqueNamer.makeUnique(input.hover().get().hoverTaskDef());
                map.put("hasHoverInjection", true);
            } else {
                hoverInjection = uniqueNamer.makeUnique(TypeInfo.of(NoneHoverTaskDef.class));
                map.put("hasHoverInjection", false);
            }
            map.put("hoverInjection", hoverInjection);
            injected.add(hoverInjection);

            final NamedTypeInfo resourceExportsInjection = uniqueNamer.makeUnique(input.exports().resourceExportsClass());
            map.put("resourceExportsInjection", resourceExportsInjection);
            injected.add(resourceExportsInjection);

            // Create injections for all command definitions. TODO: only inject needed command definitions?
            injected.addAll(input.allCommandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            // Provide a lambda that gets the name of the injected command definition from the context.
            map.put("getInjectedCommandDef", (Mustache.Lambda)(frag, out) -> {
                final TypeInfo type = (TypeInfo)frag.context();
                final @Nullable String name = uniqueNamer.getNameFor(type);
                if(name == null) {
                    throw new IllegalStateException("Cannot get injected command definition for type '" + type + "'");
                }
                out.write(name);
            });

            // Injections for testing language features with SPT.
            if(input.strategoRuntime().isPresent() && input.parser().isPresent()) {
                final NamedTypeInfo testStrategoInjection = uniqueNamer.makeUnique(input.testStrategoTaskDef());
                map.put("testStrategoInjection", testStrategoInjection);
                injected.add(testStrategoInjection);
            } else {
                map.put("testStrategoInjection", false);
            }
            if(input.constraintAnalyzer().isPresent() && input.parser().isPresent()) {
                final NamedTypeInfo analyzeInjection = uniqueNamer.makeUnique(input.constraintAnalyzer().get().analyzeTaskDef());
                map.put("analyzeInjection", analyzeInjection);
                injected.add(analyzeInjection);
            } else {
                map.put("analyzeInjection", false);
            }

            // Collect all menus.
            final MenuItemCollection allMenuItems = new MenuItemCollection(input.mainMenuItems(), input.resourceContextMenuItems(), input.editorContextMenuItems());
            input.parser().ifPresent(i -> i.collectMenus(allMenuItems));
            input.constraintAnalyzer().ifPresent(i -> i.collectMenus(allMenuItems));
            input.dynamix().ifPresent(i -> i.collectMenus(allMenuItems));
            // Deduplicate menu items.
            allMenuItems.deduplicateMenus();
            // Then add to map.
            map.put("allMainMenuItems", allMenuItems.getMainMenuItems());
            map.put("allResourceContextMenuItems", allMenuItems.getResourceContextMenuItems());
            map.put("allEditorContextMenuItems", allMenuItems.getEditorContextMenuItems());

            instanceTemplate.write(context, input.baseInstance().file(generatedJavaSourcesDirectory), input, map);
        }

        return None.instance;
    }

    private void addTaskDef(Collection<TypeInfo> taskDefs, TypeInfo taskDef, TypeInfo base) {
        taskDefs.add(taskDef);
        if(!taskDef.equals(base)) {
            taskDefs.add(base);
        }
    }


    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.apiPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessorPlatform(shared.spoofaxDependencyConstraintsDep()));
        input.languageProjectDependency().ifSome((d) -> dependencies.add(GradleConfiguredDependency.api(d)));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCommonDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCoreDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.pieApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.daggerDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
        input.parser().ifPresent((i) -> parserCompiler.getDependencies(i).addAllTo(dependencies));
        input.constraintAnalyzer().ifPresent((i) -> constraintAnalyzerCompiler.getDependencies(i).addAllTo(dependencies));
        input.strategoRuntime().ifPresent((i) -> strategoRuntimeCompiler.getDependencies(i).addAllTo(dependencies));
        input.tegoRuntime().ifPresent((i) -> tegoRuntimeCompiler.getDependencies(i).addAllTo(dependencies));
        input.codeCompletion().ifPresent((i) -> codeCompletionCompiler.getDependencies(i).addAllTo(dependencies));
        input.dynamix().ifPresent((i) -> dynamixAdapterCompiler.getDependencies(i).addAllTo(dependencies));
        dependencies.add(GradleConfiguredDependency.api(shared.sptApiDep()));
        if(input.dependOnRv32Im()) {
            dependencies.add(GradleConfiguredDependency.api(shared.rv32ImDep()));
        }
        return dependencies;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends AdapterProjectCompilerData.Input.Builder {
        }

        static Builder builder() {
            return new Builder();
        }


        /// Project

        AdapterProject adapterProject();


        /// Sub-inputs

        ClassLoaderResourcesCompiler.Input classLoaderResources();

        ExportsCompiler.Input exports();

        Optional<ParserAdapterCompiler.Input> parser();

        Optional<StylerAdapterCompiler.Input> styler();

        Optional<StrategoRuntimeAdapterCompiler.Input> strategoRuntime();

        Optional<ConstraintAnalyzerAdapterCompiler.Input> constraintAnalyzer();

        Optional<MultilangAnalyzerAdapterCompiler.Input> multilangAnalyzer();

        Optional<CodeCompletionAdapterCompiler.Input> codeCompletion();

        Optional<ReferenceResolutionAdapterCompiler.Input> referenceResolution();

        Optional<TegoRuntimeAdapterCompiler.Input> tegoRuntime();

        Optional<DynamixAdapterCompiler.Input> dynamix();

        Optional<HoverAdapterCompiler.Input> hover();

        GetSourceFilesAdapterCompiler.Input getSourceFiles();


        /// Configuration

        Optional<String> compositionGroup();

        /* None indicates that the language project is the same project as the adapter project */
        Option<GradleDependency> languageProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();

        List<TypeInfo> additionalModules();

        List<TypeInfo> additionalResourcesModules();


        List<TypeInfo> taskDefs();

        @Value.Lazy default TypeInfoCollection allTaskDefs() {
            final TypeInfoCollection taskDefs = new TypeInfoCollection(taskDefs());
            if(parser().isPresent()) {
                parser().get().collectTaskDefs(taskDefs);
            } else {
                taskDefs.add(TypeInfo.of(NoneTokenizer.class));
            }
            if(styler().isPresent()) {
                final StylerAdapterCompiler.Input i = styler().get();
                taskDefs.add(i.styleTaskDef(), i.baseStyleTaskDef());
            } else {
                taskDefs.add(TypeInfo.of(NoneStyler.class));
            }
            strategoRuntime().ifPresent((i) -> {
                taskDefs.add(i.getStrategoRuntimeProviderTaskDef(), i.baseGetStrategoRuntimeProviderTaskDef());
            });
            constraintAnalyzer().ifPresent((i) -> {
                i.collectTaskDefs(taskDefs);
            });
            multilangAnalyzer().ifPresent((i) -> {
                taskDefs.add(i.analyzeTaskDef(), i.baseAnalyzeTaskDef());
                taskDefs.add(i.indexAstTaskDef(), i.baseIndexAstTaskDef());
                taskDefs.add(i.preStatixTaskDef(), i.basePreStatixTaskDef());
                taskDefs.add(i.postStatixTaskDef(), i.basePostStatixTaskDef());
                taskDefs.add(i.checkTaskDef(), i.baseCheckTaskDef());
                taskDefs.addAll(i.libraryTaskDefs());
            });
            codeCompletion().ifPresent((i) -> {
                taskDefs.add(i.codeCompletionTaskDef(), i.baseCodeCompletionTaskDef());
                taskDefs.add(i.statixSpecTaskDef(), i.baseStatixSpecTaskDef());
            });
            referenceResolution().ifPresent((i) -> {
                taskDefs.add(i.resolveTaskDef(), i.baseResolveTaskDef());
            });
            if(!referenceResolution().isPresent()) {
                taskDefs.add(TypeInfo.of(NoneResolveTaskDef.class));
            }
            hover().ifPresent((i) -> {
                taskDefs.add(i.hoverTaskDef(), i.baseHoverTaskDef());
            });
            dynamix().ifPresent((i) -> {
                i.collectTaskDefs(taskDefs);
            });
            if(!hover().isPresent()) {
                taskDefs.add(TypeInfo.of(NoneHoverTaskDef.class));
            }
            taskDefs.add(checkTaskDef(), baseCheckTaskDef());
            taskDefs.add(checkMultiTaskDef(), baseCheckMultiTaskDef());
            taskDefs.add(checkAggregatorTaskDef(), baseCheckAggregatorTaskDef());
            taskDefs.add(checkDeaggregatorTaskDef(), baseCheckDeaggregatorTaskDef());

            taskDefs.add(getSourceFiles().getSourceFilesTaskDef(), getSourceFiles().baseGetSourceFilesTaskDef());

            if(strategoRuntime().isPresent() && parser().isPresent()) {
                taskDefs.add(testStrategoTaskDef(), baseTestStrategoTaskDef());
            }
            return taskDefs;
        }


        List<CommandDefRepr> commandDefs();

        @Value.Lazy default ArrayList<CommandDefRepr> allCommandDefs() {
            final ArrayList<CommandDefRepr> commandDefs = new ArrayList<>(commandDefs());
            parser().ifPresent(i -> i.collectCommands(commandDefs));
            constraintAnalyzer().ifPresent((i) -> i.collectCommands(commandDefs));
            dynamix().ifPresent((i) -> i.collectCommands(commandDefs));
            return commandDefs;
        }

        List<AutoCommandRequestRepr> autoCommandDefs();


        @Value.Default default CliCommandRepr cliCommand() {
            return CliCommandRepr.builder().name(shared().name()).build();
        }


        @Value.Default default List<MenuItemRepr> mainMenuItems() {
            return editorContextMenuItems();
        }

        List<MenuItemRepr> resourceContextMenuItems();

        List<MenuItemRepr> editorContextMenuItems();


        List<String> lineCommentSymbols();

        List<BlockCommentSymbols> blockCommentSymbols();

        List<BracketSymbols> bracketSymbols();


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

        @Value.Lazy default List<NamedTypeInfo> checkInjections() {
            ArrayList<NamedTypeInfo> results = new ArrayList<>();
            results.add(NamedTypeInfo.of("classLoaderResources", classLoaderResources().classLoaderResources()));
            parser().ifPresent(i -> results.add(NamedTypeInfo.of("parse", i.parseTaskDef())));
            constraintAnalyzer().ifPresent(i -> results.add(NamedTypeInfo.of("analyze", i.analyzeTaskDef())));
            return results;
        }

        @Value.Lazy default List<NamedTypeInfo> checkMultiInjections() {
            ArrayList<NamedTypeInfo> results = new ArrayList<>();
            results.add(NamedTypeInfo.of("classLoaderResources", classLoaderResources().classLoaderResources()));
            results.add(NamedTypeInfo.of("getSourceFiles", getSourceFiles().getSourceFilesTaskDef()));
            parser().ifPresent(i -> results.add(NamedTypeInfo.of("parse", i.parseTaskDef())));
            constraintAnalyzer().ifPresent(i -> results.add(NamedTypeInfo.of("analyze", i.analyzeMultiTaskDef())));
            return results;
        }

        @Value.Default default boolean dependOnRv32Im() {
            return false;
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

        default TypeInfo baseResourcesScope() {
            return adapterProject().baseResourcesScope();
        }

        default TypeInfo resourcesScope() {
            return adapterProject().resourcesScope();
        }

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

        default TypeInfo baseScope() {
            return adapterProject().baseScope();
        }

        default TypeInfo scope() {
            return adapterProject().scope();
        }

        // Dagger Qualifier (passthrough from AdapterProject)

        default TypeInfo baseQualifier() {
            return adapterProject().baseQualifier();
        }

        default TypeInfo qualifier() {
            return adapterProject().qualifier();
        }

        // Dagger component

        @Value.Default default boolean addComponentAnnotationToBaseComponent() {
            return true;
        }

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

        // Participant

        @Value.Default default TypeInfo baseParticipant() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "Participant");
        }

        Optional<TypeInfo> extendParticipant();

        default TypeInfo participant() {
            return extendParticipant().orElseGet(this::baseParticipant);
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

        // Multi file check results deaggregator task definition

        @Value.Default default TypeInfo baseCheckDeaggregatorTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "CheckDeaggregator");
        }

        Optional<TypeInfo> extendCheckDeaggregatorTaskDef();

        default TypeInfo checkDeaggregatorTaskDef() {
            return extendCheckDeaggregatorTaskDef().orElseGet(this::baseCheckDeaggregatorTaskDef);
        }

        // Stratego strategy SPT test task definition

        @Value.Default default TypeInfo baseTestStrategoTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "TestStrategoTaskDef");
        }

        Optional<TypeInfo> extendTestStrategoTaskDef();

        default TypeInfo testStrategoTaskDef() {
            return extendTestStrategoTaskDef().orElseGet(this::baseTestStrategoTaskDef);
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
                javaSourceFiles.add(baseParticipant().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseCheckTaskDef().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseCheckMultiTaskDef().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseCheckAggregatorTaskDef().file(generatedJavaSourcesDirectory));
                javaSourceFiles.add(baseCheckDeaggregatorTaskDef().file(generatedJavaSourcesDirectory));

                if(strategoRuntime().isPresent() && parser().isPresent()) {
                    javaSourceFiles.add(baseTestStrategoTaskDef().file(generatedJavaSourcesDirectory));
                }

                for(CommandDefRepr commandDef : allCommandDefs()) {
                    javaSourceFiles.add(commandDef.type().file(generatedJavaSourcesDirectory));
                }
            }
            exports().javaSourceFiles().addAllTo(javaSourceFiles);
            parser().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            styler().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            strategoRuntime().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            constraintAnalyzer().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            referenceResolution().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            multilangAnalyzer().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            codeCompletion().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            tegoRuntime().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            dynamix().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            hover().ifPresent((i) -> i.javaSourceFiles().addAllTo(javaSourceFiles));
            getSourceFiles().javaSourceFiles().addAllTo(javaSourceFiles);
            return javaSourceFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();
    }
}
