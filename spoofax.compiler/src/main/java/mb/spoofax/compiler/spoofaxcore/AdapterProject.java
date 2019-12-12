package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.cli.CliCommandRepr;
import mb.spoofax.compiler.command.AutoCommandDefRepr;
import mb.spoofax.compiler.command.CommandDefRepr;
import mb.spoofax.compiler.menu.MenuItemRepr;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.NamedTypeInfo;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.compiler.util.UniqueNamer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class AdapterProject {
    private final Template buildGradleTemplate;
    private final Template settingsGradleTemplate;
    private final Template checkTaskDefTemplate;
    private final Template componentTemplate;
    private final Template moduleTemplate;
    private final Template instanceTemplate;
    private final Template commandDefTemplate;
    private final ResourceService resourceService;
    private final Charset charset;
    private final Parser parserCompiler;
    private final Styler stylerCompiler;
    private final StrategoRuntime strategoRuntimeCompiler;
    private final ConstraintAnalyzer constraintAnalyzerCompiler;

    private AdapterProject(
        Template buildGradleTemplate,
        Template settingsGradleTemplate,
        Template checkTaskDefTemplate,
        Template componentTemplate,
        Template moduleTemplate,
        Template instanceTemplate,
        Template commandDefTemplate,
        ResourceService resourceService,
        Charset charset,
        Parser parserCompiler,
        Styler stylerCompiler,
        StrategoRuntime strategoRuntimeCompiler,
        ConstraintAnalyzer constraintAnalyzerCompiler
    ) {
        this.settingsGradleTemplate = settingsGradleTemplate;
        this.componentTemplate = componentTemplate;
        this.moduleTemplate = moduleTemplate;
        this.instanceTemplate = instanceTemplate;
        this.checkTaskDefTemplate = checkTaskDefTemplate;
        this.commandDefTemplate = commandDefTemplate;
        this.resourceService = resourceService;
        this.buildGradleTemplate = buildGradleTemplate;
        this.charset = charset;
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
    }

    public static AdapterProject fromClassLoaderResources(
        ResourceService resourceService,
        Charset charset,
        Parser parserCompiler,
        Styler stylerCompiler,
        StrategoRuntime strategoRuntimeCompiler,
        ConstraintAnalyzer constraintAnalyzerCompiler
    ) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(AdapterProject.class);
        return new AdapterProject(
            templateCompiler.getOrCompile("adapter_project/build.gradle.kts.mustache"),
            templateCompiler.getOrCompile("gradle_project/settings.gradle.kts.mustache"),
            templateCompiler.getOrCompile("adapter_project/CheckTaskDef.java.mustache"),
            templateCompiler.getOrCompile("adapter_project/Component.java.mustache"),
            templateCompiler.getOrCompile("adapter_project/Module.java.mustache"),
            templateCompiler.getOrCompile("adapter_project/Instance.java.mustache"),
            templateCompiler.getOrCompile("adapter_project/CommandDef.java.mustache"),
            resourceService,
            charset,
            parserCompiler,
            stylerCompiler,
            strategoRuntimeCompiler,
            constraintAnalyzerCompiler
        );
    }


    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        resourceService.getHierarchicalResource(input.gradleGenDirectory()).ensureDirectoryExists();

        // build.gradle.kts
        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.buildGradleKtsFile()), charset)) {
            final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
            dependencies.add(GradleConfiguredDependency.api(input.languageProjectDependency()));
            dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCoreDep()));
            dependencies.add(GradleConfiguredDependency.api(shared.pieApiDep()));
            dependencies.add(GradleConfiguredDependency.api(shared.pieDaggerDep()));
            dependencies.add(GradleConfiguredDependency.api(shared.daggerDep()));
            dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
            dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
            final HashMap<String, Object> map = new HashMap<>();
            map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            buildGradleTemplate.execute(input, map, writer);
            writer.flush();
        }

        try {
            // settings.gradle.kts (if present)
            input.settingsGradleKtsFile().ifPresent((f) -> {
                try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(f), charset)) {
                    settingsGradleTemplate.execute(input, writer);
                    writer.flush();
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        // Run adapter project compilers
        final Parser.AdapterProjectOutput parserOutput = parserCompiler.compileAdapterProject(input.parser());
        final Optional<Styler.AdapterProjectOutput> stylerOutput;
        final Optional<StrategoRuntime.AdapterProjectOutput> strategoRuntimeOutput;
        final Optional<ConstraintAnalyzer.AdapterProjectOutput> constraintAnalyzerOutput;
        try {
            stylerOutput = input.styler().map((i) -> {
                try {
                    return stylerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            strategoRuntimeOutput = input.strategoRuntime().map((i) -> {
                try {
                    return strategoRuntimeCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            constraintAnalyzerOutput = input.constraintAnalyzer().map((i) -> {
                try {
                    return constraintAnalyzerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        resourceService.getHierarchicalResource(classesGenDirectory).ensureDirectoryExists();

        // *CheckTaskDef.java
        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.checkTaskDef().file(classesGenDirectory)).createParents(), charset)) {
            checkTaskDefTemplate.execute(input, writer);
            writer.flush();
        }

        // *Component.java
        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genComponent().file(classesGenDirectory)).createParents(), charset)) {
            componentTemplate.execute(input, writer);
            writer.flush();
        }

        // *CommandDef.java
        for(CommandDefRepr commandDef : input.commandDefs()) {
            try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(commandDef.type().file(classesGenDirectory)).createParents(), charset)) {
                final UniqueNamer uniqueNamer = new UniqueNamer();
                final HashMap<String, Object> map = new HashMap<>();
                map.put("taskDefInjection", uniqueNamer.makeUnique(commandDef.taskDefType()));
                commandDefTemplate.execute(commandDef, map, writer);
                writer.flush();
            }
        }

        // Collect all task definitions.
        final ArrayList<TypeInfo> allTaskDefs = new ArrayList<>(input.taskDefs());
        allTaskDefs.add(input.parser().tokenizeTaskDef());
        allTaskDefs.addAll(parserOutput.additionalTaskDefs());
        if(input.styler().isPresent()) {
            allTaskDefs.add(input.styler().get().styleTaskDef());
        } else {
            allTaskDefs.add(TypeInfo.of("mb.spoofax.core.language.taskdef", "NullStyler"));
        }
        stylerOutput.ifPresent(o -> allTaskDefs.addAll(o.additionalTaskDefs()));
        strategoRuntimeOutput.ifPresent(o -> allTaskDefs.addAll(o.additionalTaskDefs()));
        constraintAnalyzerOutput.ifPresent(o -> allTaskDefs.addAll(o.additionalTaskDefs()));
        allTaskDefs.add(input.checkTaskDef());

        // *Module.java
        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genModule().file(classesGenDirectory)).createParents(), charset)) {
            final UniqueNamer uniqueNamer = new UniqueNamer();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("providedTaskDefs", allTaskDefs.stream().map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedCommandDefs", input.commandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedAutoCommandDefs", input.autoCommandDefs().stream().map((c) -> uniqueNamer.makeUnique(c, c.commandDef().asVariableId())).collect(Collectors.toList()));
            moduleTemplate.execute(input, map, writer);
            writer.flush();
        }

        // *Instance.java
        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genInstance().file(classesGenDirectory)).createParents(), charset)) {
            final UniqueNamer uniqueNamer = new UniqueNamer();
            uniqueNamer.reserve("fileExtensions");
            uniqueNamer.reserve("commandDefs");
            uniqueNamer.reserve("autoCommandDefs");
            final HashMap<String, Object> map = new HashMap<>();

            // Collect all injections.
            final ArrayList<NamedTypeInfo> injected = new ArrayList<>();
            map.put("injected", injected);

            // Create injections for tasks required in the language instance.
            final NamedTypeInfo tokenizeInjection = uniqueNamer.makeUnique(input.parser().tokenizeTaskDef());
            map.put("tokenizeInjection", tokenizeInjection);
            injected.add(tokenizeInjection);
            final NamedTypeInfo checkInjection = uniqueNamer.makeUnique(input.checkTaskDef());
            injected.add(checkInjection);
            map.put("checkInjection", checkInjection);
            final NamedTypeInfo styleInjection;
            if(input.styler().isPresent()) {
                styleInjection = uniqueNamer.makeUnique(input.styler().get().styleTaskDef());
            } else {
                styleInjection = uniqueNamer.makeUnique(TypeInfo.of("mb.spoofax.core.language.taskdef", "NullStyler"));
            }
            map.put("styleInjection", styleInjection);
            injected.add(styleInjection);

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

            instanceTemplate.execute(input, map, writer);
            writer.flush();
        }

        return Output.builder()
            .parser(parserOutput)
            .styler(stylerOutput)
            .strategoRuntime(strategoRuntimeOutput)
            .constraintAnalyzer(constraintAnalyzerOutput)
            .build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends AdapterProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        Parser.Input parser();

        Optional<Styler.Input> styler();

        Optional<StrategoRuntime.Input> strategoRuntime();

        Optional<ConstraintAnalyzer.Input> constraintAnalyzer();


        /// Configuration

        GradleDependency languageProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();

        List<String> fileExtensions();

        List<TypeInfo> taskDefs();

        List<CommandDefRepr> commandDefs();

        List<AutoCommandDefRepr> autoCommandDefs();

        @Value.Default default CliCommandRepr cliCommand() {
            return CliCommandRepr.builder().name(shared().name()).build();
        }

        @Value.Default default List<MenuItemRepr> mainMenuItems() {
            return editorContextMenuItems();
        }

        List<MenuItemRepr> resourceContextMenuItems();

        List<MenuItemRepr> editorContextMenuItems();


        /// Gradle files

        default ResourcePath gradleGenDirectory() {
            return shared().adapterProject().baseDirectory();
        }

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return gradleGenDirectory().appendRelativePath("build.gradle.kts");
        }

        @Value.Default default boolean standaloneProject() {
            return false;
        }

        @Value.Default @SuppressWarnings("immutables:untype") default Optional<ResourcePath> settingsGradleKtsFile() {
            if(standaloneProject()) {
                return Optional.of(gradleGenDirectory().appendRelativePath("settings.gradle.kts"));
            } else {
                return Optional.empty();
            }
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return shared().adapterProject().genSourceSpoofaxJavaDirectory();
        }


        /// Adapter project classes

        // Dagger component

        @Value.Default default TypeInfo genComponent() {
            return TypeInfo.of(shared().adapterPackage(), shared().classSuffix() + "Component");
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
            return TypeInfo.of(shared().adapterPackage(), shared().classSuffix() + "Module");
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
            return TypeInfo.of(shared().adapterPackage(), shared().classSuffix() + "Instance");
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
            return TypeInfo.of(shared().adapterTaskPackage(), shared().classSuffix() + "Check");
        }

        Optional<TypeInfo> manualCheckTaskDef();

        default TypeInfo checkTaskDef() {
            if(classKind().isManual() && manualCheckTaskDef().isPresent()) {
                return manualCheckTaskDef().get();
            }
            return genCheckTaskDef();
        }


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
    public interface Output extends Serializable {
        class Builder extends AdapterProjectData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Parser.AdapterProjectOutput parser();

        Optional<Styler.AdapterProjectOutput> styler();

        Optional<StrategoRuntime.AdapterProjectOutput> strategoRuntime();

        Optional<ConstraintAnalyzer.AdapterProjectOutput> constraintAnalyzer();
    }
}
